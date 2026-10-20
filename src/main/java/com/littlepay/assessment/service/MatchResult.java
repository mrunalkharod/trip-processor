package com.littlepay.assessment.service;


import com.littlepay.assessment.entity.Tap;
import com.littlepay.assessment.entity.Trip;

import java.util.ArrayList;
import java.util.List;


public class MatchResult {

    private final List<Trip> trips = new ArrayList<>();
    private final List<Tap> orphanTaps = new ArrayList<>();

    public void addTrip(Trip trip) {
        trips.add(trip);
    }

    public void addOrphanTap(Tap tapOff) {
        orphanTaps.add(tapOff);
    }

    public List<Trip> trips() {
        return trips;
    }

    public List<Tap> orphanTaps() {
        return orphanTaps;
    }
}
