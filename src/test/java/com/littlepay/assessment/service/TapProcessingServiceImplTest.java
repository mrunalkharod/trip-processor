package com.littlepay.assessment.service;


import com.littlepay.assessment.dto.ProcessingResponse;
import com.littlepay.assessment.entity.Tap;
import com.littlepay.assessment.entity.TapType;
import com.littlepay.assessment.entity.Trip;
import com.littlepay.assessment.repository.TapBatchRepository;
import com.littlepay.assessment.repository.TapRepository;
import com.littlepay.assessment.repository.TripBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionOperations;

import java.math.BigDecimal;
import java.util.List;


import static com.littlepay.assessment.helper.TapFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TapProcessingServiceImplTest {

    private static final int CARDS_PER_CHUNK = 1;

    @Mock
    private TapRepository tapRepository;

    @Mock
    private TapBatchRepository tapBatchRepository;

    @Mock
    private TripBatchRepository tripBatchRepository;

    @Mock
    private TripMatcher tripMatcher;

    private TapProcessingService tapProcessingService;

    @BeforeEach
    void setUp() {
        tapProcessingService = new TapProcessingServiceImpl(tapRepository, tapBatchRepository, tripBatchRepository,
                tripMatcher, TransactionOperations.withoutTransaction(), CARDS_PER_CHUNK);
    }

    @Test
    void eachChunkIsSavedAndAllItsTapsMarkedProcessed() {
        // Chunk 1: card PAN with a completed trip, an orphan tap OFF and an unmatched tap ON
        Tap on1 = tapOn(1, "22-01-2023 13:00:00", "Stop1", "Bus37");
        Tap off2 = tapOff(2, "22-01-2023 13:05:00", "Stop2", "Bus37");
        Tap off3 = tapOff(3, "22-01-2023 14:00:00", "Stop2", "Bus36");
        Tap on4 = tapOn(4, "22-01-2023 15:00:00", "Stop1", "Bus37");
        List<Tap> chunk1 = List.of(on1, off2, off3, on4);
        MatchResult result1 = new MatchResult();
        Trip completed = Trip.completed(on1, off2, new BigDecimal("3.25"));
        Trip incomplete4 = Trip.incomplete(on4, new BigDecimal("7.30"));
        result1.addTrip(completed);
        result1.addOrphanTap(off3);
        result1.addTrip(incomplete4);

        // Chunk 2: card OTHER_PAN
        Tap on5 = tap(5, "22-01-2023 09:20:00", TapType.ON, "Stop3", "Bus36", COMPANY, OTHER_PAN);
        List<Tap> chunk2 = List.of(on5);
        MatchResult result2 = new MatchResult();
        Trip incomplete5 = Trip.incomplete(on5, new BigDecimal("7.30"));
        result2.addTrip(incomplete5);

        when(tapRepository.findUnprocessedChunk(CARDS_PER_CHUNK)).thenReturn(chunk1, chunk2, List.of());
        when(tripMatcher.match(chunk1)).thenReturn(result1);
        when(tripMatcher.match(chunk2)).thenReturn(result2);

        ProcessingResponse response = tapProcessingService.processTaps();

        verify(tripBatchRepository).insertTrips(List.of(completed, incomplete4));
        verify(tapBatchRepository).markProcessed(List.of(1L, 2L, 3L, 4L));
        verify(tripBatchRepository).insertTrips(List.of(incomplete5));
        verify(tapBatchRepository).markProcessed(List.of(5L));
        assertThat(response).isEqualTo(new ProcessingResponse(5, 3, 1));
    }

    @Test
    void noUnprocessedTapsSavesNothing() {
        when(tapRepository.findUnprocessedChunk(CARDS_PER_CHUNK)).thenReturn(List.of());

        ProcessingResponse response = tapProcessingService.processTaps();

        verify(tripBatchRepository, never()).insertTrips(any());
        verify(tapBatchRepository, never()).markProcessed(any());
        assertThat(response).isEqualTo(new ProcessingResponse(0, 0, 0));
    }

    @Test
    void failureStopsTheRun() {
        Tap on1 = tapOn(1, "22-01-2023 13:00:00", "Stop1", "Bus37");
        when(tapRepository.findUnprocessedChunk(CARDS_PER_CHUNK)).thenReturn(List.of(on1));
        when(tripMatcher.match(List.of(on1))).thenThrow(new IllegalStateException("No stops for bus Bus37"));

        assertThatThrownBy(() -> tapProcessingService.processTaps()).isInstanceOf(IllegalStateException.class);
        verify(tapBatchRepository, never()).markProcessed(any());
    }
}
