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
        t.id AS timesheet_id,
        t.user_id,
        t.date,
        u.user_name,
        r.name AS role,
        
        -- Day type: is this date a working day or a holiday
        CASE 
            WHEN trim(to_char(d.work_date, 'Day')) ILIKE trim(ws.rest_day) THEN 'Holiday' 
            ELSE 'Working Day' 
        END AS day_type,
        
        t.first_clock_in,
        t.last_clock_out,
        t.tracked_hours,
        t.regular_hours,
        
        -- What happened that day
        CASE 
            WHEN t.date IS NULL THEN 
                CASE 
                    WHEN trim(to_char(d.work_date, 'Day')) ILIKE trim(ws.rest_day) THEN 'HOLIDAY' 
                    ELSE 'TIME_OFF' 
                END
            ELSE 
                CASE 
                    WHEN trim(to_char(d.work_date, 'Day')) ILIKE trim(ws.rest_day) THEN 'ExtraWorkedDay' 
                    ELSE 'WORKING_DAY' 
                END
        END AS dayy_type,

        -- Work status based on expected vs actual work time
        CASE
            WHEN t.first_clock_in IS NOT NULL
                 AND t.last_clock_out IS NOT NULL
                 AND (EXTRACT(EPOCH FROM (t.last_clock_out - t.first_clock_in))/3600.0) >=
                     (EXTRACT(EPOCH FROM (ws.end_time - ws.start_time))/3600.0)
            THEN 'SUFFICIENT_HOURS'
        
            WHEN t.first_clock_in IS NOT NULL
                 AND t.last_clock_out IS NOT NULL
                 AND (EXTRACT(EPOCH FROM (t.last_clock_out - t.first_clock_in))/3600.0) <
                     (EXTRACT(EPOCH FROM (ws.end_time - ws.start_time))/3600.0)
            THEN 'LESS_WORKED_HOURS'
        END AS work_status,

        -- Timesheet history
        th.id AS history_id,
        th.log_time,
        th.log_type,
        th.location_id,
        th.log_from,
        th.logged_timestamp

    FROM DateSeries d
    LEFT JOIN Timesheet t 
        ON d.work_date = t.date AND (:userId IS NULL OR t.user_id = :userId)
    LEFT JOIN Users u 
        ON t.user_id = u.user_id
    LEFT JOIN Role r 
        ON u.role_id = r.role_id
    LEFT JOIN Work_Schedule ws 
        ON ws.is_active = TRUE
    LEFT JOIN Timesheet_History th
        ON t.id = th.timesheet_id

    WHERE (:userId IS NOT NULL OR t.user_id IS NOT NULL)
    ORDER BY d.work_date, th.logged_timestamp
    """, nativeQuery = true)
    List<Object[]> fetchTimesheetsWithHistory(
            @Param("startDate") @Nullable LocalDate startDate,
            @Param("endDate") @Nullable LocalDate endDate,
            @Param("userId") @Nullable Long userId
    );


    List<TimesheetEntity> findByFirstClockInNotNullAndLastClockOutIsNullAndDate(LocalDate today);
}

