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

    Optional<TimesheetEntity> findByUserIdAndDate(String userId, LocalDate date);

        @Query(value = """
    WITH SelectedUsers AS (
        SELECT *
        FROM users
        WHERE active = TRUE
          AND (:userIds IS NULL OR user_id = ANY(CAST(:userIds AS VARCHAR[])))
    ),
    
    UserGroups AS (
        SELECT
            ug.user_id,
            STRING_AGG(g.group_name, ', ') AS group_name
        FROM user_group ug
        JOIN group_table g ON ug.group_id = g.group_id
        GROUP BY ug.user_id
    ),
    
    DayNumbers AS (
        SELECT 'Sunday' AS day_name, 0 AS day_num UNION ALL
        SELECT 'Monday', 1 UNION ALL
        SELECT 'Tuesday', 2 UNION ALL
        SELECT 'Wednesday', 3 UNION ALL
        SELECT 'Thursday', 4 UNION ALL
        SELECT 'Friday', 5 UNION ALL
        SELECT 'Saturday', 6
    ),
    
    UserDateMatrix AS (
        SELECT
            gs.work_date,
            TO_CHAR(gs.work_date, 'FMDay') AS day_name,
            EXTRACT(DOW FROM gs.work_date)::INT AS day_num,
            u.user_id,
            u.user_name,
            u.role_id,
            u.mobile_number,
            u.work_schedule_id
        FROM SelectedUsers u
        CROSS JOIN LATERAL (
            SELECT generate_series(
                GREATEST(COALESCE(:startDate, u.date_of_joining), u.date_of_joining),
                COALESCE(:endDate, CURRENT_DATE),
                INTERVAL '1 day'
            ) AS work_date
        ) gs
    ),
    
    WorkScheduleDuration AS (
        SELECT
            ws.work_schedule_id,
            wst.type_id,
            wst.type,
            CASE
                WHEN wst.type = 'FIXED' THEN (
                    SELECT SUM(EXTRACT(EPOCH FROM (fws.end_time - fws.start_time))) / COUNT(*)
                    FROM fixed_work_schedule fws
                    WHERE fws.work_schedule_id = ws.work_schedule_id
                )
                WHEN wst.type = 'FLEXIBLE' THEN (
                    SELECT SUM(flws.duration * 3600) / COUNT(*)
                    FROM flexible_work_schedule flws
                    WHERE flws.work_schedule_id = ws.work_schedule_id
                )
                ELSE NULL
            END AS expected_seconds
        FROM work_schedule ws
        JOIN work_schedule_type wst ON ws.work_schedule_type = wst.type_id
    ),
    
    WorkDayType AS (
        SELECT
            udm.user_id,
            udm.work_date,
            CASE
                WHEN wst.type = 'FIXED' AND EXISTS (
                    SELECT 1 FROM fixed_work_schedule fws
                    WHERE fws.work_schedule_id = ws.work_schedule_id
                      AND fws.day ILIKE udm.day_name
                ) THEN TRUE
                WHEN wst.type = 'FLEXIBLE' AND EXISTS (
                    SELECT 1 FROM flexible_work_schedule flws
                    WHERE flws.work_schedule_id = ws.work_schedule_id
                      AND flws.day ILIKE udm.day_name
                ) THEN TRUE
                WHEN wst.type = 'WEEKLY_EXCEPTION' AND EXISTS (
                    SELECT 1
                    FROM weekly_work_schedule wws
                    JOIN DayNumbers sd ON wws.start_day = sd.day_name
                    JOIN DayNumbers ed ON wws.end_day = ed.day_name
                    WHERE wws.work_schedule_id = ws.work_schedule_id
                      AND (
                          (sd.day_num <= ed.day_num AND udm.day_num BETWEEN sd.day_num AND ed.day_num)
                          OR
                          (sd.day_num > ed.day_num AND (
                              udm.day_num >= sd.day_num OR udm.day_num <= ed.day_num
                          ))
                      )
                ) THEN TRUE
                ELSE FALSE
            END AS is_working_day
        FROM UserDateMatrix udm
        JOIN users u ON u.user_id = udm.user_id
        JOIN work_schedule ws ON u.work_schedule_id = ws.work_schedule_id AND ws.organization_id = :orgId
        JOIN work_schedule_type wst ON ws.work_schedule_type = wst.type_id
    )
    
    SELECT
        udm.work_date,
        udm.user_id,
        udm.user_name,
        r.name AS role,
        udm.mobile_number,
        ws.work_schedule_name AS work_schedule,
        ug.group_name,
        t.id AS timesheet_id,
        t.first_clock_in::TIME AS first_clock_in,
        t.last_clock_out::TIME AS last_clock_out,
        COALESCE(CAST(t.tracked_hours AS TEXT), '00:00:00') AS tracked_hours,
        COALESCE(CAST(t.regular_hours AS TEXT), '00:00:00') AS regular_hours,
        t.status_id,
        ts.status_name AS status,
    
        -- Day Type
        CASE WHEN wdt.is_working_day THEN 'Working Day' ELSE 'Holiday' END AS day_type,
    
        -- User Day Type
        CASE
            WHEN t.first_clock_in IS NULL AND NOT wdt.is_working_day THEN 'Holiday'
            WHEN t.first_clock_in IS NULL THEN 'Time Off'
            WHEN t.first_clock_in IS NOT NULL AND NOT wdt.is_working_day THEN 'Extra Worked Day'
            ELSE 'Working Day'
        END AS user_day_type,
    
        -- Work Status
        CASE
            WHEN t.first_clock_in IS NULL AND NOT wdt.is_working_day THEN 'Holiday'
            WHEN t.date IS NULL THEN 'Time Off'
            WHEN t.first_clock_in IS NOT NULL AND t.last_clock_out IS NULL THEN 'Present'
            WHEN t.first_clock_in IS NOT NULL AND t.last_clock_out IS NOT NULL THEN
                CASE
                    WHEN EXISTS (
                        SELECT 1 FROM timesheet_history th2
                        WHERE th2.timesheet_id = t.id
                          AND th2.log_type = 'CLOCK_OUT'
                          AND th2.log_from = 'SYSTEM_GENERATED'
                    ) THEN 'Failed Clock Out'
                    WHEN (EXTRACT(EPOCH FROM (t.last_clock_out - t.first_clock_in))) >= COALESCE(wsd.expected_seconds, 0)
                    THEN 'Sufficient Hours'
                    ELSE 'Less Worked Hours'
                END
            ELSE 'Not Marked'
        END AS work_status,
    
        th.id AS history_id,
        th.log_time,
        th.log_type,
        th.location_id,
        th.log_from,
        th.logged_timestamp
    
    FROM UserDateMatrix udm
    LEFT JOIN WorkDayType wdt ON udm.user_id = wdt.user_id AND udm.work_date = wdt.work_date
    LEFT JOIN timesheet t ON t.user_id = udm.user_id AND t.date = udm.work_date
    LEFT JOIN timesheet_status ts ON t.status_id = ts.status_id
    LEFT JOIN role r ON udm.role_id = r.role_id
    LEFT JOIN users u ON udm.user_id = u.user_id
    LEFT JOIN work_schedule ws ON ws.work_schedule_id = u.work_schedule_id AND ws.organization_id = :orgId
    LEFT JOIN work_schedule_type wst ON ws.work_schedule_type = wst.type_id
    LEFT JOIN WorkScheduleDuration wsd ON ws.work_schedule_id = wsd.work_schedule_id
    LEFT JOIN timesheet_history th ON t.id = th.timesheet_id
    LEFT JOIN UserGroups ug ON udm.user_id = ug.user_id
    
    ORDER BY udm.user_id, udm.work_date, th.logged_timestamp
    """, nativeQuery = true)
        List<Object[]> fetchTimesheetsWithHistory(
                @Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate,
                @Param("userIds") String[] userIds,
                @Param("orgId") String orgId
        );

    List<TimesheetEntity> findActiveTimesheetsByDate(LocalDate today);

    @Query("SELECT new com.uniq.tms.tms_microservice.dto.UserAttendanceDto(t.userId, t.date, t.status.statusName) " +
            "FROM TimesheetEntity t WHERE t.userId IN :userIds AND t.date BETWEEN :from AND :to")
    List<UserAttendanceDto> findAttendanceForUsersInRange(@Param("userIds") List<String> userIds,
                                                          @Param("from") LocalDate from,
                                                          @Param("to") LocalDate to);

    @Query(value = """
    WITH SelectedUsers AS (
        SELECT *
        FROM users
        WHERE active = TRUE
          AND (:userIds IS NULL OR user_id = ANY(CAST(:userIds AS VARCHAR[])))
    ),
    
    UserGroups AS (
        SELECT
            ug.user_id,
            STRING_AGG(g.group_name, ', ') AS group_name
        FROM user_group ug
        JOIN group_table g ON ug.group_id = g.group_id
        GROUP BY ug.user_id
    ),
    
    DayNumbers AS (
        SELECT 'Sunday' AS day_name, 0 AS day_num UNION ALL
        SELECT 'Monday', 1 UNION ALL
        SELECT 'Tuesday', 2 UNION ALL
        SELECT 'Wednesday', 3 UNION ALL
        SELECT 'Thursday', 4 UNION ALL
        SELECT 'Friday', 5 UNION ALL
        SELECT 'Saturday', 6
    ),
    
    UserDateMatrix AS (
        SELECT
            gs.work_date,
            TO_CHAR(gs.work_date, 'FMDay') AS day_name,
            EXTRACT(DOW FROM gs.work_date)::INT AS day_num,
            u.user_id,
            u.user_name,
            u.role_id,
            u.mobile_number,
            u.work_schedule_id
        FROM SelectedUsers u
        CROSS JOIN LATERAL (
            SELECT generate_series(
                GREATEST(COALESCE(:startDate, u.date_of_joining), u.date_of_joining),
                COALESCE(:endDate, CURRENT_DATE),
                INTERVAL '1 day'
            ) AS work_date
        ) gs
    ),
    
    WorkScheduleDuration AS (
        SELECT
            ws.work_schedule_id,
            wst.type_id,
            wst.type,
            CASE
                WHEN wst.type = 'FIXED' THEN (
                    SELECT SUM(EXTRACT(EPOCH FROM (fws.end_time - fws.start_time))) / COUNT(*)
                    FROM fixed_work_schedule fws
                    WHERE fws.work_schedule_id = ws.work_schedule_id
                )
                WHEN wst.type = 'FLEXIBLE' THEN (
                    SELECT SUM(flws.duration * 3600) / COUNT(*)
                    FROM flexible_work_schedule flws
                    WHERE flws.work_schedule_id = ws.work_schedule_id
                )
                ELSE NULL
            END AS expected_seconds
        FROM work_schedule ws
        JOIN work_schedule_type wst ON ws.work_schedule_type = wst.type_id
    ),
    
    WorkDayType AS (
        SELECT
            udm.user_id,
            udm.work_date,
            CASE
                WHEN wst.type = 'FIXED' AND EXISTS (
                    SELECT 1 FROM fixed_work_schedule fws
                    WHERE fws.work_schedule_id = ws.work_schedule_id
                      AND fws.day ILIKE udm.day_name
                ) THEN TRUE
                WHEN wst.type = 'FLEXIBLE' AND EXISTS (
                    SELECT 1 FROM flexible_work_schedule flws
                    WHERE flws.work_schedule_id = ws.work_schedule_id
                      AND flws.day ILIKE udm.day_name
                ) THEN TRUE
                WHEN wst.type = 'WEEKLY_EXCEPTION' AND EXISTS (
                    SELECT 1
                    FROM weekly_work_schedule wws
                    JOIN DayNumbers sd ON wws.start_day = sd.day_name
                    JOIN DayNumbers ed ON wws.end_day = ed.day_name
                    WHERE wws.work_schedule_id = ws.work_schedule_id
                      AND (
                          (sd.day_num <= ed.day_num AND udm.day_num BETWEEN sd.day_num AND ed.day_num)
                          OR
                          (sd.day_num > ed.day_num AND (
                              udm.day_num >= sd.day_num OR udm.day_num <= ed.day_num
                          ))
                      )
                ) THEN TRUE
                ELSE FALSE
            END AS is_working_day
        FROM UserDateMatrix udm
        JOIN users u ON u.user_id = udm.user_id
        JOIN work_schedule ws ON u.work_schedule_id = ws.work_schedule_id AND ws.organization_id = :orgId
        JOIN work_schedule_type wst ON ws.work_schedule_type = wst.type_id
    )
    
    SELECT
        udm.work_date,
        udm.user_id,
        udm.user_name,
        r.name AS role,
        udm.mobile_number,
        ws.work_schedule_name AS work_schedule,
        ug.group_name,
        t.id AS timesheet_id,
        t.first_clock_in::TIME AS first_clock_in,
        t.last_clock_out::TIME AS last_clock_out,
        COALESCE(CAST(t.tracked_hours AS TEXT), '00:00:00') AS tracked_hours,
        COALESCE(CAST(t.regular_hours AS TEXT), '00:00:00') AS regular_hours,
        t.status_id,
        ts.status_name AS status,
    
        -- Day Type
        CASE WHEN wdt.is_working_day THEN 'Working Day' ELSE 'Holiday' END AS day_type,
    
        -- User Day Type
        CASE
            WHEN t.first_clock_in IS NULL AND NOT wdt.is_working_day THEN 'Holiday'
            WHEN t.first_clock_in IS NULL THEN 'Time Off'
            WHEN t.first_clock_in IS NOT NULL AND NOT wdt.is_working_day THEN 'Extra Worked Day'
            ELSE 'Working Day'
        END AS user_day_type,
    
        -- Work Status
        CASE
            WHEN t.first_clock_in IS NULL AND NOT wdt.is_working_day THEN 'Holiday'
            WHEN t.date IS NULL THEN 'Time Off'
            WHEN t.first_clock_in IS NOT NULL AND t.last_clock_out IS NULL THEN 'Present'
            WHEN t.first_clock_in IS NOT NULL AND t.last_clock_out IS NOT NULL THEN
                CASE
                    WHEN EXISTS (
                        SELECT 1 FROM timesheet_history th2
                        WHERE th2.timesheet_id = t.id
                          AND th2.log_type = 'CLOCK_OUT'
                          AND th2.log_from = 'SYSTEM_GENERATED'
                    ) THEN 'Failed Clock Out'
                    WHEN (EXTRACT(EPOCH FROM (t.last_clock_out - t.first_clock_in))) >= COALESCE(wsd.expected_seconds, 0)
                    THEN 'Sufficient Hours'
                    ELSE 'Less Worked Hours'
                END
            ELSE 'Not Marked'
        END AS work_status,
    
        th.id AS history_id,
        th.log_time,
        th.log_type,
        th.location_id,
        th.log_from,
        th.logged_timestamp
    
    FROM UserDateMatrix udm
    LEFT JOIN WorkDayType wdt ON udm.user_id = wdt.user_id AND udm.work_date = wdt.work_date
    LEFT JOIN timesheet t ON t.user_id = udm.user_id AND t.date = udm.work_date
    LEFT JOIN timesheet_status ts ON t.status_id = ts.status_id
    LEFT JOIN role r ON udm.role_id = r.role_id
    LEFT JOIN users u ON udm.user_id = u.user_id
    LEFT JOIN work_schedule ws ON ws.work_schedule_id = u.work_schedule_id AND ws.organization_id = :orgId
    LEFT JOIN work_schedule_type wst ON ws.work_schedule_type = wst.type_id
    LEFT JOIN WorkScheduleDuration wsd ON ws.work_schedule_id = wsd.work_schedule_id
    LEFT JOIN timesheet_history th ON t.id = th.timesheet_id
    LEFT JOIN UserGroups ug ON udm.user_id = ug.user_id
    
    ORDER BY udm.user_id, udm.work_date, th.logged_timestamp
    """, nativeQuery = true)
    List<Object[]> fetchUserTimesheetsWithHistory(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("userIds") String[] userIds,
            @Param("orgId") String orgId
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
            @Param("userIds") List<String> userIds,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("orgId") String orgId
    );
}
