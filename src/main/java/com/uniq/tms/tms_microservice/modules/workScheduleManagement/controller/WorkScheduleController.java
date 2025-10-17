package com.uniq.tms.tms_microservice.modules.workScheduleManagement.controller;

import com.uniq.tms.tms_microservice.modules.workScheduleManagement.facade.WorkScheduleFacade;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.dto.WorkScheduleDto;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.dto.WorkScheduleTypeDto;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.constant.WorkScheduleConstant;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(WorkScheduleConstant.WorkSchedule_Url)
public class WorkScheduleController {

    private static final Logger log = LogManager.getLogger(WorkScheduleController.class);
    private final WorkScheduleFacade workScheduleFacade;
    private final AuthHelper authHelper;

    public WorkScheduleController(WorkScheduleFacade workScheduleFacade, AuthHelper authHelper){
        this.workScheduleFacade = workScheduleFacade;
        this.authHelper = authHelper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getWorkSchedule(@RequestHeader ("Authorization") String authHeader) {
        try {
            String orgId = authHelper.getOrgId();
            if(orgId == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(401,"Unauthorized - Invalid Organization",null));
            }
            return ResponseEntity.ok(workScheduleFacade.getWorkSchedule(orgId));
        } catch (RuntimeException e) {
            log.error("Error occurred during getWorkSchedule: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(403, "Unauthorized", false));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createWorkschedule(@RequestHeader("Authorization") String token, @Valid @RequestBody WorkScheduleDto workScheduleDto){
            String orgId = authHelper.getOrgId();
            if(orgId == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(401,"Unauthorized - Invalid Organization",null));
            }
            return ResponseEntity.ok(workScheduleFacade.createWorkSchedule(orgId, workScheduleDto));
    }

    @PostMapping("/addType")
    public ResponseEntity<ApiResponse> addType(@RequestBody WorkScheduleTypeDto type){
        return ResponseEntity.ok(workScheduleFacade.addType(type));
    }

    @PatchMapping("/update")
    public ResponseEntity<ApiResponse> updateWorkSchedule(@RequestHeader("Authorization") String token, @RequestBody WorkScheduleDto dto) {
        try {
            String orgId = authHelper.getOrgId();
            if (orgId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(401, "Unauthorized - Invalid Organization", false));
            }
            return ResponseEntity.ok(workScheduleFacade.updateWorkSchedule(orgId, dto));
        } catch (RuntimeException e) {
            log.error("Error occurred while updating work schedule: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(500, "Update failed: " + e.getMessage(), false));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteWorkSchedule(@RequestHeader("Authorization") String token,
                                                          @RequestParam("scheduleId") String scheduleId) {
        workScheduleFacade.deleteSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getType")
    public ResponseEntity<ApiResponse> getType(@RequestHeader("Authorization") String token){
        return ResponseEntity.ok(workScheduleFacade.getType());
    }

}
