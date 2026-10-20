package com.littlepay.assessment.service;


import com.littlepay.assessment.dto.ImportResponse;
import com.littlepay.assessment.dto.TapFileResponse;
import com.littlepay.assessment.entity.Tap;
import com.littlepay.assessment.entity.TapFile;
import com.littlepay.assessment.entity.TapFileStatus;
import com.littlepay.assessment.exception.InvalidTapFileException;
import com.littlepay.assessment.exception.TapFileNotFoundException;
import com.littlepay.assessment.mapper.TapFileMapper;
import com.littlepay.assessment.repository.TapBatchRepository;
import com.littlepay.assessment.repository.TapFileRepository;
import com.littlepay.assessment.util.TapCsvParser;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class TapFileServiceImpl implements TapFileService {

    private static final Logger log = LoggerFactory.getLogger(TapFileServiceImpl.class);

    private static final List<TapFileStatus> TO_IMPORT = List.of(TapFileStatus.UPLOADED, TapFileStatus.PROCESSING);

    private final TapFileRepository tapFileRepository;
    private final TapBatchRepository tapBatchRepository;
    private final TapFileMapper tapFileMapper;
    private final Path uploadDir;
    private final int batchSize;

    public TapFileServiceImpl(TapFileRepository tapFileRepository,
                              TapBatchRepository tapBatchRepository,
                              TapFileMapper tapFileMapper,
                              @Value("${app.upload.dir}") Path uploadDir,
                              @Value("${app.upload.batch-size}") int batchSize) {
        this.tapFileRepository = tapFileRepository;
        this.tapBatchRepository = tapBatchRepository;
        this.tapFileMapper = tapFileMapper;
        this.uploadDir = uploadDir;
        this.batchSize = batchSize;
    }

    @Override
    public TapFileResponse upload(String fileName, InputStream content) throws IOException {
        Files.createDirectories(uploadDir);
        Path storedPath = uploadDir.resolve(UUID.randomUUID() + ".csv");
        Files.copy(content, storedPath);

        TapFile tapFile = tapFileRepository.save(new TapFile(fileName, storedPath.toString(), Instant.now()));
        return tapFileMapper.toResponse(tapFile);
    }

    @Override
    @Transactional(readOnly = true)
    public TapFileResponse getTapFile(Long tapFileId) {
        return tapFileRepository.findById(tapFileId)
                .map(tapFileMapper::toResponse)
                .orElseThrow(() -> new TapFileNotFoundException(tapFileId));
    }

    @Override
    public ImportResponse importTaps() {
        int imported = 0;
        int failed = 0;
        int storedTaps = 0;
        int badRows = 0;
        for (TapFile tapFile : tapFileRepository.findByStatusInOrderByIdAsc(TO_IMPORT)) {
            importFile(tapFile);
            if (tapFile.getStatus() == TapFileStatus.PROCESSED) {
                imported++;
                storedTaps += tapFile.getStoredTaps();
                badRows += tapFile.getBadRows();
            } else {
                failed++;
            }
        }
        ImportResponse response = new ImportResponse(imported, failed, storedTaps, badRows);
        log.info("Imported tap files: {}", response);
        return response;
    }

    private void importFile(TapFile tapFile) {
        tapFile.markProcessing();
        tapFileRepository.save(tapFile);
        try {
            readFile(tapFile);
        } catch (IOException | RuntimeException e) {
            log.error("Tap file {} failed", tapFile.getId(), e);
            tapFile.markFailed(e.getMessage(), Instant.now());
        }
        tapFileRepository.save(tapFile);
        log.info("Tap file {} finished: {}", tapFile.getId(), tapFile);
    }

    private void readFile(TapFile tapFile) throws IOException {
        int storedTaps = 0;
        int badRows = 0;
        List<Tap> batch = new ArrayList<>(batchSize);

        try (Reader reader = Files.newBufferedReader(Path.of(tapFile.getStoredPath()), StandardCharsets.UTF_8);
             CSVParser csv = TapCsvParser.open(reader)) {
            for (CSVRecord record : csv) {
                long lineNumber = csv.getCurrentLineNumber();
                try {
                    batch.add(TapCsvParser.parse(record, lineNumber));
                } catch (InvalidTapFileException e) {
                    // The row itself isn't logged: it holds the PAN
                    log.warn("Bad row in tap file {}: {}", tapFile.getId(), e.getMessage());
                    badRows++;
                }
                if (batch.size() >= batchSize) {
                    storedTaps += tapBatchRepository.insertNew(batch);
                    batch.clear();
                }
            }
        }
        if (!batch.isEmpty()) {
            storedTaps += tapBatchRepository.insertNew(batch);
        }
        tapFile.markProcessed(storedTaps, badRows, Instant.now());
    }
}
