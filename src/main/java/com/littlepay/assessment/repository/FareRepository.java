package com.littlepay.assessment.repository;

import com.littlepay.assessment.entity.Fare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FareRepository extends JpaRepository<Fare, Long> {

    @Query("SELECT f FROM Fare f WHERE f.bus.companyId = :companyId AND f.bus.busId = :busId")
    List<Fare> findBusStops(@Param("companyId") String companyId, @Param("busId") String busId);
}
