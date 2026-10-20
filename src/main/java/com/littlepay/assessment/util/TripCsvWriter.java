package com.littlepay.assessment.util;



import com.littlepay.assessment.entity.Trip;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;


public final class TripCsvWriter {

    private static final String[] TRIPS_COLUMNS = {
            "Started", "Finished", "DurationSecs", "FromStopId", "ToStopId", "ChargeAmount",
            "CompanyId", "BusID", "PAN", "Status"};

    public static final String TRIPS_HEADER = String.join(", ", TRIPS_COLUMNS);

    /** The specification's layout: values separated by ", ", one trip per line. Values are quoted only if needed. */
    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setDelimiter(", ")
            .setRecordSeparator('\n')
            .setHeader(TRIPS_COLUMNS)
            .get();

    private static final DateTimeFormatter UTC_FORMAT = TapCsvParser.DATE_TIME_FORMAT.withZone(ZoneOffset.UTC);

    private TripCsvWriter() {
    }

    public static String tripsCsv(List<Trip> trips) {
        StringWriter csv = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(csv, FORMAT)) {
            for (Trip trip : trips) {
                printer.printRecord(
                        format(trip.getStarted()),
                        format(trip.getFinished()),
                        trip.getDurationSecs(),
                        trip.getFromStopId(),
                        trip.getToStopId(),
                        "$" + trip.getChargeAmount().toPlainString(),
                        trip.getCompanyId(),
                        trip.getBusId(),
                        PanMasker.mask(trip.getPan()),
                        trip.getStatus().name());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);  // writing to a StringWriter doesn't fail
        }
        return csv.toString();
    }

    private static String format(Instant time) {
        return time == null ? null : UTC_FORMAT.format(time);
    }
}
