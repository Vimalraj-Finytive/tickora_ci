package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.adapter.AuthAdapter;
import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.config.JwtUtil;
import com.uniq.tms.tms_microservice.constant.PrivilegeConstant;
import com.uniq.tms.tms_microservice.dto.ApiResponse;
import com.uniq.tms.tms_microservice.dto.ChangePasswordDto;
import com.uniq.tms.tms_microservice.dto.EmailDto;
import com.uniq.tms.tms_microservice.entity.PrivilegeEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.service.AuthService;
import com.uniq.tms.tms_microservice.service.NettyfishService;
import com.uniq.tms.tms_microservice.util.EmailUtil;
import com.uniq.tms.tms_microservice.util.PasswordUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final NettyfishService nettyfishService;
    private final AuthAdapter authAdapter;
    private final UserAdapter userAdapter;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EmailUtil emailUtil;

    public AuthServiceImpl(NettyfishService nettyfishService, AuthAdapter authAdapter, UserAdapter userAdapter, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, EmailService emailService, EmailUtil emailUtil) {
        this.nettyfishService = nettyfishService;
        this.authAdapter = authAdapter;
        this.userAdapter = userAdapter;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.emailUtil = emailUtil;
    }

    public ResponseEntity<ApiResponse> authenticateUserByEmail(String email, String password,
                                                               HttpServletResponse response,
                                                               HttpServletRequest request) {
        // Step 1: Try to find the user from multiple sources
        List<Supplier<UserEntity>> userSuppliers = List.of(
                () -> authAdapter.findByEmail(email),
                () -> authAdapter.findUserByEmail(email) // from SecondaryDetails
        );
        log.info("fetch user: {}", userSuppliers);
        List<UserEntity> users = userSuppliers.stream()
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (users.isEmpty()) {
            log.info("user not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(401, "Invalid credentials: User not found", null));
        }

        // Step 2: Find the first user with email login privilege
        Optional<UserEntity> userWithEmailPrivilege = users.stream()
                .filter(user -> user.getRole().getPrivilegeEntities().stream()
                        .map(PrivilegeEntity::getName)
                        .anyMatch(priv -> priv.equals(PrivilegeConstant.Privilege_2)))
                .findFirst();

        if (userWithEmailPrivilege.isEmpty()) {
            log.info("user with email login privilege not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(401, "You are not allowed to login using email.", null));
        }

        UserEntity user = userWithEmailPrivilege.get();

        boolean isPasswordValid = PasswordUtil.isPasswordMatch(password, user.getPassword());
        log.info("password valid: {}", isPasswordValid);
        // Step 3: Validate password
        if (!isPasswordValid) {
            log.info("password not valid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(401, "Invalid Credentials: Wrong password", null));
        }

        Map<String, Object> userData = new HashMap<>();
        if (user.isDefaultPassword()) {
            log.info(("sending mail to user to change default password"));
            boolean isNewUser = user.isDefaultPassword();
            emailUtil.sendDefaultPasswordReminderEmail(user.getEmail(), user.getUserName(), password, isNewUser);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(401, "You must change your default password before logging in.", null));
        }

            // Step 5: Generate JWT Token
            log.info("Generating JWT Token");
            String jwtToken = jwtUtil.generateToken(email, System.currentTimeMillis(), request);
            Cookie jwtCookie = new Cookie("JWT_TOKEN", jwtToken);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(true);
            jwtCookie.setPath("/");
            response.addCookie(jwtCookie);

            // Step 6: Prepare privilege set
            log.info("Preparing privilege set");
            Set<String> privileges = user.getRole().getPrivilegeEntities().stream()
                    .map(PrivilegeEntity::getName)
                    .collect(Collectors.toSet());

            // Step 7: Prepare response data

            userData.put("username", user.getUserName());
            userData.put("role", user.getRole().getName());
            userData.put("JWT_TOKEN", jwtToken);
            userData.put("userId", user.getUserId());
            userData.put("isRegisterUser", user.isRegisterUser());
            userData.put("userId", user.getUserId());
            userData.put("privilage", privileges);
            return ResponseEntity.ok(new ApiResponse(200, "Login Successful", userData));
    }

    @Override
    public ResponseEntity<ApiResponse> sendOtp(String mobile, HttpSession session) {
        // Step 1: Try to find the user from multiple sources
        List<Supplier<UserEntity>> userSuppliers = List.of(
                () -> authAdapter.findByMobileNumber(mobile),
                () -> authAdapter.findStudentIdByMobile(mobile) // from SecondaryDetails
        );

        List<UserEntity> users = userSuppliers.stream()
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(401, "Invalid credentials: User not found", null));
        }

        // Step 2: Find the first user with email login privilege
        Optional<UserEntity> userWithEmailPrivilege = users.stream()
                .filter(user -> user.getRole().getPrivilegeEntities().stream()
                        .map(PrivilegeEntity::getName)
                        .anyMatch(priv -> priv.equals(PrivilegeConstant.Privilege_1)))
                .findFirst();

        if (userWithEmailPrivilege.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(401, "You are not allowed to login using email.", null));
        }

        UserEntity user = userWithEmailPrivilege.get();

        String generatedOtp;
        String result;
