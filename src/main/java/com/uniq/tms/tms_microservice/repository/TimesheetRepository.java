package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.dto.UserAttendanceDto;
import com.uniq.tms.tms_microservice.dto.UserDashboard;
import com.uniq.tms.tms_microservice.entity.TimesheetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimesheetRepository extends JpaRepository<TimesheetEntity, Long> {

    Optional<TimesheetEntity> findByUserIdAndDate(Long userId, LocalDate date);

    @Query(value = """
    WITH SelectedUsers AS (
        SELECT * FROM users WHERE active = TRUE AND (:userIds IS NULL OR user_id = ANY(:userIds))
    ),
    UserGroups AS (
    SELECT
    ug.user_id,
    STRING_AGG(g.group_name, ', ') AS group_name
    FROM user_group ug
    JOIN group_table g ON ug.group_id = g.group_id
    GROUP BY ug.user_id
    ),
    UserDateMatrix AS (
        SELECT
            gs.work_date,
            u.user_id,
            u.user_name,
            u.role_id,
            u.mobile_number
        FROM SelectedUsers u
        CROSS JOIN LATERAL (
            SELECT generate_series(
            GREATEST(COALESCE(:startDate, u.date_of_joining), u.date_of_joining),
            COALESCE(:endDate, CURRENT_DATE),
            INTERVAL '1 day'
            ) AS work_date
        ) gs
    )
    SELECT
        udm.work_date,
        udm.user_id,
        udm.user_name,
        r.name AS role,
        udm.mobile_number,
        ug.group_name,
        t.id AS timesheet_id,
        t.first_clock_in::TIME AS first_clock_in,
        t.last_clock_out::TIME AS last_clock_out,
        COALESCE(CAST(t.tracked_hours AS TEXT), '00:00:00') AS tracked_hours,
        COALESCE(CAST(t.regular_hours AS TEXT), '00:00:00') AS regular_hours,
        t.status_id,
   
        CASE
            WHEN trim(to_char(udm.work_date, 'Day')) ILIKE trim(ws.rest_day) THEN 'Holiday'
            ELSE 'Working Day'
        END AS day_type,
        CASE
            WHEN t.date IS NULL THEN
                CASE
                    WHEN trim(to_char(udm.work_date, 'Day')) ILIKE trim(ws.rest_day) THEN 'Holiday'
                    ELSE 'Time Off'
                END
            ELSE
                CASE
                    WHEN trim(to_char(udm.work_date, 'Day')) ILIKE trim(ws.rest_day) THEN 'Extra Worked Day'
                    ELSE 'Working Day'
                END
        END AS user_day_type,
        CASE
            WHEN trim(to_char(udm.work_date, 'FMDay')) ILIKE trim(ws.rest_day) THEN
                CASE
                    WHEN t.date IS NOT NULL THEN 'Extra Worked Day'
                    ELSE 'Holiday'
                END
            WHEN t.date IS NULL THEN 'Time Off'
            WHEN t.first_clock_in IS NOT NULL AND t.last_clock_out IS NULL THEN 'Present'
            WHEN t.first_clock_in IS NOT NULL AND t.last_clock_out IS NOT NULL THEN
                CASE
                    WHEN EXISTS (
                    SELECT 1
                    FROM timesheet_history th2
                    WHERE th2.timesheet_id = t.id
                    AND th2.log_type = 'CLOCK_OUT'
                    AND th2.log_from = 'SYSTEM_GENERATED'
                    ) THEN 'Failed Clock Out'
                    WHEN (EXTRACT(EPOCH FROM (t.last_clock_out - t.first_clock_in)) / 3600.0) >=
                         (EXTRACT(EPOCH FROM (ws.end_time - ws.start_time)) / 3600.0)
                    THEN 'Sufficient Hours'
                    ELSE 'Less Worked Hours'
                END
            ELSE 'Time Off'
        END AS work_status,
        th.id AS history_id,
        th.log_time,
        th.log_type,
        th.location_id,
        th.log_from,
        th.logged_timestamp
    FROM UserDateMatrix udm
    LEFT JOIN timesheet t ON t.user_id = udm.user_id AND t.date = udm.work_date
    LEFT JOIN role r ON udm.role_id = r.role_id
    LEFT JOIN work_schedule ws ON ws.is_active = TRUE AND ws.organization_id = :orgId
    LEFT JOIN timesheet_history th ON t.id = th.timesheet_id
    LEFT JOIN UserGroups ug ON udm.user_id = ug.user_id
    ORDER BY udm.user_id, udm.work_date, th.logged_timestamp
    """, nativeQuery = true)
    List<Object[]> fetchTimesheetsWithHistory(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate,
                                              @Param("userIds") Long[] userIds,
                                              @Param("orgId") Long orgId);

    List<TimesheetEntity> findActiveTimesheetsByDate(LocalDate today);

    @Query("SELECT new com.uniq.tms.tms_microservice.dto.UserAttendanceDto(t.userId, t.date, t.statusId) " +
            "FROM TimesheetEntity t WHERE t.userId IN :userIds AND t.date BETWEEN :from AND :to")
    List<UserAttendanceDto> findAttendanceForUsersInRange(@Param("userIds") List<Long> userIds,
                                                          @Param("from") LocalDate from,
                                                          @Param("to") LocalDate to);

    @Query(value = """
    WITH SelectedUsers AS (
        SELECT * FROM users
        WHERE active = TRUE
          AND (ARRAY[:userIds] IS NULL OR user_id = ANY(ARRAY[:userIds]))
    ),
    UserGroups AS (
        SELECT
            ug.user_id,
            STRING_AGG(g.group_name, ', ') AS group_name
        FROM user_group ug
        JOIN group_table g ON ug.group_id = g.group_id
        GROUP BY ug.user_id
    ),
    UserDateMatrix AS (
        SELECT
            gs.work_date,
            u.user_id,
            u.user_name,
            u.role_id,
            u.mobile_number
        FROM SelectedUsers u
        CROSS JOIN LATERAL (
            SELECT generate_series(
                GREATEST(COALESCE(:startDate, u.date_of_joining), u.date_of_joining),
                COALESCE(:endDate, CURRENT_DATE),
                INTERVAL '1 day'
            ) AS work_date
        ) gs
    )
    SELECT
        udm.work_date,
        udm.user_id,
        udm.user_name,
        r.name AS role,
        udm.mobile_number,
        ug.group_name,
        t.id AS timesheet_id,
        t.first_clock_in::TIME AS first_clock_in,
        t.last_clock_out::TIME AS last_clock_out,
        COALESCE(CAST(t.tracked_hours AS TEXT), '00:00:00') AS tracked_hours,
        COALESCE(CAST(t.regular_hours AS TEXT), '00:00:00') AS regular_hours,
        t.status_id,
        CASE
            WHEN trim(to_char(udm.work_date, 'Day')) ILIKE trim(ws.rest_day) THEN 'Holiday'
            ELSE 'Working Day'
        END AS day_type,
        CASE
            WHEN t.date IS NULL THEN
                CASE
                    WHEN trim(to_char(udm.work_date, 'Day')) ILIKE trim(ws.rest_day) THEN 'Holiday'
                    ELSE 'Time Off'
                END
            ELSE
                CASE
                    WHEN trim(to_char(udm.work_date, 'Day')) ILIKE trim(ws.rest_day) THEN 'Extra Worked Day'
                    ELSE 'Working Day'
                END
        END AS user_day_type,
        CASE
            WHEN trim(to_char(udm.work_date, 'FMDay')) ILIKE trim(ws.rest_day) THEN
                CASE
                    WHEN t.date IS NOT NULL THEN 'Extra Worked Day'
                    ELSE 'Holiday'
                END
            WHEN t.date IS NULL THEN 'Time Off'
            WHEN t.first_clock_in IS NOT NULL AND t.last_clock_out IS NULL THEN 'Present'
            WHEN t.first_clock_in IS NOT NULL AND t.last_clock_out IS NOT NULL THEN
                CASE
                    WHEN EXISTS (
                        SELECT 1
                        FROM timesheet_history th2
                        WHERE th2.timesheet_id = t.id
                          AND th2.log_type = 'CLOCK_OUT'
                          AND th2.log_from = 'SYSTEM_GENERATED'
                    ) THEN 'Failed Clock Out'
                    WHEN (EXTRACT(EPOCH FROM (t.last_clock_out - t.first_clock_in)) / 3600.0) >=
                         (EXTRACT(EPOCH FROM (ws.end_time - ws.start_time)) / 3600.0)
                    THEN 'Sufficient Hours'
                    ELSE 'Less Worked Hours'
                END
            ELSE 'Time Off'
        END AS work_status,
        th.id AS history_id,
        th.log_time,
        th.log_type,
        th.location_id,
        th.log_from,
        th.logged_timestamp
    FROM UserDateMatrix udm
    LEFT JOIN timesheet t ON t.user_id = udm.user_id AND t.date = udm.work_date
    LEFT JOIN role r ON udm.role_id = r.role_id
    LEFT JOIN work_schedule ws ON ws.is_active = TRUE
    LEFT JOIN timesheet_history th ON t.id = th.timesheet_id
    LEFT JOIN UserGroups ug ON udm.user_id = ug.user_id
    ORDER BY udm.user_id, udm.work_date, th.logged_timestamp
""", nativeQuery = true)
    List<Object[]> fetchUserTimesheetsWithHistory(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("userIds") Long[] userIds
    );

    @Query(value = """
    SELECT
        u.user_id AS userId,
        d.log_date AS logDate,
        t.status_id AS statusId
    FROM (
        SELECT generate_series(:fromDate, :toDate, interval '1 day')::date AS log_date
    ) d
    JOIN users u ON u.active = true
    LEFT JOIN timesheet t ON u.user_id = t.user_id AND t.date = d.log_date
    WHERE u.organization_id = :orgId
    """, nativeQuery = true)
    List<UserDashboard> getDashboard(
            @Param("userIds") List<Long> userIds,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("orgId") Long orgId
    );
}
