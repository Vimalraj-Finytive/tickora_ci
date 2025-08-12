package com.uniq.tms.tms_microservice.service;

import com.uniq.tms.tms_microservice.dto.ApiResponse;
import com.uniq.tms.tms_microservice.dto.ChangePasswordDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    ResponseEntity<ApiResponse> logoutUser(HttpServletRequest request, HttpServletResponse response);

    public ResponseEntity<ApiResponse> authenticateUserByEmail(String email, String password,
                                                               HttpServletResponse response,
                                                               HttpServletRequest request);
    ResponseEntity<ApiResponse> authenticateUserByMobile(String mobile, String otp, HttpServletResponse response, HttpServletRequest request);

    ResponseEntity<ApiResponse> resetPassword(String email, ChangePasswordDto request);

    ResponseEntity<ApiResponse> forgotPassword(String email);

    ResponseEntity<ApiResponse> sendOtp(String mobile);
}
