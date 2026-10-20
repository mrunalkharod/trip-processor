package com.littlepay.assessment.repository;

import com.littlepay.assessment.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    @Query("SELECT t FROM Trip t WHERE t.started >= :from AND t.started < :to ORDER BY t.started, t.id")
    List<Trip> findTripsStartedBetween(@Param("from") Instant from, @Param("to") Instant to);
}
