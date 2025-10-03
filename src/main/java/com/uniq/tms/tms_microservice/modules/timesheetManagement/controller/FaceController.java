package com.uniq.tms.tms_microservice.modules.timesheetManagement.controller;

import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.constant.TimesheetConstant;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.ClockInOutRequestDto;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.FaceDto;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.facade.TimesheetFacade;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.RegisterDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(TimesheetConstant.FACE_URL)
public class FaceController{

    private final TimesheetFacade timesheetFacade;

    public FaceController(TimesheetFacade timesheetFacade) {
        this.timesheetFacade = timesheetFacade;
    }

    @PostMapping("/register")
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
        ApiResponse<RegisterDto> response =timesheetFacade.compareMultiFace(faceDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}
