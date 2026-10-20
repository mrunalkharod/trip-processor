package com.littlepay.assessment.service;

import com.littlepay.assessment.entity.Trip;
import com.littlepay.assessment.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;


import static com.littlepay.assessment.helper.TapFixtures.tapOff;
import static com.littlepay.assessment.helper.TapFixtures.tapOn;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripServiceImplTest {

    private static final LocalDate DAY = LocalDate.of(2023, 1, 22);
    private static final Instant DAY_START = Instant.parse("2023-01-22T00:00:00Z");
    private static final Instant NEXT_DAY_START = Instant.parse("2023-01-23T00:00:00Z");

    private final Trip trip = Trip.completed(tapOn(1, "22-01-2023 13:00:00", "Stop1", "Bus37"),
            tapOff(2, "22-01-2023 13:05:00", "Stop2", "Bus37"), new BigDecimal("3.25"));

    @Mock
    private TripRepository tripRepository;

    private TripService tripService;

    @BeforeEach
    void setUp() {
        tripService = new TripServiceImpl(tripRepository);
    }

    @Test
    void exportWritesEveryCompanysTripsForTheDayInSpecificationCsv() {
        when(tripRepository.findTripsStartedBetween(DAY_START, NEXT_DAY_START)).thenReturn(List.of(trip));

        assertThat(tripService.exportTripsCsv(DAY)).contains(
                "22-01-2023 13:00:00, 22-01-2023 13:05:00, 300, Stop1, Stop2, $3.25, "
                        + "Company1, Bus37, 550000******5559, COMPLETED");
    }
}
