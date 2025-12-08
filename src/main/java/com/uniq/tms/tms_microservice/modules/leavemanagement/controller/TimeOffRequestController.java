package com.uniq.tms.tms_microservice.modules.leavemanagement.controller;

import com.uniq.tms.tms_microservice.modules.leavemanagement.constant.LeaveConstant;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.facade.TimeOffFacade;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffExportRequest;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.shared.dto.EnumDto;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(LeaveConstant.TIMEOFF_REQUEST_URL)
public class TimeOffRequestController {

    private static final Logger log = LogManager.getLogger(TimeOffRequestController.class);

    private final TimeOffFacade timeOffFacade;
    private final AuthHelper authHelper;

    public TimeOffRequestController(TimeOffFacade timeOffFacade, AuthHelper authHelper) {
        this.timeOffFacade = timeOffFacade;
        this.authHelper = authHelper;
    }

    @Value("${csv.request.download.dir}")
    private String downloadDir;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<TimeOffRequestDto>> createRequest(@RequestHeader("Authorization") String token, @RequestBody TimeOffRequestDto requestDto) {
        ApiResponse<TimeOffRequestDto> createdRequest = timeOffFacade.createRequest(requestDto);
        return ResponseEntity.ok(createdRequest);
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<EmployeeStatusUpdateDto>> employeeUpdateStatus(@RequestHeader("Authorization") String token, @RequestBody EmployeeStatusUpdateDto dto){
        ApiResponse<EmployeeStatusUpdateDto> updateRequest = timeOffFacade.employeeUpdateStatus(dto);
        return ResponseEntity.ok(updateRequest);
    }

    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<AdminStatusUpdateDto>> adminUpdateStatus(@RequestHeader("Authorization") String token, @RequestBody AdminStatusUpdateDto dto){
        ApiResponse<AdminStatusUpdateDto> updateRequest = timeOffFacade.adminUpdateStatus(dto);
        return ResponseEntity.ok(updateRequest);
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse> getStatus(
            @RequestHeader("Authorization") String token) {
        ApiResponse<List<EnumDto>> response = timeOffFacade.getStatus();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<List<TimeOffExportDto>>> getRequests(
            @RequestHeader("Authorization") String token,
            @RequestBody TimeOffExportRequest dto) {
        String loggedUserId = authHelper.getUserId();
        ApiResponse<List<TimeOffExportDto>> result =
                timeOffFacade.filterRequests(dto, loggedUserId);
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

    @GetMapping("/download")
    public ResponseEntity<?> downloadTimeoffRequest(
            @RequestHeader("Authorization") String token,
            @RequestParam String fileName) {
        try {
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse<>(400, "Invalid file name", null)
                );
            }
            Path filePath = Paths.get(downloadDir).resolve(fileName);
            if (!Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ApiResponse<>(404, "File not generated yet. Please try again later.", null)
                );
            }
            long fileSize = Files.size(filePath);
            InputStreamResource resource = new InputStreamResource(Files.newInputStream(filePath));
            MediaType mediaType = determineMediaType(fileName);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName +
                                    "\"; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8))
                    .contentType(mediaType)
                    .contentLength(fileSize)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>(500, "Error reading file", null)
            );
        }
    }

    private MediaType determineMediaType(String fileName) {
        if (fileName.toLowerCase().endsWith(".csv")) {
            return MediaType.parseMediaType("text/csv");
        } else if (fileName.toLowerCase().endsWith(".xlsx")) {
            return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } else {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    @GetMapping("/hourType")
    public ResponseEntity<ApiResponse<List<EnumDto>>> getHourType(
            @RequestHeader("Authorization") String token) {
        ApiResponse<List<EnumDto>> response = timeOffFacade.getHourType();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}
