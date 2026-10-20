package com.littlepay.assessment.repository;

import com.littlepay.assessment.entity.Tap;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;


@Repository
public class TapBatchRepository {

    static final String INSERT_TAP = """
            INSERT INTO tap (tap_id, tap_time, tap_type, stop_id, company_id, bus_id, pan, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (tap_id) DO NOTHING
            """;

    static final String MARK_PROCESSED = "UPDATE tap SET status = 'PROCESSED' WHERE tap_id = ANY(?)";

    private final JdbcTemplate jdbcTemplate;

    public TapBatchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** @return the number of taps inserted  */
    public int insertNew(List<Tap> taps) {
        if (taps.isEmpty()) {
            return 0;
        }
        int[][] counts = jdbcTemplate.batchUpdate(INSERT_TAP, taps, taps.size(), (ps, tap) -> {
            ps.setLong(1, tap.getId());
            ps.setObject(2, OffsetDateTime.ofInstant(tap.getTapTime(), ZoneOffset.UTC));
            ps.setString(3, tap.getTapType().name());
            ps.setString(4, tap.getStopId());
            ps.setString(5, tap.getCompanyId());
            ps.setString(6, tap.getBusId());
            ps.setString(7, tap.getPan());
            ps.setString(8, tap.getStatus().name());
        });
        int inserted = 0;
        for (int[] batch : counts) {
            for (int count : batch) {
                inserted += Math.max(count, 0);
            }
        }
        return inserted;
    }


    public int markProcessed(Collection<Long> tapIds) {
        if (tapIds.isEmpty()) {
            return 0;
        }
        return jdbcTemplate.update(MARK_PROCESSED,
                ps -> ps.setArray(1, ps.getConnection().createArrayOf("bigint", tapIds.toArray())));
    }
}
