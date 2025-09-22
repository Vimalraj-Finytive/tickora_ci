package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.enums.LogType;
import com.uniq.tms.tms_microservice.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.entity.TimesheetHistoryEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TimesheetHistoryRepository extends JpaRepository<TimesheetHistoryEntity, Long> {

    @Query("""
    SELECT t FROM TimesheetEntity t
    WHERE t.user.userId IN :userIds
      AND t.date = :logDate
""")
    List<TimesheetEntity> findLatestLogByTimesheet(@Param("userIds") List<String> userIds, @Param("logDate") LocalDate date);

    @Modifying
    @Transactional
    @Query("UPDATE TimesheetHistoryEntity th SET th.logTime = :logTime WHERE th.timesheet.id = :timesheetId AND th.logType = :logType")
    void updateTimesheetHistory(@Param("timesheetId") Long timesheetId,
                               @Param("logType") LogType logType,
                               @Param("logTime") LocalTime logTime);

    @Query("""
    SELECT th.logType
    FROM TimesheetHistoryEntity th
    JOIN th.timesheet t
    WHERE t.user.userId = :userId
      AND t.date = CURRENT_DATE
    ORDER BY th.loggedTimestamp DESC
""")
    List<LogType> findLatestLogTypesByUserIdForToday(@Param("userId") String userId);

}
