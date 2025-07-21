package com.uniq.tms.tms_microservice.controller;

import com.uniq.tms.tms_microservice.config.JwtUtil;
import com.uniq.tms.tms_microservice.constant.UserConstant;
import com.uniq.tms.tms_microservice.dto.ApiResponse;
import com.uniq.tms.tms_microservice.dto.WorkScheduleDto;
import com.uniq.tms.tms_microservice.dto.WorkScheduleTypeDto;
import com.uniq.tms.tms_microservice.facade.AuthFacade;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(UserConstant.WorkSchedule_Url)
public class WorkScheduleController {

    private static final Logger log = LogManager.getLogger(WorkScheduleController.class);
    private final AuthFacade authFacade;
    private final JwtUtil jwtUtil;

    public WorkScheduleController(AuthFacade authFacade, JwtUtil jwtUtil){
        this.authFacade = authFacade;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getWorkSchedule(@RequestHeader ("Authorization") String authHeader) {
        if(authHeader == null || authHeader.isBlank()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(403,"Authorization Header Missing",false));
        }
        try {
            String jwt = jwtUtil.extractJwt(authHeader);
            String orgId = jwtUtil.extractOrgIdFromToken(jwt);
            if(orgId == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(401,"Unauthorized - Invalid Organization",null));
            }
            return ResponseEntity.ok(authFacade.getWorkSchedule(orgId));
        } catch (RuntimeException e) {
            log.error("Error occurred during getWorkSchedule: {}", e.getMessage(), e);  // log it
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(403, "Unauthorized", false));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createWorkschedule(@RequestHeader("Authorization") String token, @RequestBody WorkScheduleDto workScheduleDto){
        if(token == null || token.isBlank()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(403,"Authorization Header Missing",false));
        }
        try {
            String jwt = jwtUtil.extractJwt(token);
            String orgId = jwtUtil.extractOrgIdFromToken(jwt);
            if(orgId == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(401,"Unauthorized - Invalid Organization",null));
            }
            return ResponseEntity.ok(authFacade.createWorkSchedule(orgId, workScheduleDto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(403,"Unauthorized",false));
        }
    }

    @PostMapping("/addType")
    public ResponseEntity<ApiResponse> addType(@RequestBody WorkScheduleTypeDto type){
        return ResponseEntity.ok(authFacade.addType(type));
    }

    @PatchMapping("/update")
    public ResponseEntity<ApiResponse> updateWorkSchedule(@RequestHeader("Authorization") String token, @RequestBody WorkScheduleDto dto) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(403, "Authorization Header Missing", false));
        }

        try {
            String jwt = jwtUtil.extractJwt(token);
            String orgId = jwtUtil.extractOrgIdFromToken(jwt);
            if (orgId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(401, "Unauthorized - Invalid Organization", false));
            }

            return ResponseEntity.ok(authFacade.updateWorkSchedule(orgId, dto));

        } catch (RuntimeException e) {
            log.error("Error occurred while updating work schedule: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(500, "Update failed: " + e.getMessage(), false));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteWorkSchedule(@RequestHeader("Authorization") String token,
                                                          @RequestParam("scheduleId") String scheduleId) {
        authFacade.deleteSchedule(token, scheduleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getType")
    public ResponseEntity<ApiResponse> getType(@RequestHeader("Authorization") String token){
        return ResponseEntity.ok(authFacade.getType(token));
    }

}
