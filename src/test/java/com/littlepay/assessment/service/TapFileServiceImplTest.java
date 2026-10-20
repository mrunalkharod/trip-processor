package com.littlepay.assessment.service;

import com.littlepay.assessment.dto.ImportResponse;
import com.littlepay.assessment.dto.TapFileResponse;
import com.littlepay.assessment.entity.Tap;
import com.littlepay.assessment.entity.TapFile;
import com.littlepay.assessment.entity.TapFileStatus;
import com.littlepay.assessment.exception.TapFileNotFoundException;
import com.littlepay.assessment.mapper.TapFileMapper;
import com.littlepay.assessment.repository.TapBatchRepository;
import com.littlepay.assessment.repository.TapFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TapFileServiceImplTest {

    private static final Instant UPLOADED_AT = Instant.parse("2023-01-25T01:00:00Z");
    private static final int BATCH_SIZE = 2;
    private static final String HEADER = "ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN\n";
    private static final String ONE_TAP = HEADER + "1,22-01-2023 13:00:00,ON,Stop1,Company1,Bus37,5500005555555559\n";

    @TempDir
    private Path uploadDir;

    @Mock
    private TapFileRepository tapFileRepository;

    @Mock
    private TapBatchRepository tapBatchRepository;

    private final List<List<Long>> insertedBatches = new ArrayList<>();

    @Captor
    private ArgumentCaptor<TapFile> tapFileCaptor;

    private TapFileService tapFileService;

    @BeforeEach
    void setUp() {
        tapFileService = new TapFileServiceImpl(tapFileRepository, tapBatchRepository, new TapFileMapper(), uploadDir, BATCH_SIZE);
        lenient().when(tapFileRepository.save(any(TapFile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(tapBatchRepository.insertNew(any())).thenAnswer(invocation -> {
            List<Tap> batch = invocation.getArgument(0);
            insertedBatches.add(batch.stream().map(Tap::getId).toList());
            return batch.size();
        });
    }

    // ---- Upload and status ----

    @Test
    void uploadSavesFileAndRecordsItAsUploaded() throws Exception {
        when(tapFileRepository.save(any(TapFile.class))).thenAnswer(invocation -> {
            TapFile saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 7L);
            return saved;
        });

        TapFileResponse response = tapFileService.upload("taps.csv",
                new ByteArrayInputStream(ONE_TAP.getBytes(StandardCharsets.UTF_8)));

        verify(tapFileRepository).save(tapFileCaptor.capture());
        TapFile saved = tapFileCaptor.getValue();
        assertThat(Files.readString(Path.of(saved.getStoredPath()))).isEqualTo(ONE_TAP);
        assertThat(Path.of(saved.getStoredPath()).getParent()).isEqualTo(uploadDir);
        assertThat(response.tapFileId()).isEqualTo(7L);
        assertThat(response.fileName()).isEqualTo("taps.csv");
        assertThat(response.status()).isEqualTo(TapFileStatus.UPLOADED);
        assertThat(response.uploadedAt()).isNotNull();
    }

    @Test
    void getTapFileReturnsItsStatus() {
        TapFile tapFile = new TapFile("taps.csv", "/data/uploads/a.csv", UPLOADED_AT);
        tapFile.markProcessed(6, 0, UPLOADED_AT.plusSeconds(1));
        when(tapFileRepository.findById(7L)).thenReturn(Optional.of(tapFile));

        TapFileResponse response = tapFileService.getTapFile(7L);

        assertThat(response.status()).isEqualTo(TapFileStatus.PROCESSED);
        assertThat(response.storedTaps()).isEqualTo(6);
    }

    @Test
    void getTapFileThrowsWhenNotFound() {
        when(tapFileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tapFileService.getTapFile(99L)).isInstanceOf(TapFileNotFoundException.class);
    }


    @Test
    void storesGoodRowsInBatchesAndCountsBadRows() throws IOException {
        TapFile tapFile = tapFile(1, HEADER + """
                1, 22-01-2023 13:00:00, ON, Stop1, Company1, Bus37, 5500005555555559
                2, 22-01-2023 13:05:00, OFF, Stop2, Company1, Bus37, 5500005555555559

                3, 22-01-2023 09:20:00, IN, Stop3, Company1, Bus36, 4111111111111111
                4, 23-01-2023 08:00:00, ON, Stop1, Company1, Bus37, 4111111111111111
                5, 23-01-2023 08:02:00, OFF, Stop1, Company1, Bus37, 4111111111111111
                """);
        givenFilesToImport(tapFile);

        ImportResponse response = tapFileService.importTaps();

        assertThat(insertedBatches).containsExactly(List.of(1L, 2L), List.of(4L, 5L));
        assertThat(tapFile.getStatus()).isEqualTo(TapFileStatus.PROCESSED);
        assertThat(tapFile.getStoredTaps()).isEqualTo(4);
        assertThat(tapFile.getBadRows()).isEqualTo(1);
        assertThat(tapFile.getProcessedAt()).isNotNull();
        assertThat(response).isEqualTo(new ImportResponse(1, 0, 4, 1));
    }

    @Test
    void importsEveryWaitingFileAndCountsTheFailedOnes() throws IOException {
        TapFile good = tapFile(1, ONE_TAP);
        TapFile missing = new TapFile("gone.csv", uploadDir.resolve("gone.csv").toString(), UPLOADED_AT);
        ReflectionTestUtils.setField(missing, "id", 2L);
        givenFilesToImport(good, missing);

        ImportResponse response = tapFileService.importTaps();

        assertThat(good.getStatus()).isEqualTo(TapFileStatus.PROCESSED);
        assertThat(missing.getStatus()).isEqualTo(TapFileStatus.FAILED);
        assertThat(missing.getErrorMessage()).contains("gone.csv");
        assertThat(response).isEqualTo(new ImportResponse(1, 1, 1, 0));
    }

    @Test
    void readsUploadedFilesAndOnesLeftProcessing() {
        givenFilesToImport();

        ImportResponse response = tapFileService.importTaps();

        verify(tapFileRepository).findByStatusInOrderByIdAsc(List.of(TapFileStatus.UPLOADED, TapFileStatus.PROCESSING));
        assertThat(response).isEqualTo(new ImportResponse(0, 0, 0, 0));
    }

    @Test
    void columnsAreFoundByNameInAnyOrder() throws IOException {
        TapFile tapFile = tapFile(1, """
                pan,busid,companyid,stopid,taptype,datetimeutc,id
                5500005555555559,Bus37,Company1,Stop1,ON,22-01-2023 13:00:00,1
                """);
        givenFilesToImport(tapFile);

        tapFileService.importTaps();

        assertThat(insertedBatches).containsExactly(List.of(1L));
        assertThat(tapFile.getStoredTaps()).isEqualTo(1);
    }

    @Test
    void fileWithoutHeaderRowMarksTheFileFailed() throws IOException {
        TapFile tapFile = tapFile(1, "1,22-01-2023 13:00:00,ON,Stop1,Company1,Bus37,5500005555555559\n");
        givenFilesToImport(tapFile);

        tapFileService.importTaps();

        assertThat(tapFile.getStatus()).isEqualTo(TapFileStatus.FAILED);
        assertThat(tapFile.getErrorMessage()).contains("missing column(s) ID, DateTimeUTC");
        verify(tapBatchRepository, never()).insertNew(any());
    }

    @Test
    void unclosedQuoteMarksTheFileFailed() throws IOException {
        TapFile tapFile = tapFile(1, HEADER
                + "1, 22-01-2023 13:00:00, ON, \"Stop1, Company1, Bus37, 5500005555555559\n");
        givenFilesToImport(tapFile);

        tapFileService.importTaps();

        assertThat(tapFile.getStatus()).isEqualTo(TapFileStatus.FAILED);
        verify(tapBatchRepository, never()).insertNew(any());
    }

    @Test
    void tapsAlreadyStoredAreNotCounted() throws IOException {
        TapFile tapFile = tapFile(1, ONE_TAP);
        givenFilesToImport(tapFile);
        doReturn(0).when(tapBatchRepository).insertNew(any());

        ImportResponse response = tapFileService.importTaps();

        assertThat(tapFile.getStatus()).isEqualTo(TapFileStatus.PROCESSED);
        assertThat(tapFile.getStoredTaps()).isZero();
        assertThat(response.tapsStored()).isZero();
    }

    private TapFile tapFile(long id, String content) throws IOException {
        Path storedPath = Files.writeString(uploadDir.resolve("upload-" + id + ".csv"), content);
        TapFile tapFile = new TapFile("taps.csv", storedPath.toString(), UPLOADED_AT);
        ReflectionTestUtils.setField(tapFile, "id", id);
        return tapFile;
    }

    private void givenFilesToImport(TapFile... tapFiles) {
        when(tapFileRepository.findByStatusInOrderByIdAsc(List.of(TapFileStatus.UPLOADED, TapFileStatus.PROCESSING)))
                .thenReturn(List.of(tapFiles));
    }
}
