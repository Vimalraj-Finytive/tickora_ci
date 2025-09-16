package com.uniq.tms.tms_microservice.service;

import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.model.TimesheetHistory;
import com.uniq.tms.tms_microservice.model.TimesheetStatus;
import java.time.LocalDate;
import java.util.List;

public interface TimesheetService {

    PaginationResponseDto  getAllTimesheets(String userIdFromToken, String orgId, String role, TimesheetReportDto request);
    List<TimesheetHistory> processTimesheetLogs(List<TimesheetHistory> timesheetMiddlewareLogs);
    TimesheetDto updateClockInOut(String userId, LocalDate date, TimesheetDto request, String orgId);
    void autoClockOut(String orgId);
    List<UserDashboardDto> getAllUserInfo(String orgId, String userIdFromToken, LocalDate fromDate, LocalDate toDate, String userId, List<Long>groupIds, String type);
    List<UserTimesheetDto> getUserTimesheets(String userIdFromToken, String orgId, String role, TimesheetReportDto request);
    List<TimesheetStatus> getStatus();
    List<TimesheetHistory> processTimesheet(List<TimesheetHistory> timesheetMiddlewareLogs);
}
