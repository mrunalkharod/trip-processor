package com.littlepay.assessment.service;


import com.littlepay.assessment.entity.Fare;
import com.littlepay.assessment.exception.FareNotFoundException;
import com.littlepay.assessment.repository.FareRepository;
import com.littlepay.assessment.util.FareCalculator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class FareServiceImpl implements FareService {

    private final FareRepository fareRepository;
    private final BigDecimal baseFare;

    public FareServiceImpl(FareRepository fareRepository, @Value("${app.fare.base-fare}") BigDecimal baseFare) {
        this.fareRepository = fareRepository;
        this.baseFare = baseFare;
    }

    @Override
    public BigDecimal fareBetween(String companyId, String busId, String fromStopId, String toStopId) {
        Map<String, BigDecimal> stopValues = stopValuesOf(companyId, busId);
        return FareCalculator.fare(baseFare,
                distanceValue(stopValues, companyId, busId, fromStopId),
                distanceValue(stopValues, companyId, busId, toStopId));
    }

    @Override
    public BigDecimal maxFareFrom(String companyId, String busId, String stopId) {
        Map<String, BigDecimal> stopValues = stopValuesOf(companyId, busId);
        return FareCalculator.maxFare(baseFare,
                distanceValue(stopValues, companyId, busId, stopId), stopValues.values());
    }

    /** The bus's stops and their distance values. */
    private Map<String, BigDecimal> stopValuesOf(String companyId, String busId) {
        List<Fare> stops = fareRepository.findBusStops(companyId, busId);
        if (stops.isEmpty()) {
            throw new FareNotFoundException("No stops for bus %s of %s".formatted(busId, companyId));
        }
        return stops.stream().collect(Collectors.toMap(Fare::getStopId, Fare::getDistanceValue));
    }

    private static BigDecimal distanceValue(Map<String, BigDecimal> stopValues,
                                            String companyId, String busId, String stopId) {
        BigDecimal value = stopValues.get(stopId);
        if (value == null) {
            throw new FareNotFoundException("Stop %s is not served by bus %s of %s".formatted(stopId, busId, companyId));
        }
        return value;
    }
}
