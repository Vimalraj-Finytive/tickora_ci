package com.uniq.tms.tms_microservice.modules.timesheetManagement.controller;

import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.*;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.mapper.TimesheetDtoMapper;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.constant.TimesheetConstant;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.facade.TimesheetFacade;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(TimesheetConstant.Timesheet_URL)
public class  TimesheetController {

    private final TimesheetFacade timesheetFacade;
    private final AuthHelper authHelper;
    private final TimesheetDtoMapper timesheetDtoMapper;

    public TimesheetController(TimesheetFacade timesheetFacade, AuthHelper authHelper, TimesheetDtoMapper timesheetDtoMapper) {
        this.timesheetFacade = timesheetFacade;
        this.authHelper = authHelper;
        this.timesheetDtoMapper = timesheetDtoMapper;
    }
    
    @PostMapping
    public ResponseEntity<PaginationResponseDto> getAllTimesheets(@RequestHeader("Authorization") String token,
                                                                  @RequestBody TimesheetReportDto request) {
        PaginationResponseDto timesheets =timesheetFacade.getAllTimesheets(request);
        return ResponseEntity.ok(timesheets);
    }

    @PostMapping("/clockin")
    public ResponseEntity<ApiResponse> logTimesheet(@RequestHeader("Authorization") String token, @RequestBody List<TimesheetHistoryDto> timesheetLogs) {
        List<TimesheetHistoryDto> savedLogs =timesheetFacade.processTimesheetLogs(timesheetLogs);
        return ResponseEntity.ok(new ApiResponse(201, "Timesheet logged successfully", savedLogs));
    }

    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<TimesheetDto>> updateClockInOutTimes(
            @RequestParam String userId,
            @RequestParam LocalDate date,
            @RequestBody TimesheetDto request) {
        String orgId = authHelper.getOrgId();
        TimesheetDto updatedTimesheet =timesheetFacade.updateClockInOut(userId, date, request,orgId);

        if (updatedTimesheet == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, "Timesheet not found for user on " + date, null));
        }

        return ResponseEntity.ok(new ApiResponse<>(200, "Timesheet updated successfully", updatedTimesheet));
    }

    @PutMapping("/editTimesheet")
    public ResponseEntity<ApiResponse<TimesheetDto>> upsertClockInOutTimes(
            @RequestParam String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody TimesheetDto request) {

        String userIdFromToken = authHelper.getUserId();
        String role = authHelper.getCurrentUser().getRole();
        String orgId = authHelper.getOrgId();

        TimesheetDto timesheetDto =
                timesheetFacade.upsertClockInOut(userIdFromToken, userId, date, request, orgId, role);

        return ResponseEntity.ok(new ApiResponse<>(200, "Timesheet upserted successfully", timesheetDto));
    }

    @PostMapping("/dashboard")
    public ResponseEntity<?> getDashboard(@RequestHeader("Authorization") String token,
                                          @RequestBody(required = false) DashboardDto request) {
        List<UserDashboardDto> dashboards =timesheetFacade.getAllUserInfo( request);
        return ResponseEntity.ok(new ApiResponse(200, "Dashboard Loaded Successfully", dashboards));
    }

    @PostMapping("/userTimesheets")
    public ResponseEntity<?> getUserTimesheets(@RequestHeader("Authorization") String token,
                                               @RequestBody TimesheetReportDto request) {
        List<UserTimesheetDto> timesheets =timesheetFacade.getUserTimesheets(request);
        return ResponseEntity.ok(new ApiResponse(200, "Success", timesheets));
    }
    
    @GetMapping("/status")
    public ResponseEntity<ApiResponse> addStatus(@RequestHeader("Authorization") String token){
        return ResponseEntity.ok(timesheetFacade.getStatus());
    }

    @PostMapping("dashboard/summary")
    public ResponseEntity<?> getDashboardSummary(@RequestHeader("Authorization") String token,
                                                 @RequestBody DashboardSummaryRequest request) {
        List<DashboardSummaryDto> summary = timesheetFacade.getDashboardSummary(request);
        return ResponseEntity.ok(new ApiResponse(200, "Dashboard Summary Loaded Successfully", summary));
    }

}
