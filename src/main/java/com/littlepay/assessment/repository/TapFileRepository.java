package com.littlepay.assessment.repository;

import com.littlepay.assessment.entity.TapFile;
import com.littlepay.assessment.entity.TapFileStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TapFileRepository extends JpaRepository<TapFile, Long> {
    List<TapFile> findByStatusInOrderByIdAsc(Collection<TapFileStatus> statuses);
}
