package com.uniq.tms.tms_microservice.controller;

import com.uniq.tms.tms_microservice.constant.UserConstant;
import com.uniq.tms.tms_microservice.dto.ApiResponse;
import com.uniq.tms.tms_microservice.dto.TimesheetDto;
import com.uniq.tms.tms_microservice.dto.TimesheetHistoryDto;
import com.uniq.tms.tms_microservice.facade.AuthFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(UserConstant.Timesheet_URL)

public class  TimesheetController {

    private final AuthFacade authFacade;

    public TimesheetController(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllTimesheets(@RequestHeader("Authorization") String token,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String timePeriod,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) List<Long> groupId) {

        List<TimesheetDto> timesheets = authFacade.getAllTimesheets(token,date, timePeriod,userId, groupId);
        return ResponseEntity.ok(new ApiResponse(200, "Timesheets fetched successfully", timesheets));
    }

    @PostMapping("/clockin")
    public ResponseEntity<ApiResponse> logTimesheet(@RequestBody List<TimesheetHistoryDto> timesheetLogs) {
        List<TimesheetHistoryDto> savedLogs = authFacade.processTimesheetLogs(timesheetLogs);
        return ResponseEntity.ok(new ApiResponse(201, "Timesheet logged successfully", savedLogs));
    }

    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<TimesheetDto>> updateClockInOutTimes(
            @RequestParam Long userId,
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
            @RequestParam Long userId,
            @RequestParam LocalDate date,
            @RequestBody TimesheetDto request) {

        TimesheetDto timesheetDto = authFacade.upsertClockInOut(userId, date, request);

        return ResponseEntity.ok(new ApiResponse<>(200, "Timesheet upserted successfully", timesheetDto));
    }
}
