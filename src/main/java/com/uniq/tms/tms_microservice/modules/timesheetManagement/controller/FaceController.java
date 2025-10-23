package com.uniq.tms.tms_microservice.modules.timesheetManagement.controller;

import com.uniq.tms.tms_microservice.modules.userManagement.dto.UserValidationDto;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.constant.TimesheetConstant;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.ClockInOutRequestDto;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.FaceDto;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.facade.TimesheetFacade;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.RegisterDto;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(TimesheetConstant.FACE_URL)
public class FaceController{

    private final TimesheetFacade timesheetFacade;
    @Autowired
    private final AuthHelper authHelper;

    public FaceController(TimesheetFacade timesheetFacade, AuthHelper authHelper) {
        this.timesheetFacade = timesheetFacade;
        this.authHelper = authHelper;
    }


    @PostMapping(value = "/register")
    public ResponseEntity<ApiResponse<RegisterDto>> registerUserFace(@RequestHeader("Authorization") String token,
                                                                     @ModelAttribute RegisterDto registerDto){
        ApiResponse<RegisterDto> response =timesheetFacade.registerUserFace(registerDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/clockInOut")
    public ResponseEntity<ApiResponse<ClockInOutRequestDto>> ClockINOutUser(@RequestHeader("Authorization") String token,
                                                                            @ModelAttribute ClockInOutRequestDto registerDto){
        ApiResponse<ClockInOutRequestDto> response =timesheetFacade.clockInOutUser(registerDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/multiface/compare")
    public ResponseEntity<ApiResponse<RegisterDto>> compareMultiFace(@RequestHeader("Authorization") String token,
                                                                   @ModelAttribute FaceDto faceDto){

        String userIdFromToken = authHelper.getUserId();
        ApiResponse<RegisterDto> response =timesheetFacade.compareMultiFace(faceDto,userIdFromToken);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/validation")
    public ResponseEntity<ApiResponse<UserValidationDto>> validateUser(@RequestHeader("Authorization") String token,
                                                                       @RequestBody UserValidationDto request) {
        ApiResponse<UserValidationDto> response = timesheetFacade.validateUser(request.getUserId());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
