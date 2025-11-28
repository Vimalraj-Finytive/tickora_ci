package com.uniq.tms.tms_microservice.modules.leavemanagement.controller;

import com.uniq.tms.tms_microservice.modules.leavemanagement.constant.LeaveConstant;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.facade.TimeOffFacade;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
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

    @PostMapping("/requests/filter")
    public ResponseEntity<ApiResponse<Map<String, List<TimeOffRequestGroupDto>>>> getRequests(
            @RequestHeader("Authorization") String token,
            @RequestBody TimeOffccDto dto) {
        ApiResponse<Map<String, List<TimeOffRequestGroupDto>>> result =
                timeOffFacade.filterRequests(dto);
        return ResponseEntity.status(result.getStatusCode()).body(result);
    }

    @GetMapping("/requests/filter/role/{fromDate}/{toDate}")
    public ResponseEntity<ApiResponse<List<TimeoffRequestResponseDto>>> filterRequestsBasedOnRole(
            @PathVariable LocalDate fromDate,
            @PathVariable LocalDate toDate) {

        ApiResponse<List<TimeoffRequestResponseDto>> response = timeOffFacade.filterRequestsBasedOnRole(fromDate, toDate);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}
