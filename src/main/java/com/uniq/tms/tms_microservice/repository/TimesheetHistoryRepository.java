package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.entity.TimesheetHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimesheetHistoryRepository extends JpaRepository<TimesheetHistoryEntity, Long> {

    @Query("""
    SELECT t FROM TimesheetEntity t
    WHERE t.userId IN :userIds
      AND t.date = :logDate
""")
    List<TimesheetEntity> findLatestLogByTimesheet(@Param("userIds") List<Long> userIds, @Param("logDate") LocalDate date);
}
