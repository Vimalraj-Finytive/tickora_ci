package com.uniq.tms.tms_microservice.controller;

import com.uniq.tms.tms_microservice.constant.UserConstant;
import com.uniq.tms.tms_microservice.dto.ApiResponse;
import com.uniq.tms.tms_microservice.dto.DashboardDto;
import com.uniq.tms.tms_microservice.dto.TimesheetDto;
import com.uniq.tms.tms_microservice.dto.TimesheetHistoryDto;
import com.uniq.tms.tms_microservice.dto.TimesheetReportDto;
import com.uniq.tms.tms_microservice.dto.UserDashboardDto;
import com.uniq.tms.tms_microservice.dto.UserTimesheetDto;
import com.uniq.tms.tms_microservice.dto.UserTimesheetResponseDto;
import com.uniq.tms.tms_microservice.facade.AuthFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(UserConstant.Timesheet_URL)
public class  TimesheetController {

    private final AuthFacade authFacade;
    public TimesheetController(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    @PostMapping
    public ResponseEntity<?> getAllTimesheets(@RequestHeader("Authorization") String token,
                                                        @RequestBody TimesheetReportDto request) {
        List<UserTimesheetResponseDto> timesheets = authFacade.getAllTimesheets(request);
        return ResponseEntity.ok(new ApiResponse(200, "Success", timesheets));
    }

    @PostMapping("/clockin")
    public ResponseEntity<ApiResponse> logTimesheet(@RequestHeader("Authorization") String token,@RequestBody List<TimesheetHistoryDto> timesheetLogs) {
        List<TimesheetHistoryDto> savedLogs = authFacade.processTimesheetLogs(timesheetLogs);
        return ResponseEntity.ok(new ApiResponse(201, "Timesheet logged successfully", savedLogs));
    }

    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<TimesheetDto>> updateClockInOutTimes(
            @RequestParam String userId,
            @RequestParam LocalDate date,
            @RequestBody TimesheetDto request) {

        TimesheetDto updatedTimesheet = authFacade.updateClockInOut(userId, date, request);

        if (updatedTimesheet == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, "Timesheet not found for user on " + date, null));
        }

        return ResponseEntity.ok(new ApiResponse<>(200, "Timesheet updated successfully", updatedTimesheet));
    }

    @PutMapping("/editTimesheet")
    public ResponseEntity<ApiResponse<TimesheetDto>> upsertClockInOutTimes(
            @RequestHeader("Authorization") String token,
            @RequestParam String userId,
            @RequestParam LocalDate date,
            @RequestBody TimesheetDto request) {

        TimesheetDto timesheetDto = authFacade.upsertClockInOut(userId, date, request);

        return ResponseEntity.ok(new ApiResponse<>(200, "Timesheet upserted successfully", timesheetDto));
    }

    @PostMapping("/dashboard")
    public ResponseEntity<?> getDashboard(@RequestHeader("Authorization") String token,
                                          @RequestBody(required = false) DashboardDto request) {
        List<UserDashboardDto> dashboards = authFacade.getAllUserInfo( request);
        return ResponseEntity.ok(new ApiResponse(200, "Dashboard Loaded Successfully", dashboards));
    }

    @PostMapping("/userTimesheets")
    public ResponseEntity<?> getUserTimesheets(@RequestHeader("Authorization") String token,
                                               @RequestBody TimesheetReportDto request) {
        List<UserTimesheetDto> timesheets = authFacade.getUserTimesheets(request);
        return ResponseEntity.ok(new ApiResponse(200, "Success", timesheets));
    }
    
    @GetMapping("/status")
    public ResponseEntity<ApiResponse> addStatus(@RequestHeader("Authorization") String token){
        return ResponseEntity.ok(authFacade.getStatus());
    }
}
