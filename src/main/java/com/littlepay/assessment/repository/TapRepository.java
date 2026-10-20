package com.littlepay.assessment.repository;

import com.littlepay.assessment.entity.Tap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TapRepository extends JpaRepository<Tap, Long> {


    @Query(value = """
            SELECT t.* FROM tap t
            WHERE t.status = 'UNPROCESSED'
              AND (t.company_id, t.pan) IN (
                  SELECT DISTINCT c.company_id, c.pan FROM tap c
                  WHERE c.status = 'UNPROCESSED'
                  ORDER BY c.company_id, c.pan
                  LIMIT :cardLimit)
            ORDER BY t.company_id, t.pan, t.tap_time, t.tap_id
            """, nativeQuery = true)
    List<Tap> findUnprocessedChunk(@Param("cardLimit") int cardLimit);
}
