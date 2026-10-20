package com.littlepay.assessment.service;


import com.littlepay.assessment.dto.ProcessingResponse;
import com.littlepay.assessment.entity.Tap;
import com.littlepay.assessment.repository.TapBatchRepository;
import com.littlepay.assessment.repository.TapRepository;
import com.littlepay.assessment.repository.TripBatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionOperations;

import java.util.List;


@Service
public class TapProcessingServiceImpl implements TapProcessingService {

    private static final Logger log = LoggerFactory.getLogger(TapProcessingServiceImpl.class);

    private final TapRepository tapRepository;
    private final TapBatchRepository tapBatchRepository;
    private final TripBatchRepository tripBatchRepository;
    private final TripMatcher tripMatcher;
    private final TransactionOperations transactionOperations;
    private final int cardsPerChunk;

    public TapProcessingServiceImpl(TapRepository tapRepository,
                                    TapBatchRepository tapBatchRepository,
                                    TripBatchRepository tripBatchRepository,
                                    TripMatcher tripMatcher,
                                    TransactionOperations transactionOperations,
                                    @Value("${app.processing.cards-per-chunk}") int cardsPerChunk) {
        this.tapRepository = tapRepository;
        this.tapBatchRepository = tapBatchRepository;
        this.tripBatchRepository = tripBatchRepository;
        this.tripMatcher = tripMatcher;
        this.transactionOperations = transactionOperations;
        this.cardsPerChunk = cardsPerChunk;
    }

    @Override
    public ProcessingResponse processTaps() {
        ProcessingResponse total = new ProcessingResponse(0, 0, 0);
        int chunks = 0;
        ProcessingResponse chunk;
        while ((chunk = transactionOperations.execute(status -> processChunk())) != null) {
            chunks++;
            total = add(total, chunk);
            log.debug("Chunk {} done: {}", chunks, chunk);
        }
        log.info("Processed taps in {} chunk(s): {}", chunks, total);
        return total;
    }

    /** Processes the next chunk of cards; {@code null} when no UNPROCESSED taps are left. */
    private ProcessingResponse processChunk() {
        List<Tap> taps = tapRepository.findUnprocessedChunk(cardsPerChunk);
        if (taps.isEmpty()) {
            return null;
        }
        MatchResult result = tripMatcher.match(taps);
        tripBatchRepository.insertTrips(result.trips());
        // Every tap is now in a trip or logged as an orphan, so the whole chunk is done.
        tapBatchRepository.markProcessed(taps.stream().map(Tap::getId).toList());
        return new ProcessingResponse(taps.size(), result.trips().size(), result.orphanTaps().size());
    }

    private static ProcessingResponse add(ProcessingResponse a, ProcessingResponse b) {
        return new ProcessingResponse(a.tapsProcessed() + b.tapsProcessed(),
                a.tripsCreated() + b.tripsCreated(), a.orphanTapsLogged() + b.orphanTapsLogged());
    }
}
