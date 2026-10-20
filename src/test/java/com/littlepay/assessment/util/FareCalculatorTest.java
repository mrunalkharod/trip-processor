package com.littlepay.assessment.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class FareCalculatorTest {

    private static final BigDecimal BASE_FARE = new BigDecimal("1.45");
    private static final BigDecimal STOP1 = new BigDecimal("0.00");
    private static final BigDecimal STOP2 = new BigDecimal("1.80");
    private static final BigDecimal STOP3 = new BigDecimal("5.85");
    private static final List<BigDecimal> BUS37_STOPS = List.of(STOP1, STOP2, STOP3);

    @ParameterizedTest(name = "{0}")
    @MethodSource("specificationFares")
    void formulaGivesSpecificationFares(String scenario, BigDecimal from, BigDecimal to, String expected) {
        assertThat(FareCalculator.fare(BASE_FARE, from, to)).isEqualByComparingTo(expected);
    }

    static Stream<Arguments> specificationFares() {
        return Stream.of(
                arguments("Stop1 to Stop2 is $3.25", STOP1, STOP2, "3.25"),
                arguments("Stop2 to Stop1 is $3.25", STOP2, STOP1, "3.25"),
                arguments("Stop2 to Stop3 is $5.50", STOP2, STOP3, "5.50"),
                arguments("Stop3 to Stop2 is $5.50", STOP3, STOP2, "5.50"),
                arguments("Stop1 to Stop3 is $7.30", STOP1, STOP3, "7.30"),
                arguments("Stop3 to Stop1 is $7.30", STOP3, STOP1, "7.30"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("maxFares")
    void maxFareIsFareToFarthestStop(String scenario, BigDecimal from, String expected) {
        assertThat(FareCalculator.maxFare(BASE_FARE, from, BUS37_STOPS)).isEqualByComparingTo(expected);
    }

    static Stream<Arguments> maxFares() {
        return Stream.of(
                arguments("From Stop1 the farthest is Stop3: $7.30", STOP1, "7.30"),
                arguments("From Stop2 the farthest is Stop3: $5.50 (spec example)", STOP2, "5.50"),
                arguments("From Stop3 the farthest is Stop1: $7.30", STOP3, "7.30"));
    }

    @Test
    void faresHaveTwoDecimalPlaces() {
        assertThat(FareCalculator.fare(BASE_FARE, STOP1, STOP2).scale()).isEqualTo(2);
        assertThat(FareCalculator.maxFare(BASE_FARE, STOP2, BUS37_STOPS).scale()).isEqualTo(2);
    }
}
