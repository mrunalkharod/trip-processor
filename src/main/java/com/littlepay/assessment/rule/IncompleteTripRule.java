package com.littlepay.assessment.rule;


import com.littlepay.assessment.entity.Tap;
import com.littlepay.assessment.entity.Trip;
import com.littlepay.assessment.service.FareService;
import com.littlepay.assessment.service.MatchResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Tap ON with no tap OFF: an INCOMPLETE trip.
 */
@Component
@Order(3)
public class IncompleteTripRule implements TripRule {

    private final FareService fareService;

    public IncompleteTripRule(FareService fareService) {
        this.fareService = fareService;
    }

    @Override
    public boolean appliesTo(TapPair pair) {
        return pair.hasTapOn() && !pair.hasTapOff();
    }

    @Override
    public void apply(TapPair pair, MatchResult result) {
        Tap on = pair.tapOn();
        result.addTrip(Trip.incomplete(on, fareService.maxFareFrom(on.getCompanyId(), on.getBusId(), on.getStopId())));
    }
}
