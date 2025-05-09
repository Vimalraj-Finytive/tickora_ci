package com.uniq.tms.tms_microservice.repository;

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
    UserDateMatrix AS (
        SELECT
            gs.work_date,
            u.user_id,
            u.user_name,
            u.role_id
        FROM SelectedUsers u
        CROSS JOIN LATERAL (
            SELECT generate_series(
                COALESCE(:startDate, u.date_of_joining),
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
        t.id AS timesheet_id,
        t.first_clock_in::TIME AS first_clock_in,
        t.last_clock_out::TIME AS last_clock_out,
        COALESCE(CAST(t.tracked_hours AS TEXT), '00:00:00') AS tracked_hours,
        COALESCE(CAST(t.regular_hours AS TEXT), '00:00:00') AS regular_hours,
   
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
            WHEN t.first_clock_in IS NOT NULL AND t.last_clock_out IS NOT NULL THEN
                CASE
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
    ORDER BY udm.user_id, udm.work_date, th.logged_timestamp
    """, nativeQuery = true)
    List<Object[]> fetchTimesheetsWithHistory(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate,
                                              @Param("userIds") Long[] userIds);

    List<TimesheetEntity> findActiveTimesheetsByDate(LocalDate today);
}
