package com.uniq.tms.tms_microservice.modules.timesheetManagement.services;

import com.uniq.tms.tms_microservice.modules.userManagement.dto.UserValidationDto;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.ClockInOutRequestDto;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.FaceDto;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.RegisterDto;

public interface FaceService {
    ApiResponse<RegisterDto> UserFaceRegister(RegisterDto registerDto, String orgSchema);

    ApiResponse<ClockInOutRequestDto> clockInOutUser(ClockInOutRequestDto registerDto, String orgSchema);

    ApiResponse<RegisterDto> compareMultiFace(FaceDto faceDto, String orgSchema,String UserIdFromToken);

    ApiResponse<UserValidationDto> validateUser(String userId);

}
