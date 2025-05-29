package com.uniq.tms.tms_microservice.service;

import com.uniq.tms.tms_microservice.dto.TimesheetDto;
import com.uniq.tms.tms_microservice.dto.TimesheetReportDto;
import com.uniq.tms.tms_microservice.dto.UserDashboardDto;
import com.uniq.tms.tms_microservice.dto.UserTimesheetDto;
import com.uniq.tms.tms_microservice.dto.UserTimesheetResponseDto;
import com.uniq.tms.tms_microservice.model.TimesheetHistory;
import java.time.LocalDate;
import java.util.List;

public interface TimesheetService {

    List<UserTimesheetResponseDto> getAllTimesheets(Long userIdFromToken, Long orgId, String role, TimesheetReportDto request);
    List<TimesheetHistory> processTimesheetLogs(List<TimesheetHistory> timesheetMiddlewareLogs);
    TimesheetDto updateClockInOut(Long userId, LocalDate date, TimesheetDto request);
    void autoClockOut();
    List<UserDashboardDto> getAllUserInfo(Long orgId, Long userIdFromToken, LocalDate fromDate, LocalDate toDate, Long userId);
    List<UserTimesheetDto> getUserTimesheets(Long userIdFromToken, Long orgId, String role, TimesheetReportDto request);
}
