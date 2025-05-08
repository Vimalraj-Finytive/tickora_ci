package com.uniq.tms.tms_microservice.controller;

import com.uniq.tms.tms_microservice.constant.UserConstant;
import com.uniq.tms.tms_microservice.dto.ApiResponse;
import com.uniq.tms.tms_microservice.dto.TimesheetDto;
import com.uniq.tms.tms_microservice.dto.TimesheetHistoryDto;
import com.uniq.tms.tms_microservice.dto.TimesheetReportDto;
import com.uniq.tms.tms_microservice.facade.AuthFacade;
import com.uniq.tms.tms_microservice.util.ReportUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(UserConstant.Timesheet_URL)

public class  TimesheetController {

    private final AuthFacade authFacade;
    private final ReportUtils reportUtils;

    public TimesheetController(AuthFacade authFacade, ReportUtils reportUtils) {
        this.authFacade = authFacade;
        this.reportUtils = reportUtils;
    }

    @GetMapping
    public ResponseEntity<?> getAllTimesheets(@RequestHeader("Authorization") String token,
                                                        @ModelAttribute TimesheetReportDto request) {
        List<TimesheetDto> timesheets = authFacade.getAllTimesheets(token,request);
        return ResponseEntity.ok(new ApiResponse(200, "Success", timesheets));
    }

    @PostMapping("/clockin")
    public ResponseEntity<ApiResponse> logTimesheet(@RequestHeader("Authorization") String token,@RequestBody List<TimesheetHistoryDto> timesheetLogs) {
        List<TimesheetHistoryDto> savedLogs = authFacade.processTimesheetLogs(token,timesheetLogs);
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
            @RequestHeader("Authorization") String token,
            @RequestParam Long userId,
            @RequestParam LocalDate date,
            @RequestBody TimesheetDto request) {

        TimesheetDto timesheetDto = authFacade.upsertClockInOut(token,userId, date, request);

        return ResponseEntity.ok(new ApiResponse<>(200, "Timesheet upserted successfully", timesheetDto));
    }

    @PostMapping
    public ResponseEntity<?> getTimesheets(
            @RequestHeader("Authorization") String token,
            @RequestBody(required = false) TimesheetReportDto request) {

        List<TimesheetDto> timesheets = authFacade.getAllTimesheets(
                token,
                request
        );

        if ("csv".equalsIgnoreCase(request.getFormat())) {
            try {
                byte[] csv = reportUtils.exportToCsv(timesheets);
                InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(csv));
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=timesheetReport.csv")
                        .contentType(MediaType.parseMediaType("text/csv"))
                        .body(resource);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(500, "CSV export failed", e.getMessage()));
            }
        } else if ("xlsx".equalsIgnoreCase(request.getFormat())) {
            try {
                byte[] xlsx = reportUtils.exportToXlsx(timesheets);
                InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(xlsx));
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=timesheetReport.xlsx")
                        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(resource);
            }catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(500, "XLSX export failed", e.getMessage()));
            }
        }
        return ResponseEntity.ok(new ApiResponse(200, "Report Downloaded Success", timesheets));
    }
}
