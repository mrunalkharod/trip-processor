package com.littlepay.assessment.util;



import com.littlepay.assessment.entity.Trip;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;


public final class TripCsvWriter {

    public static final String TRIPS_HEADER =
            "Started, Finished, DurationSecs, FromStopId, ToStopId, ChargeAmount, CompanyId, BusID, PAN, Status";

    private static final DateTimeFormatter UTC_FORMAT = TapCsvParser.DATE_TIME_FORMAT.withZone(ZoneOffset.UTC);
    private static final String SEPARATOR = ", ";

    private TripCsvWriter() {
    }

    public static String tripsCsv(List<Trip> trips) {
        StringBuilder csv = new StringBuilder(TRIPS_HEADER).append('\n');
        for (Trip trip : trips) {
            csv.append(String.join(SEPARATOR,
                    format(trip.getStarted()),
                    format(trip.getFinished()),
                    trip.getDurationSecs() == null ? "" : trip.getDurationSecs().toString(),
                    trip.getFromStopId(),
                    trip.getToStopId() == null ? "" : trip.getToStopId(),
                    "$" + trip.getChargeAmount().toPlainString(),
                    trip.getCompanyId(),
                    trip.getBusId(),
                    PanMasker.mask(trip.getPan()),
                    trip.getStatus().name())).append('\n');
        }
        return csv.toString();
    }

    private static String format(Instant time) {
        return time == null ? "" : UTC_FORMAT.format(time);
    }
}
