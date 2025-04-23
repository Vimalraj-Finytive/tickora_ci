package com.uniq.tms.tms_microservice.service;

import com.uniq.tms.tms_microservice.dto.ApiResponse;
import com.uniq.tms.tms_microservice.dto.ChangePasswordDto;
import com.uniq.tms.tms_microservice.dto.EmailDto;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    ResponseEntity<ApiResponse> authenticateUser(String email, String password, HttpServletResponse response, HttpServletRequest request);

    ResponseEntity<ApiResponse> logoutUser(HttpServletRequest request, HttpServletResponse response);

    UserEntity validateEmailDto(EmailDto email);

    ResponseEntity<ApiResponse> resetPassword(String email, ChangePasswordDto request);

    ResponseEntity<ApiResponse> forgotPassword(String email);
}
