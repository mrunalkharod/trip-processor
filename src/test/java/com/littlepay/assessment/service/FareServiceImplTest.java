package com.littlepay.assessment.service;


import com.littlepay.assessment.entity.Bus;
import com.littlepay.assessment.entity.Fare;
import com.littlepay.assessment.exception.FareNotFoundException;
import com.littlepay.assessment.repository.FareRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FareServiceImplTest {

    @Mock
    private FareRepository fareRepository;

    private FareService fareService;

    @BeforeEach
    void setUp() {
        fareService = new FareServiceImpl(fareRepository, new BigDecimal("1.45"));
    }

    @Test
    void fareUsesTheBusStopValues() {
        givenStops("Bus37", "0.00", "1.80", "5.85");

        assertThat(fareService.fareBetween("Company1", "Bus37", "Stop3", "Stop1")).isEqualByComparingTo("7.30");
        assertThat(fareService.fareBetween("Company1", "Bus37", "Stop1", "Stop2")).isEqualByComparingTo("3.25");
        assertThat(fareService.fareBetween("Company1", "Bus37", "Stop2", "Stop3")).isEqualByComparingTo("5.50");
    }

    @Test
    void maxFareFromStopIsFareToTheBusFarthestStop() {
        givenStops("Bus37", "0.00", "1.80", "5.85");

        assertThat(fareService.maxFareFrom("Company1", "Bus37", "Stop2")).isEqualByComparingTo("5.50");
        assertThat(fareService.maxFareFrom("Company1", "Bus37", "Stop1")).isEqualByComparingTo("7.30");
    }

    @Test
    void busWithoutStopsFails() {
        when(fareRepository.findBusStops("Company1", "Bus99")).thenReturn(List.of());

        assertThatThrownBy(() -> fareService.fareBetween("Company1", "Bus99", "Stop1", "Stop2"))
                .isInstanceOf(FareNotFoundException.class)
                .hasMessageContaining("Bus99");
    }

    @Test
    void stopNotServedByTheBusFails() {
        givenStops("Bus37", "0.00", "1.80", "5.85");

        assertThatThrownBy(() -> fareService.maxFareFrom("Company1", "Bus37", "Stop9"))
                .isInstanceOf(FareNotFoundException.class)
                .hasMessageContaining("Stop9");
    }

    /** The bus serves Stop1, Stop2, Stop3 with the given distance values. */
    private void givenStops(String busId, String stop1, String stop2, String stop3) {
        Bus bus = new Bus("Company1", busId);
        List<Fare> stops = List.of(
                bus.putStop("Stop1", new BigDecimal(stop1)),
                bus.putStop("Stop2", new BigDecimal(stop2)),
                bus.putStop("Stop3", new BigDecimal(stop3)));
        when(fareRepository.findBusStops("Company1", busId)).thenReturn(stops);
    }
}
