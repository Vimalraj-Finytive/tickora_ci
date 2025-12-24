package com.uniq.tms.tms_microservice.modules.timesheetManagement.repository;

import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.LogFrom;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.LogType;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.TimesheetHistoryEntity;
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
    @Query("UPDATE TimesheetHistoryEntity th SET th.logTime = :logTime WHERE th.timesheet.id = :timesheetId AND th.logType = :logType AND th.logFrom = :logFrom")
    void updateTimesheetHistory(@Param("timesheetId") Long timesheetId,
                               @Param("logType") LogType logType,
                               @Param("logTime") LocalTime logTime,
                                @Param("logFrom") LogFrom logFrom);

    @Modifying
    @Query("""
    DELETE FROM TimesheetHistoryEntity h
    WHERE h.timesheet.user.userId = :userId
      AND h.timesheet.date BETWEEN :startDate AND :endDate
      AND h.logFrom = 'SYSTEM_GENERATED'
      AND h.logType IN :logTypes
""")
    void deleteLeaveHistories(
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("logTypes") List<LogType> logTypes
    );

}
