package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.TimesheetEntity;
import io.micrometer.common.lang.Nullable;
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
    WITH DateSeries AS (
        SELECT generate_series(
            COALESCE(CAST(:startDate AS DATE), '1970-01-01'),
            COALESCE(CAST(:endDate AS DATE), CURRENT_DATE),
            INTERVAL '1 day'
        ) AS work_date
    )
    SELECT
        d.work_date,
        u.user_id,
        u.user_name,
        r.name AS role,
        t.id AS timesheet_id,
        t.first_clock_in,
        t.last_clock_out,
        t.tracked_hours,
        t.regular_hours,
        
        -- Day type (holiday/working)
        CASE
            WHEN trim(to_char(d.work_date, 'Day')) ILIKE trim(ws.rest_day) THEN 'Holiday'
            ELSE 'Working Day'
        END AS day_type,
        
        -- Day status
        CASE
            WHEN t.date IS NULL THEN
                CASE
                    WHEN trim(to_char(d.work_date, 'Day')) ILIKE trim(ws.rest_day) THEN 'Holiday'
                    ELSE 'Time Off'
                END
            ELSE
                CASE
                    WHEN trim(to_char(d.work_date, 'Day')) ILIKE trim(ws.rest_day) THEN 'Extra Worked Day'
                    ELSE 'Working Day'
                END
        END AS day_status,
        
        -- Work status
        CASE
            WHEN t.first_clock_in IS NOT NULL
                 AND t.last_clock_out IS NOT NULL
                 AND (
                     EXTRACT(EPOCH FROM (t.last_clock_out - t.first_clock_in)) / 3600.0
                 ) >= (
                     EXTRACT(EPOCH FROM (ws.end_time - ws.start_time)) / 3600.0
                 )
            THEN 'Sufficient Hours'
            
            WHEN t.first_clock_in IS NOT NULL
                 AND t.last_clock_out IS NOT NULL
                 AND (
                     EXTRACT(EPOCH FROM (t.last_clock_out - t.first_clock_in)) / 3600.0
                 ) < (
                     EXTRACT(EPOCH FROM (ws.end_time - ws.start_time)) / 3600.0
                 )
            THEN 'Less Worked Hours'
        END AS work_status,
        
        -- Timesheet history (log entries)
        th.id AS history_id,
        th.log_time,
        th.log_type,
        th.location_id,
        th.log_from,
        th.logged_timestamp

    FROM DateSeries d
    CROSS JOIN (
        SELECT * FROM users WHERE (:userId IS NULL OR user_id = :userId)
    ) u
    LEFT JOIN timesheet t
        ON d.work_date = t.date AND t.user_id = u.user_id
    LEFT JOIN role r
        ON u.role_id = r.role_id
    LEFT JOIN work_schedule ws
        ON ws.is_active = TRUE
    LEFT JOIN timesheet_history th
        ON t.id = th.timesheet_id

    WHERE (:userId IS NULL OR u.user_id = :userId)

    ORDER BY u.user_id, d.work_date, th.logged_timestamp
""", nativeQuery = true)
    List<Object[]> fetchTimesheetsWithHistory(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("userId") Long userId
    );

    List<TimesheetEntity> findByFirstClockInNotNullAndLastClockOutIsNullAndDate(LocalDate today);

}

