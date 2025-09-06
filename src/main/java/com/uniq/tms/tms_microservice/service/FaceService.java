package com.uniq.tms.tms_microservice.service;

import com.uniq.tms.tms_microservice.dto.*;


public interface FaceService {
    ApiResponse<RegisterDto> UserFaceRegister(RegisterDto registerDto, String orgSchema);

    ApiResponse<ClockInOutRequestDto> clockInOutUser(ClockInOutRequestDto registerDto, String orgSchema);

    ApiResponse<RegisterDto> compareMultiFace(FaceDto faceDto, String orgSchema);
}
