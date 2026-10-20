package com.littlepay.assessment.service;


import com.littlepay.assessment.entity.*;
import com.littlepay.assessment.exception.FareNotFoundException;
import com.littlepay.assessment.repository.FareRepository;
import com.littlepay.assessment.rule.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.littlepay.assessment.helper.TapFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

class TripMatcherTest {

    private final FareRepository fareRepository = mock(FareRepository.class);
    private final FareService fareService = new FareServiceImpl(fareRepository, new BigDecimal("1.45"));

    private TripMatcher tripMatcher;

    @BeforeEach
    void setUp() {
        setUpStops();
        tripMatcher = new TripMatcher(defaultRules());
    }


    private void setUpStops() {
        givenStops("Bus36", "Stop1", "0.00", "Stop2", "1.80", "Stop3", "5.85");
        givenStops("Bus37", "Stop1", "0.00", "Stop2", "1.80", "Stop3", "5.85");
        givenStops("Bus50", "Stop1", "0.00", "Stop3", "3.35", "Stop4", "4.55");
    }

    private void givenStops(String busId, String... stopIdsAndValues) {
        Bus bus = new Bus("Company1", busId);
        List<Fare> stops = new ArrayList<>();
        for (int i = 0; i < stopIdsAndValues.length; i += 2) {
            stops.add(bus.putStop(stopIdsAndValues[i], new BigDecimal(stopIdsAndValues[i + 1])));
        }
        lenient().when(fareRepository.findBusStops("Company1", busId)).thenReturn(stops);
    }

