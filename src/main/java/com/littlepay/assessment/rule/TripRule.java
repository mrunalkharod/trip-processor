package com.littlepay.assessment.rule;


import com.littlepay.assessment.service.MatchResult;

public interface TripRule {

    /** Whether this rule handles the pair. */
    boolean appliesTo(TapPair pair);

    /** Adds the resulting trip or orphan tap to {@code result}. */
    void apply(TapPair pair, MatchResult result);
}
