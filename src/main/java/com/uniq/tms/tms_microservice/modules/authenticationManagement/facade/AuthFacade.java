package com.uniq.tms.tms_microservice.modules.authenticationManagement.facade;

import com.uniq.tms.tms_microservice.modules.authenticationManagement.dto.EmailDto;
import com.uniq.tms.tms_microservice.modules.authenticationManagement.services.AuthService;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.ChangePasswordDto;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class AuthFacade {

    private final AuthService authService;
    private final AuthHelper authHelper;

    public AuthFacade(AuthService authService,
                       AuthHelper authHelper) {

        this.authService = authService;
        this.authHelper = authHelper;
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${csv.download.dir}")
    private String downloadDir;

    private final Logger log = LoggerFactory.getLogger(AuthFacade.class);

    public ResponseEntity<ApiResponse> handleLoginByEmail(String email, String password, HttpServletResponse
            response, HttpServletRequest request) {
        return authService.authenticateUserByEmail(email, password, response, request);
    }

    public ResponseEntity<ApiResponse> handleLogout(HttpServletRequest request, HttpServletResponse response) {
        return authService.logoutUser(request, response);
    }

    public ResponseEntity<ApiResponse> validateEmail(EmailDto email) {
            ResponseEntity<ApiResponse> response = authService.forgotPassword(email.getEmail());
            return ResponseEntity.ok(response.getBody());
    }

    public ResponseEntity<ApiResponse> resetPassword(String email, ChangePasswordDto request) {
            return authService.resetPassword(email, request);
    }

    public ResponseEntity<ApiResponse> authenticateUserByMobile(String mobile, String otp, HttpServletResponse
            response, HttpServletRequest request) {
        return authService.authenticateUserByMobile(mobile, otp, response, request);
    }

    public ResponseEntity<ApiResponse> sendOTP(String mobile) {
        return authService.sendOtp(mobile);
    }

    public ApiResponse<String> getSchema() {
        String orgSchema = authHelper.getSchema();
        if(orgSchema  != null) {
            return new ApiResponse<>(200, "Organization schema send successfully", orgSchema);
        }else{
            return new ApiResponse<>(404,"User Schema not found for this organization",null);
        }
    }

}
