package com.littlepay.assessment.rule;


import com.littlepay.assessment.entity.Tap;
import com.littlepay.assessment.entity.Trip;
import com.littlepay.assessment.service.FareService;
import com.littlepay.assessment.service.MatchResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Tap ON and tap OFF at different stops: a COMPLETED trip.
 */
@Component
@Order(1)
public class CompletedTripRule implements TripRule {

    private final FareService fareService;

    public CompletedTripRule(FareService fareService) {
        this.fareService = fareService;
    }

    @Override
    public boolean appliesTo(TapPair pair) {
        return pair.isComplete() && !pair.tapOn().isSameStopAs(pair.tapOff());
    }

    @Override
    public void apply(TapPair pair, MatchResult result) {
        Tap on = pair.tapOn();
        result.addTrip(Trip.completed(on, pair.tapOff(),
                fareService.fareBetween(on.getCompanyId(), on.getBusId(), on.getStopId(), pair.tapOff().getStopId())));
    }
}
