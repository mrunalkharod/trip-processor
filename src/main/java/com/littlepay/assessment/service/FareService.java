package com.littlepay.assessment.service;


import java.math.BigDecimal;

public interface FareService {

    BigDecimal fareBetween(String companyId, String busId, String fromStopId, String toStopId);

    BigDecimal maxFareFrom(String companyId, String busId, String stopId);
}
