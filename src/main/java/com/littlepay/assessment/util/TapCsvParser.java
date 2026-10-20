package com.littlepay.assessment.util;


import com.littlepay.assessment.entity.Tap;
import com.littlepay.assessment.entity.TapType;
import com.littlepay.assessment.exception.InvalidTapFileException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;


public final class TapCsvParser {

    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static final String ID = "ID";
    public static final String DATE_TIME_UTC = "DateTimeUTC";
    public static final String TAP_TYPE = "TapType";
    public static final String STOP_ID = "StopId";
    public static final String COMPANY_ID = "CompanyId";
    public static final String BUS_ID = "BusID";
    public static final String PAN = "PAN";

    private static final List<String> COLUMNS = List.of(ID, DATE_TIME_UTC, TAP_TYPE, STOP_ID, COMPANY_ID, BUS_ID, PAN);

    static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()                      // read the column names from the first row
            .setSkipHeaderRecord(true)
            .setIgnoreHeaderCase(true)
            .setIgnoreSurroundingSpaces(true) // "1, 22-01-2023 ..." and quoted values after ", "
            .setTrim(true)
            .setIgnoreEmptyLines(true)
            .get();

    private static final Pattern PAN_DIGITS = Pattern.compile("\\d{12,19}");
    private static final int BYTE_ORDER_MARK = '﻿';

    private TapCsvParser() {
    }

    /**
     * Opens taps.csv and reads its header. Iterate the returned parser for the rows;
     * {@link CSVParser#getCurrentLineNumber()} gives the line of the row just read.
     *
     * @throws InvalidTapFileException if a column is missing from the header
     */
    public static CSVParser open(Reader reader) throws IOException {
        BufferedReader buffered = reader instanceof BufferedReader b ? b : new BufferedReader(reader);
        skipByteOrderMark(buffered);
        CSVParser parser = CSVParser.builder().setReader(buffered).setFormat(FORMAT).get();
        List<String> missing = COLUMNS.stream()
                .filter(column -> parser.getHeaderMap().keySet().stream().noneMatch(column::equalsIgnoreCase))
                .toList();
        if (!missing.isEmpty()) {
            parser.close();
            throw new InvalidTapFileException("The header is missing column(s) " + String.join(", ", missing));
        }
        return parser;
    }

    /**
     * Turns one row into a new, UNPROCESSED tap.
     *
     * @throws InvalidTapFileException if the row is invalid
     */
    public static Tap parse(CSVRecord record, long lineNumber) {
        if (!record.isConsistent()) {
            throw new InvalidTapFileException(lineNumber,
                    "expected %d columns but found %d".formatted(COLUMNS.size(), record.size()));
        }
        for (String column : COLUMNS) {
            if (record.get(column).isEmpty()) {
                throw new InvalidTapFileException(lineNumber, column + " is empty");
            }
        }
        String pan = record.get(PAN);
        if (!PAN_DIGITS.matcher(pan).matches()) {
            throw new InvalidTapFileException(lineNumber, "PAN must be 12-19 digits");
        }
        return new Tap(parseId(record.get(ID), lineNumber), parseTime(record.get(DATE_TIME_UTC), lineNumber),
                parseType(record.get(TAP_TYPE), lineNumber), record.get(STOP_ID), record.get(COMPANY_ID),
                record.get(BUS_ID), pan);
    }

    /** The row's values joined back together, for the dead-letter stream. */
    public static String toLine(CSVRecord record) {
        return String.join(", ", record.values());
    }

    private static Long parseId(String value, long lineNumber) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            throw new InvalidTapFileException(lineNumber, "ID is not a number: " + value);
        }
    }

    private static Instant parseTime(String value, long lineNumber) {
        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMAT).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            throw new InvalidTapFileException(lineNumber, "DateTimeUTC must be dd-MM-yyyy HH:mm:ss: " + value);
        }
    }

    private static TapType parseType(String value, long lineNumber) {
        try {
            return TapType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidTapFileException(lineNumber, "TapType must be ON or OFF: " + value);
        }
    }

    /** Commons CSV doesn't skip a UTF-8 byte-order mark, which some editors add. */
    private static void skipByteOrderMark(BufferedReader reader) throws IOException {
        reader.mark(1);
        if (reader.read() != BYTE_ORDER_MARK) {
            reader.reset();
        }
    }
}
