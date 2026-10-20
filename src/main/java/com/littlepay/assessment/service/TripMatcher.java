package com.littlepay.assessment.service;


import com.littlepay.assessment.entity.Tap;
import com.littlepay.assessment.rule.TapPair;
import com.littlepay.assessment.rule.TripRule;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Component
public class TripMatcher {

    private static final Comparator<Tap> TIMELINE_ORDER =
            Comparator.comparing(Tap::getTapTime).thenComparing(Tap::getId);

    private final List<TripRule> rules;

    public TripMatcher(List<TripRule> rules) {
        this.rules = rules;
    }

    public MatchResult match(List<Tap> taps) {
        Map<String, List<Tap>> tapsByCard = new LinkedHashMap<>();
        for (Tap tap : taps) {
            tapsByCard.computeIfAbsent(tap.getCompanyId() + "|" + tap.getPan(), card -> new ArrayList<>()).add(tap);
        }

        MatchResult result = new MatchResult();
        for (List<Tap> cardTaps : tapsByCard.values()) {
            for (TapPair pair : pair(cardTaps)) {
                ruleFor(pair).apply(pair, result);
            }
        }
        return result;
    }


    List<TapPair> pair(List<Tap> cardTaps) {
        List<TapPair> pairs = new ArrayList<>();
        Tap openTapOn = null;

        for (Tap tap : cardTaps.stream().sorted(TIMELINE_ORDER).toList()) {
            if (tap.isOn()) {
                if (openTapOn != null) {
                    pairs.add(TapPair.tapOnOnly(openTapOn));
                }
                openTapOn = tap;
            } else if (openTapOn != null && openTapOn.isSameBusAs(tap)) {
                pairs.add(new TapPair(openTapOn, tap));
                openTapOn = null;
            } else {
                pairs.add(TapPair.tapOffOnly(tap));
            }
        }
        if (openTapOn != null) {
            pairs.add(TapPair.tapOnOnly(openTapOn));
        }
        return pairs;
    }

    private TripRule ruleFor(TapPair pair) {
        return rules.stream()
                .filter(rule -> rule.appliesTo(pair))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No rule handles " + pair));
    }
}
