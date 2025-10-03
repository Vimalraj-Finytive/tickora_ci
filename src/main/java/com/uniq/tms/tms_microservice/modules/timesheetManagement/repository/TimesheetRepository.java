package com.uniq.tms.tms_microservice.modules.timesheetManagement.repository;

import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.UserAttendanceDto;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.projection.TimesheetHistoryProjection;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.projection.TimesheetProjection;
import com.uniq.tms.tms_microservice.modules.userManagement.projections.UserDashboard;
import com.uniq.tms.tms_microservice.modules.userManagement.projections.UserGroupProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimesheetRepository extends JpaRepository<TimesheetEntity, Long> {

    Optional<TimesheetEntity> findByUser_UserIdAndDate(String userId, LocalDate date);

    // Main timesheets per user
    @Query(
            value = "SELECT * FROM fetch_main_timesheets(:startDate, :endDate, :userIds)",
            nativeQuery = true
    )
    List<TimesheetProjection> fetchMainTimesheets(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("userIds") String[] userIds
    );

    // Groups per user
        @Query(value =
                "SELECT * FROM fetch_user_groups(:userIds)",
                nativeQuery = true
        )
    List<UserGroupProjection> fetchUserGroups(@Param("userIds") String[] userIds);

    // Timesheet history per user
    @Query(value = "SELECT * FROM fetch_timesheet_history(:timesheetIds)",
            nativeQuery = true
    )
    List<TimesheetHistoryProjection> fetchTimesheetHistory(@Param("timesheetIds") Long[] timesheetIds);

    List<TimesheetEntity> findActiveTimesheetsByDate(LocalDate today);

    @Query("SELECT new com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.UserAttendanceDto(t.user.userId, t.date, t.status.statusName) " +
            "FROM TimesheetEntity t WHERE t.user.userId IN :userIds AND t.date BETWEEN :from AND :to")
    List<UserAttendanceDto> findAttendanceForUsersInRange(@Param("userIds") List<String> userIds,
                                                          @Param("from") LocalDate from,
                                                          @Param("to") LocalDate to);

    @Query(value = """
    SELECT
        u.user_id AS userId,
        d.log_date AS logDate,
        t.status_id AS statusId
    FROM (
        SELECT CAST(generate_series(:fromDate, :toDate, interval '1 day') AS date) AS log_date
    ) d
    JOIN users u ON u.active = true
    LEFT JOIN timesheet t ON u.user_id = t.user_id AND t.date = d.log_date
    WHERE u.organization_id = :orgId
      AND u.user_id IN (:userIds)
    """, nativeQuery = true)
    List<UserDashboard> getDashboard(
            @Param("userIds") List<String> userIds,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("orgId") String orgId
    );

    @Query("SELECT t FROM TimesheetEntity t WHERE t.status.statusId IN :statusIds AND t.date BETWEEN :startDate AND :endDate")
    List<TimesheetEntity> findUserByStatusIdIn(@Param("statusIds") List<String> statusIds,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT u.userId FROM UserEntity u " +
            "WHERE u.userId NOT IN (" +
            "   SELECT t.user.userId FROM TimesheetEntity t " +
            "   WHERE t.date BETWEEN :startDate AND :endDate" +
            ") " +
            "ORDER BY u.userId ASC")
    List<String> findUserByStatusIdNotIn(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

}
