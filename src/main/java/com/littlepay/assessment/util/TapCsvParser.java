package com.littlepay.assessment.util;


import com.littlepay.assessment.entity.Tap;
import com.littlepay.assessment.entity.TapType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Pattern;

public final class TapCsvParser {

    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private static final Pattern PAN = Pattern.compile("\\d{12,19}");
    private static final int COLUMNS = 7;

    private TapCsvParser() {
    }

    /** Whether a line is the header row ({@code ID, DateTimeUTC, ...}). */
    public static boolean isHeader(String line) {
        return stripByteOrderMark(line).split(",", 2)[0].strip().equalsIgnoreCase("ID");
    }


    public static Tap parseLine(String line, int lineNumber) {
        String[] fields = stripByteOrderMark(line).split(",", -1);
        if (fields.length != COLUMNS) {
          // throw exception
        }
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].strip();
            if (fields[i].isEmpty()) {
              // throw exception
            }
        }
        if (!PAN.matcher(fields[6]).matches()) {
          // throw exception
        }
        return new Tap(parseId(fields[0], lineNumber), parseTime(fields[1], lineNumber),
                parseType(fields[2], lineNumber), fields[3], fields[4], fields[5], fields[6]);
    }

    private static Long parseId(String value, int lineNumber) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
           // throw execption
        }
    }

    private static Instant parseTime(String value, int lineNumber) {
        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMAT).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            // throw exception
        }
    }

    private static TapType parseType(String value, int lineNumber) {
        try {
            return TapType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            // throw exception
        }
    }

    private static String stripByteOrderMark(String line) {
        return line.startsWith("﻿") ? line.substring(1) : line;
    }
}
