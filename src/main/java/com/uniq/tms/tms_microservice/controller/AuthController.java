package com.uniq.tms.tms_microservice.controller;

import com.uniq.tms.tms_microservice.config.security.cache.OtpFallbackCache;
import com.uniq.tms.tms_microservice.config.security.jwt.JwtUtil;
import com.uniq.tms.tms_microservice.constant.UserConstant;
import com.uniq.tms.tms_microservice.dto.ApiResponse;
import com.uniq.tms.tms_microservice.dto.ChangePasswordDto;
import com.uniq.tms.tms_microservice.dto.EmailDto;
import com.uniq.tms.tms_microservice.dto.LoginDto;
import com.uniq.tms.tms_microservice.facade.AuthFacade;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping(UserConstant.Auth_Url)
public class AuthController {

    private static final Logger logger = LogManager.getLogger(AuthController.class);

    private final AuthFacade authFacade;
    private final JwtUtil jwtUtil;
    private final OtpFallbackCache otpFallbackCache;

    public AuthController(AuthFacade authFacade, JwtUtil jwtUtil, OtpFallbackCache otpFallbackCache) {
        this.authFacade = authFacade;
        this.jwtUtil = jwtUtil;
        this.otpFallbackCache = otpFallbackCache;
    }

    @PostMapping("/loginByEmail")
    public ResponseEntity<ApiResponse> loginByEmail(@RequestBody LoginDto loginDto,
                                                HttpServletResponse response,
                                                HttpServletRequest request) {


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
        return authFacade.handleLoginByEmail(loginDto.getEmail(), loginDto.getPassword(), response, request);
    }

    @GetMapping("/loginByMobile")
    public ResponseEntity<ApiResponse> loginByMobile(@RequestParam String mobile, @RequestParam String otp, HttpServletResponse response, HttpServletRequest request) {
        return authFacade.authenticateUserByMobile(mobile,otp,response,request);
    }

    @GetMapping("/sendOTP")
    public ResponseEntity<ApiResponse> sendOTP(@RequestParam String mobile) {
        ResponseEntity<ApiResponse> response = authFacade.sendOTP(mobile);
        return response;
    }

    @GetMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request, HttpServletResponse response ) {
        logger.info("Logout Request Received");
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

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            boolean isValid = jwtUtil.validateToken(token);

            if (isValid) {
                return ResponseEntity.ok(true);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token: " + e.getMessage());
        }
    }

    @GetMapping("/debug/otps")
    public Map<String, String> getAllOtps() {
        return otpFallbackCache.getAllEntries();
    }

    @GetMapping("/debug/otpsCount")
    public Map<String, String> getAllOtpsCount() {
        return otpFallbackCache.getAllOtpCounts();
    }

}
