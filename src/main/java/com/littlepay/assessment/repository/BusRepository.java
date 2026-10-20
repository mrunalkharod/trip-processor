package com.littlepay.assessment.repository;

import com.littlepay.assessment.entity.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long> {

    Optional<Bus> findByCompanyIdAndBusId(String companyId, String busId);
}