    /** The rules in the same order Spring injects them (by @Order). */
    private List<TripRule> defaultRules() {
        return List.of(
                new CompletedTripRule(fareService),
                new CancelledTripRule(),
                new IncompleteTripRule(fareService),
                new OrphanTapRule());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("singleTripCases")
    void testSingleTripStatusAndCharge(String scenario, List<Tap> taps, TripStatus expectedStatus, String expectedCharge) {
        MatchResult result = tripMatcher.match(taps);

        assertThat(result.trips()).hasSize(1);
        assertThat(result.trips().get(0).getStatus()).isEqualTo(expectedStatus);
        assertThat(result.trips().get(0).getChargeAmount()).isEqualByComparingTo(expectedCharge);
        assertThat(result.orphanTaps()).isEmpty();
    }

    static Stream<Arguments> singleTripCases() {
        return Stream.of(
                arguments("Tap on Stop1, tap off Stop2 is COMPLETED at $3.25",
                        List.of(tapOn(1, "22-01-2023 13:00:00", "Stop1", "Bus37"),
                                tapOff(2, "22-01-2023 13:05:00", "Stop2", "Bus37")),
                        TripStatus.COMPLETED, "3.25"),
                arguments("Fares apply in either direction: Stop3 to Stop1 is $7.30",
                        List.of(tapOn(1, "22-01-2023 08:00:00", "Stop3", "Bus37"),
                                tapOff(2, "22-01-2023 08:30:00", "Stop1", "Bus37")),
                        TripStatus.COMPLETED, "7.30"),
                arguments("Stop1 to Stop3 on express Bus50 is $4.80",
                        List.of(tapOn(1, "22-01-2023 08:00:00", "Stop1", "Bus50"),
                                tapOff(2, "22-01-2023 08:20:00", "Stop3", "Bus50")),
                        TripStatus.COMPLETED, "4.80"),
                arguments("Tap on and off at the same stop is CANCELLED at $0.00",
                        List.of(tapOn(1, "23-01-2023 08:00:00", "Stop1", "Bus37"),
                                tapOff(2, "23-01-2023 08:02:00", "Stop1", "Bus37")),
                        TripStatus.CANCELLED, "0.00"),
                arguments("No tap off from Stop2 is INCOMPLETE at the max fare $5.50",
                        List.of(tapOn(1, "22-01-2023 08:00:00", "Stop2", "Bus37")),
                        TripStatus.INCOMPLETE, "5.50"),
                arguments("No tap off on express Bus50 uses that bus's max fare $6.00",
                        List.of(tapOn(1, "22-01-2023 08:00:00", "Stop1", "Bus50")),
                        TripStatus.INCOMPLETE, "6.00"));
    }

    @Test
    void completedTripHasAllFieldsSet() {
        Trip trip = tripMatcher.match(List.of(
                tapOn(1, "22-01-2023 13:00:00", "Stop1", "Bus37"),
                tapOff(2, "22-01-2023 13:05:00", "Stop2", "Bus37"))).trips().get(0);

        assertThat(trip.getStarted()).isEqualTo(utc("22-01-2023 13:00:00"));
        assertThat(trip.getFinished()).isEqualTo(utc("22-01-2023 13:05:00"));
        assertThat(trip.getDurationSecs()).isEqualTo(300L);
        assertThat(trip.getFromStopId()).isEqualTo("Stop1");
        assertThat(trip.getToStopId()).isEqualTo("Stop2");
        assertThat(trip.getCompanyId()).isEqualTo("Company1");
        assertThat(trip.getBusId()).isEqualTo("Bus37");
        assertThat(trip.getPan()).isEqualTo(PAN);
        assertThat(trip.getOnTapId()).isEqualTo(1L);
        assertThat(trip.getOffTapId()).isEqualTo(2L);
    }

    @Test
    void incompleteTripLeavesTapOffFieldsEmpty() {
        Trip trip = tripMatcher.match(List.of(tapOn(1, "22-01-2023 08:00:00", "Stop2", "Bus37")))
                .trips().get(0);

        assertThat(trip.getFinished()).isNull();
        assertThat(trip.getDurationSecs()).isNull();
        assertThat(trip.getToStopId()).isNull();
        assertThat(trip.getOffTapId()).isNull();
    }

    @Test
    void tapOffWithoutTapOnIsOrphanAndNotCharged() {
        MatchResult result = tripMatcher.match(List.of(tapOff(6, "24-01-2023 16:30:00", "Stop2", "Bus37")));

        assertThat(result.trips()).isEmpty();
        assertThat(result.orphanTaps()).extracting(Tap::getId).containsExactly(6L);
    }

    @Test
    void missedTapOffThenTapOnAnotherBusMakesFirstTripIncomplete() {
        MatchResult result = tripMatcher.match(List.of(
                tapOn(1, "22-01-2023 08:00:00", "Stop2", "Bus37"),
                tapOn(2, "22-01-2023 12:00:00", "Stop1", "Bus36"),
                tapOff(3, "22-01-2023 12:20:00", "Stop3", "Bus36")));

        assertThat(result.trips()).extracting(Trip::getStatus)
                .containsExactly(TripStatus.INCOMPLETE, TripStatus.COMPLETED);
        assertThat(result.trips()).extracting(Trip::getChargeAmount)
                .containsExactly(new BigDecimal("5.50"), new BigDecimal("7.30"));
        assertThat(result.orphanTaps()).isEmpty();
    }

    @Test
    void eachCardIsMatchedSeparately() {
        MatchResult result = tripMatcher.match(List.of(
                tap(1, "22-01-2023 08:00:00", TapType.ON, "Stop1", "Bus37", "Company1", PAN),
                tap(2, "22-01-2023 08:01:00", TapType.ON, "Stop1", "Bus37", "Company1", OTHER_PAN),
                tap(3, "22-01-2023 08:10:00", TapType.OFF, "Stop2", "Bus37", "Company1", PAN)));

        assertThat(result.trips()).extracting(Trip::getPan).containsExactly(PAN, OTHER_PAN);
        assertThat(result.trips()).extracting(Trip::getStatus)
                .containsExactly(TripStatus.COMPLETED, TripStatus.INCOMPLETE);
    }

    @Test
    void specificationExample() {
        // The whole example taps.csv, processed in one run (ordered as the repository returns them).
        MatchResult result = tripMatcher.match(List.of(
                tap(3, "22-01-2023 09:20:00", TapType.ON, "Stop3", "Bus36", "Company1", OTHER_PAN),
                tap(4, "23-01-2023 08:00:00", TapType.ON, "Stop1", "Bus37", "Company1", OTHER_PAN),
                tap(5, "23-01-2023 08:02:00", TapType.OFF, "Stop1", "Bus37", "Company1", OTHER_PAN),
                tap(1, "22-01-2023 13:00:00", TapType.ON, "Stop1", "Bus37", "Company1", PAN),
                tap(2, "22-01-2023 13:05:00", TapType.OFF, "Stop2", "Bus37", "Company1", PAN),
                tap(6, "24-01-2023 16:30:00", TapType.OFF, "Stop2", "Bus37", "Company1", PAN)));

        assertThat(result.trips()).extracting(Trip::getOnTapId).containsExactly(3L, 4L, 1L);
        assertThat(result.trips()).extracting(Trip::getStatus)
                .containsExactly(TripStatus.INCOMPLETE, TripStatus.CANCELLED, TripStatus.COMPLETED);
        assertThat(result.trips()).extracting(Trip::getChargeAmount)
                .containsExactly(new BigDecimal("7.30"), new BigDecimal("0.00"), new BigDecimal("3.25"));
        assertThat(result.orphanTaps()).extracting(Tap::getId).containsExactly(6L);
    }

    @Test
    void tapWithAnotherCompanyDoesNotCloseOpenTrip() {
        MatchResult result = tripMatcher.match(List.of(
                tap(1, "22-01-2023 08:00:00", TapType.ON, "Stop1", "Bus37", "Company1", PAN),
                tap(3, "22-01-2023 08:20:00", TapType.OFF, "Stop3", "Bus37", "Company1", PAN),
                tap(2, "22-01-2023 08:05:00", TapType.OFF, "Stop2", "Bus37", "Company2", PAN)));

        assertThat(result.trips()).extracting(Trip::getStatus).containsExactly(TripStatus.COMPLETED);
        assertThat(result.trips()).extracting(Trip::getOffTapId).containsExactly(3L);
        assertThat(result.orphanTaps()).extracting(Tap::getId).containsExactly(2L);
    }

    @Test
    void missingFareFailsInsteadOfChargingZero() {
        List<Tap> taps = List.of(
                tapOn(1, "22-01-2023 08:00:00", "Stop1", "Bus37"),
                tapOff(2, "22-01-2023 08:10:00", "Stop9", "Bus37"));

        assertThatThrownBy(() -> tripMatcher.match(taps)).isInstanceOf(FareNotFoundException.class);
    }

    @Test
    void newRuleCanBeAddedWithoutChangingExistingRules() {
        // Example of a future rule: a pair more than 3 hours apart is treated as a missed tap OFF.
        TripRule maxTripDuration = new TripRule() {
            @Override
            public boolean appliesTo(TapPair pair) {
                return pair.isComplete() && Duration.between(pair.tapOn().getTapTime(), pair.tapOff().getTapTime())
                        .compareTo(Duration.ofHours(3)) > 0;
            }

            @Override
            public void apply(TapPair pair, MatchResult result) {
                new IncompleteTripRule(fareService).apply(TapPair.tapOnOnly(pair.tapOn()), result);
                new OrphanTapRule().apply(TapPair.tapOffOnly(pair.tapOff()), result);
            }
        };
        List<TripRule> rules = new ArrayList<>(defaultRules());
        rules.add(0, maxTripDuration);
        TripMatcher withNewRule = new TripMatcher(rules);

        MatchResult result = withNewRule.match(List.of(
                tapOn(1, "22-01-2023 08:00:00", "Stop1", "Bus37"),
                tapOff(2, "22-01-2023 18:00:00", "Stop2", "Bus37")));

        assertThat(result.trips()).extracting(Trip::getStatus).containsExactly(TripStatus.INCOMPLETE);
        assertThat(result.orphanTaps()).extracting(Tap::getId).containsExactly(2L);
    }

    // ---- Step 1: pairing ----

    private final Tap on1 = tapOn(1, "22-01-2023 08:00:00", "Stop1", "Bus37");
    private final Tap off2 = tapOff(2, "22-01-2023 08:10:00", "Stop2", "Bus37");

    @Test
    void tapOnAndTapOffOnSameBusArePaired() {
        assertThat(tripMatcher.pair(List.of(on1, off2))).containsExactly(new TapPair(on1, off2));
    }

    @Test
    void tapOnWithoutTapOffIsPairedWithNull() {
        assertThat(tripMatcher.pair(List.of(on1))).containsExactly(TapPair.tapOnOnly(on1));
    }

    @Test
    void tapOffWithoutTapOnIsPairedWithNull() {
        assertThat(tripMatcher.pair(List.of(off2))).containsExactly(TapPair.tapOffOnly(off2));
    }

    @Test
    void newTapOnBeforeAnyTapOffLeavesFirstTapOnWithoutTapOff() {
        Tap on2 = tapOn(2, "22-01-2023 12:00:00", "Stop1", "Bus36");
        Tap off3 = tapOff(3, "22-01-2023 12:20:00", "Stop3", "Bus36");

        assertThat(tripMatcher.pair(List.of(on1, on2, off3)))
                .containsExactly(TapPair.tapOnOnly(on1), new TapPair(on2, off3));
    }

    @Test
    void tapOffOnDifferentBusIsNotPairedAndTapOnStaysOpen() {
        Tap offOtherBus = tapOff(2, "22-01-2023 08:30:00", "Stop2", "Bus36");
        Tap off3 = tapOff(3, "22-01-2023 09:00:00", "Stop2", "Bus37");

        assertThat(tripMatcher.pair(List.of(on1, offOtherBus, off3)))
                .containsExactly(TapPair.tapOffOnly(offOtherBus), new TapPair(on1, off3));
    }

    @Test
    void secondTapOffAfterPairIsUnpaired() {
        Tap off3 = tapOff(3, "22-01-2023 08:11:00", "Stop2", "Bus37");

        assertThat(tripMatcher.pair(List.of(on1, off2, off3)))
                .containsExactly(new TapPair(on1, off2), TapPair.tapOffOnly(off3));
    }

    @Test
    void tapsArePairedInTimeOrderNotFileOrder() {
        Tap on3 = tapOn(3, "22-01-2023 17:00:00", "Stop3", "Bus37");
        Tap off4 = tapOff(4, "22-01-2023 17:30:00", "Stop1", "Bus37");

        assertThat(tripMatcher.pair(List.of(off4, on1, on3, off2)))
                .containsExactly(new TapPair(on1, off2), new TapPair(on3, off4));
    }

    @Test
    void identicalTimesAreOrderedByTapId() {
        Tap on10 = tapOn(10, "22-01-2023 08:00:00", "Stop1", "Bus37");
        Tap off11 = tapOff(11, "22-01-2023 08:00:00", "Stop2", "Bus37");

        assertThat(tripMatcher.pair(List.of(off11, on10))).containsExactly(new TapPair(on10, off11));
    }
}
