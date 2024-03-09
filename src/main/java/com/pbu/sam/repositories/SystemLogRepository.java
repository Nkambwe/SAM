package com.pbu.sam.repositories;

import com.pbu.sam.entities.SystemLog;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {

    @Transactional
    @Query("SELECT sl FROM SystemLog sl WHERE sl.logTime >= :startDate AND sl.logTime <= :endDate")
    List<SystemLog> getLogs(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}

