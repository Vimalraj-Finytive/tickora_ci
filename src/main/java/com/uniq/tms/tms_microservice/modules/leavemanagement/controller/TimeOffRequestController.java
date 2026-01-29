package com.uniq.tms.tms_microservice.modules.leavemanagement.controller;

import com.uniq.tms.tms_microservice.modules.leavemanagement.constant.LeaveConstant;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.facade.TimeOffFacade;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffExportRequest;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.shared.dto.EnumDto;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import jakarta.validation.Valid;
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
    public ResponseEntity<ApiResponse<TimeOffRequestDto>> createRequest(@RequestHeader("Authorization") String token,
                                                                        @Valid @RequestBody TimeOffRequestDto requestDto) {
        ApiResponse<TimeOffRequestDto> createdRequest = timeOffFacade.createRequest(requestDto);
        return ResponseEntity.ok(createdRequest);
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<EmployeeStatusUpdateDto>> employeeUpdateStatus(@RequestHeader("Authorization") String token,
                                                                                     @RequestBody EmployeeStatusUpdateDto dto){
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

    @GetMapping("/hourType")
    public ResponseEntity<ApiResponse<List<EnumDto>>> getHourType(
            @RequestHeader("Authorization") String token) {
        ApiResponse<List<EnumDto>> response = timeOffFacade.getHourType();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}
