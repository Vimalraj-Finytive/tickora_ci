package com.uniq.tms.tms_microservice.service;



import com.uniq.tms.tms_microservice.dto.TimesheetDto;
import com.uniq.tms.tms_microservice.model.TimesheetHistory;

import java.time.LocalDate;
import java.util.List;

public interface TimesheetService {

    List<TimesheetDto> getAllTimesheets(LocalDate date, String timePeriod, Long userId);
    List<TimesheetHistory> processTimesheetLogs(List<TimesheetHistory> timesheetMiddlewareLogs);
    TimesheetDto updateClockInOut(Long userId, LocalDate date, TimesheetDto request);
    void autoClockOutForAllEmployees();
}

