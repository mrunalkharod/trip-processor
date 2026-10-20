package com.littlepay.assessment.service;


import java.time.LocalDate;

public interface TripService {

    String exportTripsCsv(LocalDate date);
}
