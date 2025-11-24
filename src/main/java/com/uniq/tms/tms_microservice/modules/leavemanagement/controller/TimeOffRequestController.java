package com.uniq.tms.tms_microservice.modules.leavemanagement.controller;

import com.uniq.tms.tms_microservice.modules.leavemanagement.constant.LeaveConstant;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.facade.TimeOffFacade;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(LeaveConstant.TIMEOFF_REQUEST_URL)
public class TimeOffRequestController {

    private final TimeOffFacade timeOffFacade;

    public TimeOffRequestController(TimeOffFacade timeOffFacade) {
        this.timeOffFacade = timeOffFacade;
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

    @PostMapping("/requests/filter")
    public ResponseEntity<ApiResponse<List<TimeoffRequestResponseDto>>> filterRequests(
            @RequestBody RequestFilterDto dto) {
        ApiResponse<List<TimeoffRequestResponseDto>> response =
                timeOffFacade.filterRequests(dto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}
