package com.littlepay.assessment.repository;

import com.littlepay.assessment.entity.Trip;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;


@Repository
public class TripBatchRepository {

    static final String INSERT_TRIP = """
            INSERT INTO trip (started, finished, duration_secs, from_stop_id, to_stop_id, charge_amount,
                              company_id, bus_id, pan, status, on_tap_id, off_tap_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public TripBatchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertTrips(List<Trip> trips) {
        if (trips.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate(INSERT_TRIP, trips, trips.size(), (ps, trip) -> {
            ps.setObject(1, utc(trip.getStarted()));
            ps.setObject(2, utc(trip.getFinished()), Types.TIMESTAMP_WITH_TIMEZONE);
            ps.setObject(3, trip.getDurationSecs(), Types.BIGINT);
            ps.setString(4, trip.getFromStopId());
            ps.setString(5, trip.getToStopId());
            ps.setBigDecimal(6, trip.getChargeAmount());
            ps.setString(7, trip.getCompanyId());
            ps.setString(8, trip.getBusId());
            ps.setString(9, trip.getPan());
            ps.setString(10, trip.getStatus().name());
            ps.setLong(11, trip.getOnTapId());
            ps.setObject(12, trip.getOffTapId(), Types.BIGINT);
        });
    }

    private static OffsetDateTime utc(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
