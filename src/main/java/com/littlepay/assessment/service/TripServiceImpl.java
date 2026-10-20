package com.littlepay.assessment.service;


import com.littlepay.assessment.repository.TripRepository;
import com.littlepay.assessment.util.TripCsvWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;


@Service
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;

    public TripServiceImpl(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public String exportTripsCsv(LocalDate date) {
        return TripCsvWriter.tripsCsv(tripRepository.findTripsStartedBetween(startOf(date), startOf(date.plusDays(1))));
    }

    private static Instant startOf(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}
