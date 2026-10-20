package com.littlepay.assessment.rule;


import com.littlepay.assessment.entity.Tap;
import com.littlepay.assessment.entity.Trip;
import com.littlepay.assessment.entity.TripStatus;
import com.littlepay.assessment.service.FareService;
import com.littlepay.assessment.service.MatchResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static com.littlepay.assessment.helper.TapFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class TripRulesTest {

    @Mock
    private FareService fareService;

    private final Tap onStop1 = tapOn(1, "22-01-2023 08:00:00", "Stop1", "Bus37");
    private final Tap offStop1 = tapOff(2, "22-01-2023 08:02:00", "Stop1", "Bus37");
    private final Tap offStop2 = tapOff(3, "22-01-2023 08:10:00", "Stop2", "Bus37");

    private final TapPair completed = new TapPair(onStop1, offStop2);
    private final TapPair cancelled = new TapPair(onStop1, offStop1);
    private final TapPair tapOnOnly = TapPair.tapOnOnly(onStop1);
    private final TapPair tapOffOnly = TapPair.tapOffOnly(offStop2);

    @Test
    void completedTripRuleChargesTheFareBetweenTheTwoStops() {
        when(fareService.fareBetween("Company1", "Bus37", "Stop1", "Stop2")).thenReturn(new BigDecimal("3.25"));
        CompletedTripRule rule = new CompletedTripRule(fareService);
        assertAppliesOnlyTo(rule, completed);

        MatchResult result = new MatchResult();
        rule.apply(completed, result);

        assertThat(result.trips()).hasSize(1);
        Trip trip = result.trips().get(0);
        assertThat(trip.getStatus()).isEqualTo(TripStatus.COMPLETED);
        assertThat(trip.getChargeAmount()).isEqualByComparingTo("3.25");
        assertThat(trip.getStarted()).isEqualTo(utc("22-01-2023 08:00:00"));
        assertThat(trip.getFinished()).isEqualTo(utc("22-01-2023 08:10:00"));
        assertThat(trip.getDurationSecs()).isEqualTo(600L);
        assertThat(trip.getFromStopId()).isEqualTo("Stop1");
        assertThat(trip.getToStopId()).isEqualTo("Stop2");
        assertThat(trip.getOnTapId()).isEqualTo(1L);
        assertThat(trip.getOffTapId()).isEqualTo(3L);
        assertThat(result.orphanTaps()).isEmpty();
    }

    @Test
    void cancelledTripRuleChargesNothingAndNeedsNoFare() {
        CancelledTripRule rule = new CancelledTripRule();
        assertAppliesOnlyTo(rule, cancelled);

        MatchResult result = new MatchResult();
        rule.apply(cancelled, result);

        Trip trip = result.trips().get(0);
        assertThat(trip.getStatus()).isEqualTo(TripStatus.CANCELLED);
        assertThat(trip.getChargeAmount()).isEqualByComparingTo("0.00");
        assertThat(trip.getDurationSecs()).isEqualTo(120L);
        assertThat(trip.getOnTapId()).isEqualTo(1L);
        assertThat(trip.getOffTapId()).isEqualTo(2L);
        verifyNoInteractions(fareService);
    }

    @Test
    void incompleteTripRuleChargesTheMaximumFareFromTheStop() {
        when(fareService.maxFareFrom("Company1", "Bus37", "Stop1")).thenReturn(new BigDecimal("7.30"));
        IncompleteTripRule rule = new IncompleteTripRule(fareService);
        assertAppliesOnlyTo(rule, tapOnOnly);

        MatchResult result = new MatchResult();
        rule.apply(tapOnOnly, result);

        Trip trip = result.trips().get(0);
        assertThat(trip.getStatus()).isEqualTo(TripStatus.INCOMPLETE);
        assertThat(trip.getChargeAmount()).isEqualByComparingTo("7.30");
        assertThat(trip.getFinished()).isNull();
        assertThat(trip.getDurationSecs()).isNull();
        assertThat(trip.getToStopId()).isNull();
        assertThat(trip.getOffTapId()).isNull();
    }

    @Test
    void orphanTapRuleLogsTheTapOffWithoutChargingAndNeedsNoFare() {
        OrphanTapRule rule = new OrphanTapRule();
        assertAppliesOnlyTo(rule, tapOffOnly);

        MatchResult result = new MatchResult();
        rule.apply(tapOffOnly, result);

        assertThat(result.trips()).isEmpty();
        assertThat(result.orphanTaps()).hasSize(1);
        assertThat(result.orphanTaps().get(0).getId()).isEqualTo(3L);
        verifyNoInteractions(fareService);
    }

    private void assertAppliesOnlyTo(TripRule rule, TapPair expected) {
        for (TapPair pair : new TapPair[]{completed, cancelled, tapOnOnly, tapOffOnly}) {
            assertThat(rule.appliesTo(pair)).isEqualTo(pair == expected);
        }
    }
}
