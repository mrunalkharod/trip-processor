package com.littlepay.assessment.rule;


import com.littlepay.assessment.entity.Tap;
import com.littlepay.assessment.service.MatchResult;
import com.littlepay.assessment.util.PanMasker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Tap OFF with no tap ON: an orphan tap.
 */
@Component
@Order(4)
public class OrphanTapRule implements TripRule {

    private static final Logger log = LoggerFactory.getLogger(OrphanTapRule.class);

    @Override
    public boolean appliesTo(TapPair pair) {
        return pair.hasTapOff() && !pair.hasTapOn();
    }

    @Override
    public void apply(TapPair pair, MatchResult result) {
        Tap tapOff = pair.tapOff();
        log.warn("Orphan tap OFF: tapId={}, time={}, stop={}, company={}, bus={}, pan={}", tapOff.getId(),
                tapOff.getTapTime(), tapOff.getStopId(), tapOff.getCompanyId(), tapOff.getBusId(),
                PanMasker.mask(tapOff.getPan()));
        result.addOrphanTap(tapOff);
    }
}
