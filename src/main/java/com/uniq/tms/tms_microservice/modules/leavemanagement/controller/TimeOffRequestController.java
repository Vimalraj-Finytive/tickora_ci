package com.uniq.tms.tms_microservice.modules.leavemanagement.controller;

import com.uniq.tms.tms_microservice.modules.leavemanagement.constant.LeaveConstant;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.facade.TimeOffFacade;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffExportRequest;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(LeaveConstant.TIMEOFF_REQUEST_URL)
public class TimeOffRequestController {

    private final TimeOffFacade timeOffFacade;
    private final AuthHelper authHelper;

    public TimeOffRequestController(TimeOffFacade timeOffFacade, AuthHelper authHelper) {
        this.timeOffFacade = timeOffFacade;
        this.authHelper = authHelper;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createRequest(@RequestHeader("Authorization") String token, @RequestBody TimeOffRequestDto requestDto) {
        ApiResponse createdRequest = timeOffFacade.createRequest(requestDto);
        return ResponseEntity.ok(createdRequest);
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse> employeeUpdateStatus(@RequestHeader("Authorization") String token, @RequestBody EmployeeStatusUpdateDto dto){
        ApiResponse updateRequest = timeOffFacade.employeeUpdateStatus(dto);
        return ResponseEntity.ok(updateRequest);
    }

    @PatchMapping("/update")
    public ResponseEntity<ApiResponse> adminUpdateStatus(@RequestHeader("Authorization") String token, @RequestBody AdminStatusUpdateDto dto){
        ApiResponse updateRequest = timeOffFacade.adminUpdateStatus(dto);
        return ResponseEntity.ok(updateRequest);
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse> getStatus(
            @RequestHeader("Authorization") String token) {
        ApiResponse<List<StatusEnumDto>> response = timeOffFacade.getStatus();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, List<TimeOffRequestGroupDto>>>> getRequests(
            @RequestHeader("Authorization") String token,
            @RequestBody TimeOffExportRequest dto) {
        String loggedUserId = authHelper.getUserId();
        ApiResponse<Map<String, List<TimeOffRequestGroupDto>>> result =
                timeOffFacade.filterRequests(dto,loggedUserId);
        return ResponseEntity.status(result.getStatusCode()).body(result);
    }

    @GetMapping("/requests/filter/role/{fromDate}/{toDate}")
    public ResponseEntity<ApiResponse<List<TimeoffRequestResponseDto>>> filterRequestsBasedOnRole(
            @PathVariable LocalDate fromDate,
            @PathVariable LocalDate toDate) {

        ApiResponse<List<TimeoffRequestResponseDto>> response = timeOffFacade.filterRequestsBasedOnRole(fromDate, toDate);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<Map<String,String>>> startExport(@RequestBody TimeOffExportRequestDto request) {
        String schema = authHelper.getSchema();
        String orgId = authHelper.getOrgId();
        ApiResponse<Map<String,String>> export = timeOffFacade.startExportJob(request, schema, orgId);
        return ResponseEntity.status(export.getStatusCode()).body(export);
    }

    @GetMapping("generate/status/{exportId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getReportStatus(
            @PathVariable String exportId) {
        String schema = authHelper.getSchema();
        String orgId = authHelper.getOrgId();
        ApiResponse<Map<String, String>> reportStatus = timeOffFacade.getExportStatus(schema, orgId, exportId);
        return ResponseEntity.status(reportStatus.getStatusCode()).body(reportStatus);
    }

    @GetMapping("/download/{exportId}")
    public ResponseEntity<ApiResponse<Resource>> downloadReport(
            @PathVariable String exportId,
            @RequestParam String type) throws Exception {
        String schema = authHelper.getSchema();
        String orgId = authHelper.getOrgId();
        ApiResponse<Resource> downloadStatus = timeOffFacade.downloadExport(schema, orgId, exportId, type);
        return ResponseEntity.status(downloadStatus.getStatusCode()).body(downloadStatus);
    }

}
