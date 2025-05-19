package com.uniq.tms.tms_microservice.controller;

import com.uniq.tms.tms_microservice.config.JwtUtil;
import com.uniq.tms.tms_microservice.constant.UserConstant;
import com.uniq.tms.tms_microservice.dto.ApiResponse;
import com.uniq.tms.tms_microservice.facade.AuthFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserConstant.WorkSchedule_Url)
public class WorkScheduleController {
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
            Long orgId = jwtUtil.extractOrgIdFromToken(jwt);
            if(orgId == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(401,"Unauthorized - Invalid Organization",null));
            }
            return ResponseEntity.ok(authFacade.getWorkSchedule(orgId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(403,"Unauthorized",false));
        }
    }
}
