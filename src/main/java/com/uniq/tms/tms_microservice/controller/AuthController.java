package com.uniq.tms.tms_microservice.controller;

import com.uniq.tms.tms_microservice.constant.UserConstant;
import com.uniq.tms.tms_microservice.dto.ApiResponse;
import com.uniq.tms.tms_microservice.dto.ChangePasswordDto;
import com.uniq.tms.tms_microservice.dto.EmailDto;
import com.uniq.tms.tms_microservice.dto.LoginDto;
import com.uniq.tms.tms_microservice.facade.AuthFacade;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserConstant.Auth_Url)
public class AuthController {
    private final AuthFacade authFacade;

    public AuthController(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginDto loginDto,
                                                HttpServletResponse response,
                                                HttpServletRequest request) {
        Logger logger = LoggerFactory.getLogger(getClass());

        logger.info("Login Request Received: {}", loginDto);

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            logger.warn("Removing old token from request: {}", authHeader);

            response.setHeader("Authorization", "");
        }
        if (loginDto.getEmail() == null || loginDto.getPassword() == null) {
            logger.warn("Missing Email or Password in Login Request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(400, "Email and Password are required", null));
        }

        return authFacade.handleLogin(loginDto.getEmail(), loginDto.getPassword(), response, request);
    }


    @GetMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request, HttpServletResponse response ) {
        return authFacade.handleLogout(request, response);
    }

    @PostMapping("/validate-email")
    public ResponseEntity<ApiResponse> validateEmail(@RequestBody EmailDto email) {
        return authFacade.validateEmail(email);
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestParam String email,
                                                     @Valid @RequestBody ChangePasswordDto request) {
        return authFacade.resetPassword(email, request);
    }

}
