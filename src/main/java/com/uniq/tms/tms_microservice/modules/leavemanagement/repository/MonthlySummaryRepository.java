package com.uniq.tms.tms_microservice.modules.leavemanagement.repository;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.MonthlySummaryEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlySummaryRepository extends JpaRepository<MonthlySummaryEntity,Long> {

    @Query("""
    SELECT m
    FROM MonthlySummaryEntity m
    WHERE m.month = :month AND m.year = :year
    """)
    List<MonthlySummaryEntity> findByMonthAndYear(
            @Param("month") Integer month,
            @Param("year") Integer year
    );

}
