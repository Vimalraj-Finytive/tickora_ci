package com.uniq.tms.tms_microservice.adapter;



import com.uniq.tms.tms_microservice.dto.TimesheetDto;
import com.uniq.tms.tms_microservice.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.entity.TimesheetHistoryEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TimesheetAdapter {

    List<TimesheetDto> filterTimesheetsForAllUsers(LocalDate startDate, LocalDate endDate, Long userId);
    Optional<TimesheetEntity> findByUserIdAndDate(Long userId, LocalDate date);
    TimesheetEntity saveTimesheet(TimesheetEntity timesheet);
    TimesheetHistoryEntity saveTimesheetHistory(TimesheetHistoryEntity history);
    void calculateTrackedAndBreakHours(List<TimesheetHistoryEntity> savedLogs);
    TimesheetEntity save(TimesheetEntity timesheet);
    TimesheetEntity findUserIdAndDate(Long userId, LocalDate date);
    List<TimesheetEntity> findByFirstClockInNotNullAndLastClockOutIsNullAndDate(LocalDate today);
    void saveAll(List<TimesheetEntity> openClockIns);
}