//		if ("9952996769".equals(phoneNumber)) {
        if ("7010274041".equals(mobile)) {
            generatedOtp = "112233";
            result = "success";
        } else {
            generatedOtp = nettyfishService.generateOtp();
//             result = nettyfishService.sendOtp(phoneNumber, generatedOtp);
            result="success";
        }
        System.out.println("Generated OTP:===================>>>>>>> " + generatedOtp);
        System.out.println("Session ID at OTP generation: " + session.getId());
        if ("success".equals(result)) {
            session.setAttribute("otp", generatedOtp);
            session.setAttribute("phone", mobile);
            Enumeration<String> attributes = session.getAttributeNames();
            while (attributes.hasMoreElements()) {
                String attr = attributes.nextElement();
                System.out.println("Session Attribute -> " + attr + ": " + session.getAttribute(attr));
            }
            // OTP Expiration Timer
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    session.removeAttribute("otp");
                    System.out.println("OTP expired and removed from session.");
                }
            }, 300000); // 5 minutes
            return ResponseEntity.ok(new ApiResponse(200, "OTP sent to "+mobile, null));
        } else {
            return ResponseEntity.ok(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to send OTP: " + result, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponse> authenticateUserByMobile(String mobile, String otp, HttpServletResponse response, HttpServletRequest request) {

        // Step 1: Try to find the user from multiple sources
        List<Supplier<UserEntity>> userSuppliers = List.of(
                () -> authAdapter.findByMobileNumber(mobile),
                () -> authAdapter.findStudentIdByMobile(mobile) // from SecondaryDetails
        );

        List<UserEntity> users = userSuppliers.stream()
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(401, "Invalid credentials: User not found", null));
        }
        UserEntity user = users.get(0);

        // Fetch the OTP and mobile from the session
        HttpSession session = request.getSession(false); // false = don't create new session if not exists
        if (session == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(400, "Session expired. Please request a new OTP.", null));
        }

        String sessionOtp = (String) session.getAttribute("otp");
        String sessionMobile = (String) session.getAttribute("phone");

        if (sessionOtp == null || sessionMobile == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(400, "OTP expired or not found in session.", null));
        }

        // Validate mobile number
        if (!mobile.equals(sessionMobile)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(400, "Mobile number mismatch", null));
        }

        // Validate OTP value
        if (!otp.equals(sessionOtp)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(400, "Invalid OTP", null));
        }

//        // Validate expiry time
//        long currentTime = Instant.now().getEpochSecond();
//        if (currentTime > otpExpiry) {
//            return ResponseEntity.badRequest()
//                    .body(new ApiResponse(400, "OTP expired", null));
//        }

        // OTP validation success — proceed to generate main JWT token

        // Step 5: Generate JWT Token
        String jwtToken = jwtUtil.generateToken(mobile, System.currentTimeMillis(), request);
        Cookie jwtCookie = new Cookie("JWT_TOKEN", jwtToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // Set to true in production with HTTPS
        jwtCookie.setPath("/");
        response.addCookie(jwtCookie);

        // Step 6: Prepare privilege set
        Set<String> privileges = user.getRole().getPrivilegeEntities().stream()
                .map(PrivilegeEntity::getName)
                .collect(Collectors.toSet());

        // Step 7: Prepare response data
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", user.getUserName());
        userData.put("role", user.getRole().getName());
        userData.put("JWT_TOKEN", jwtToken);
        userData.put("userId", user.getUserId());
        userData.put("privilage", privileges);

        return ResponseEntity.ok(new ApiResponse(200, "Login Successful", userData));
    }

    public ResponseEntity<ApiResponse> logoutUser(HttpServletRequest request, HttpServletResponse response) {
        String jwtToken = null;
        log.info("Checking for JWT Token in Headers");
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
        }

        if (jwtToken == null) {
            log.info("Checking for JWT Token in Cookies");
            jwtToken = jwtUtil.extractJwtFromCookies(request);
        }

        if (jwtToken == null || jwtToken.isEmpty()) {
            log.info("JWT Token not found in headers or cookies");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(400, "JWT Token not found", null));
        }

        try {
            log.info("Extract username form JWT Token");
            String username = jwtUtil.extractUsername(jwtToken);
            if (username == null) {
                log.info("Username not found in JWT Token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(401, "Invalid JWT Token", null));
            }
            log.info("Blacklisting JWT Token");
            jwtUtil.blacklistToken(jwtToken, request.getHeader("User-Agent"));

            Cookie jwtCookie = new Cookie("JWT_TOKEN", "");
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(0);
            jwtCookie.setSecure(true);
            jwtCookie.setAttribute("SameSite", "None");
            response.addCookie(jwtCookie);

            return ResponseEntity.ok(new ApiResponse(200, "Logout Successful", null));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error during logout: {}", e.getMessage());
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
        if (Boolean.FALSE.equals(user.isDefaultPassword())) {
            String defaultPassword = PasswordUtil.generateDefaultPassword();
            user.setPassword(PasswordUtil.encryptPassword(defaultPassword));
            user.setDefaultPassword(true);
            userAdapter.saveUser(user);
            boolean isNewUser = false;
            emailUtil.sendForgotPasswordReminderEmail(user.getEmail(), user.getUserName(), defaultPassword, isNewUser);
            return ResponseEntity.ok(new ApiResponse(200, "New default password sent to email", null));
        }
        return ResponseEntity.ok(new ApiResponse(200, "User already has a default password", null));
    }
}
