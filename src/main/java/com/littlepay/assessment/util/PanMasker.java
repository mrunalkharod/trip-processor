package com.littlepay.assessment.util;


public final class PanMasker {

    private static final int KEEP_FIRST = 6;
    private static final int KEEP_LAST = 4;

    private PanMasker() {
    }

    public static String mask(String pan) {
        if (pan == null || pan.isEmpty()) {
            return pan;
        }
        int length = pan.length();
        if (length <= KEEP_FIRST + KEEP_LAST) {
            int visible = Math.min(KEEP_LAST, length);
            return "*".repeat(length - visible) + pan.substring(length - visible);
        }
        return pan.substring(0, KEEP_FIRST)
                + "*".repeat(length - KEEP_FIRST - KEEP_LAST)
                + pan.substring(length - KEEP_LAST);
    }
}
