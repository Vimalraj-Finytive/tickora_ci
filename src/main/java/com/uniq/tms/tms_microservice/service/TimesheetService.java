package com.uniq.tms.tms_microservice.service;

import com.uniq.tms.tms_microservice.dto.TimesheetDto;
import com.uniq.tms.tms_microservice.model.TimesheetHistory;
import java.time.LocalDate;
import java.util.List;

public interface TimesheetService {

    List<TimesheetDto> getAllTimesheets(Long userIdFromToken, String role, LocalDate date, String timePeriod, Long userId, List<Long> groupIds);
    List<TimesheetHistory> processTimesheetLogs(List<TimesheetHistory> timesheetMiddlewareLogs);
    TimesheetDto updateClockInOut(Long userId, LocalDate date, TimesheetDto request);
    void autoClockOutForAllEmployees();
}
