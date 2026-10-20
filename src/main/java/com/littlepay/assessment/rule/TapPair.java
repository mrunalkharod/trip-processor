package com.littlepay.assessment.rule;


import com.littlepay.assessment.entity.Tap;

/**
 * A tap ON and the tap OFF matched to it. Either can be null.
 */
public record TapPair(Tap tapOn, Tap tapOff) {

    public TapPair {
        if (tapOn == null && tapOff == null) {
            throw new IllegalArgumentException("A pair needs at least one tap");
        }
    }

    public static TapPair tapOnOnly(Tap tapOn) {
        return new TapPair(tapOn, null);
    }

    public static TapPair tapOffOnly(Tap tapOff) {
        return new TapPair(null, tapOff);
    }

    public boolean hasTapOn() {
        return tapOn != null;
    }

    public boolean hasTapOff() {
        return tapOff != null;
    }

    public boolean isComplete() {
        return hasTapOn() && hasTapOff();
    }
}
