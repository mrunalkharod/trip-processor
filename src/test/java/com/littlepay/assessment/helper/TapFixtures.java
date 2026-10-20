package com.littlepay.assessment.helper;


import com.littlepay.assessment.entity.Tap;
import com.littlepay.assessment.entity.TapType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Builds taps for tests. Times use the taps.csv format and are UTC.
 */
public final class TapFixtures {

    public static final String PAN = "5500005555555559";
    public static final String OTHER_PAN = "4111111111111111";
    public static final String COMPANY = "Company1";

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private TapFixtures() {
    }

    public static Tap tapOn(long id, String time, String stopId, String busId) {
        return tap(id, time, TapType.ON, stopId, busId, COMPANY, PAN);
    }

    public static Tap tapOff(long id, String time, String stopId, String busId) {
        return tap(id, time, TapType.OFF, stopId, busId, COMPANY, PAN);
    }

    public static Tap tap(long id, String time, TapType type, String stopId, String busId, String companyId,
                          String pan) {
        return new Tap(id, utc(time), type, stopId, companyId, busId, pan);
    }

    public static Instant utc(String time) {
        return LocalDateTime.parse(time, FORMAT).toInstant(ZoneOffset.UTC);
    }
}

