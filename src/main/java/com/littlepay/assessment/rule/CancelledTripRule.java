package com.littlepay.assessment.rule;


import com.littlepay.assessment.entity.Trip;
import com.littlepay.assessment.service.MatchResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Tap ON and tap OFF at the same stop.
 */
@Component
@Order(2)
public class CancelledTripRule implements TripRule {

    @Override
    public boolean appliesTo(TapPair pair) {
        return pair.isComplete() && pair.tapOn().isSameStopAs(pair.tapOff());
    }

    @Override
    public void apply(TapPair pair, MatchResult result) {
        result.addTrip(Trip.cancelled(pair.tapOn(), pair.tapOff()));
    }
}
