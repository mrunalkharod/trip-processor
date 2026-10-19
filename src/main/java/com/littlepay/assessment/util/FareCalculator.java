package com.littlepay.assessment.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;


public final class FareCalculator {

    private FareCalculator() {
    }

    public static BigDecimal fare(BigDecimal baseFare, BigDecimal fromValue, BigDecimal toValue) {
        return fromValue.subtract(toValue).abs().add(baseFare).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal maxFare(BigDecimal baseFare, BigDecimal fromValue, Collection<BigDecimal> stopValues) {
        BigDecimal farthest = stopValues.stream()
                .map(value -> fromValue.subtract(value).abs())
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        return farthest.add(baseFare).setScale(2, RoundingMode.HALF_UP);
    }
}
