package com.uniq.tms.tms_microservice.controller;

import com.uniq.tms.tms_microservice.constant.UserConstant;
import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.facade.AuthFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(UserConstant.FACE_URL)
public class FaceController{

    private final AuthFacade authFacade;

    public FaceController(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterDto>> registerUserFace(@RequestHeader("Authorization") String token,
                                                                     @ModelAttribute RegisterDto registerDto){
        ApiResponse<RegisterDto> response = authFacade.registerUserFace(registerDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/clockInOut")
    public ResponseEntity<ApiResponse<ClockInOutRequestDto>> ClockINOutUser(@RequestHeader("Authorization") String token,
                                                                   @ModelAttribute ClockInOutRequestDto registerDto){
        ApiResponse<ClockInOutRequestDto> response = authFacade.clockInOutUser(registerDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/multiface/compare")
    public ResponseEntity<ApiResponse<RegisterDto>> compareMultiFace(@RequestHeader("Authorization") String token,
                                                                   @ModelAttribute FaceDto faceDto){
        ApiResponse<RegisterDto> response = authFacade.compareMultiFace(faceDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/validation")
    public ResponseEntity<ApiResponse<UserValidationDto>> validateUser(@RequestHeader("Authorization") String token,
                                                                     @RequestBody UserValidationDto request){
        ApiResponse<UserValidationDto> response = authFacade.validateUser(request.getUserId());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
