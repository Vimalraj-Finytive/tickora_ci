package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.adapter.AuthAdapter;
import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.config.JwtUtil;
import com.uniq.tms.tms_microservice.dto.ApiResponse;
import com.uniq.tms.tms_microservice.dto.ChangePasswordDto;
import com.uniq.tms.tms_microservice.dto.EmailDto;
import com.uniq.tms.tms_microservice.entity.PrivilegeEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.service.AuthService;
import com.uniq.tms.tms_microservice.util.EmailUtil;
import com.uniq.tms.tms_microservice.util.PasswordUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {
    private final AuthAdapter authAdapter;
    private final UserAdapter userAdapter;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EmailUtil emailUtil;

    public AuthServiceImpl(AuthAdapter authAdapter, UserAdapter userAdapter, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, EmailService emailService, EmailUtil emailUtil) {
        this.authAdapter = authAdapter;
        this.userAdapter = userAdapter;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.emailUtil = emailUtil;
    }

    @Override
    public ResponseEntity<ApiResponse> authenticateUser(String email, String password, HttpServletResponse response, HttpServletRequest request) {

        UserEntity userEntity = authAdapter.findByEmail(email);

        if (userEntity == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(401, "Invalid Credentials: User not found", null));
        }

        boolean isPasswordValid = PasswordUtil.isPasswordMatch(password, userEntity.getPassword());

        if (!isPasswordValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(401, "Invalid Credentials: Wrong password", null));
        }

        if (userEntity.isDefaultPassword()) {
            boolean isNewUser = userEntity.isDefaultPassword();
            emailUtil.sendDefaultPasswordReminderEmail(userEntity.getEmail(), userEntity.getUserName(), password, isNewUser);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(401, "You must change your default password before logging in.", null));
        }

        String jwtToken = jwtUtil.generateToken(email, System.currentTimeMillis(), request);
        Cookie jwtCookie = new Cookie("JWT_TOKEN", jwtToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        response.addCookie(jwtCookie);

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", userEntity.getUserName());
        userData.put("role", userEntity.getRole().getName());
        userData.put("JWT_TOKEN", jwtToken);
        userData.put("userId", userEntity.getUserId());
        userData.put("isRegisterUser", userEntity.isRegisterUser());
        Set<String> privilage = userEntity.getRole().getPrivilegeEntities()
                .stream().map(PrivilegeEntity::getName).collect(Collectors.toSet());
        userData.put("privilege", privilage);
        return ResponseEntity.ok(new ApiResponse(200, "Login Successful", userData));
    }

    public ResponseEntity<ApiResponse> logoutUser(HttpServletRequest request, HttpServletResponse response) {
        String jwtToken = null;

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
        }

        if (jwtToken == null) {
            jwtToken = jwtUtil.extractJwtFromCookies(request);
        }

        if (jwtToken == null || jwtToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(400, "JWT Token not found", null));
        }

        try {
            String username = jwtUtil.extractUsername(jwtToken);
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(401, "Invalid JWT Token", null));
            }

            jwtUtil.blacklistToken(jwtToken, request.getHeader("User-Agent"), request.getRemoteAddr());

            Cookie jwtCookie = new Cookie("JWT_TOKEN", "");
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(0);
            jwtCookie.setSecure(false);
            jwtCookie.setAttribute("SameSite", "None");
            response.addCookie(jwtCookie);

            return ResponseEntity.ok(new ApiResponse(200, "Logout Successful", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(500, "Error during logout", null));
        }
    }

    @Override
    public UserEntity validateEmailDto(EmailDto email) {
        return authAdapter.findUserByEmail(email.getEmail());
    }

    @Override
    public ResponseEntity<ApiResponse> resetPassword(String email, ChangePasswordDto request) {

        UserEntity user = userAdapter.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different from the old password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setDefaultPassword(false);
        userAdapter.updatePassword(user);

        return ResponseEntity.ok(new ApiResponse(201,"Password reset successfully", true));
    }

    public ResponseEntity<ApiResponse> forgotPassword(String email) {
        UserEntity user = userAdapter.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (Boolean.TRUE.equals(user.isDefaultPassword())) {
            return ResponseEntity.ok(new ApiResponse(200, "User is already using default password", null));
        }

        String defaultPassword = PasswordUtil.generateDefaultPassword();
        user.setPassword(PasswordUtil.encryptPassword(defaultPassword));
        user.setDefaultPassword(true);
        userAdapter.saveUser(user);
        boolean isNewUser = false;
        emailUtil.sendForgotPasswordReminderEmail(user.getEmail(), user.getUserName(), defaultPassword, isNewUser);
        return ResponseEntity.ok(new ApiResponse(200, "New default password sent to email", null));
    }
}
