package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.adapter.AuthAdapter;
import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.config.security.cache.OtpFallbackCache;
import com.uniq.tms.tms_microservice.config.security.jwt.JwtUtil;
import com.uniq.tms.tms_microservice.config.security.schema.TenantContext;
import com.uniq.tms.tms_microservice.dto.ApiResponse;
import com.uniq.tms.tms_microservice.dto.ChangePasswordDto;
import com.uniq.tms.tms_microservice.dto.OrganizationDropdownDto;
import com.uniq.tms.tms_microservice.enums.PrivilegeConstants;
import com.uniq.tms.tms_microservice.entity.SecondaryDetailsEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.enums.CountryEnum;
import com.uniq.tms.tms_microservice.enums.OrganizationSizeRangeEnum;
import com.uniq.tms.tms_microservice.exception.CommonExceptionHandler;
import com.uniq.tms.tms_microservice.helper.EmailHelper;
import com.uniq.tms.tms_microservice.helper.RolePrivilegeHelper;
import com.uniq.tms.tms_microservice.helper.TenantResolverHelper;
import com.uniq.tms.tms_microservice.model.OtpSendResponse;
import com.uniq.tms.tms_microservice.service.AuthService;
import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import com.uniq.tms.tms_microservice.service.NettyfishService;
import com.uniq.tms.tms_microservice.util.CacheKeyUtil;
import com.uniq.tms.tms_microservice.util.PasswordUtil;
import com.uniq.tms.tms_microservice.util.TenantUtil;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final NettyfishService nettyfishService;
    private final AuthAdapter authAdapter;
    private final UserAdapter userAdapter;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailHelper emailHelper;
    private final CacheLoaderService cacheLoaderService;
    private final CacheKeyUtil cacheKeyUtil;
    private final StringRedisTemplate redisTemplate;
    private final OtpFallbackCache otpFallbackCache;
    private final TenantResolverHelper tenantResolverHelper;
    private final RolePrivilegeHelper rolePrivilegeHelper;

    public AuthServiceImpl(NettyfishService nettyfishService, AuthAdapter authAdapter, UserAdapter userAdapter, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, EmailHelper emailHelper, CacheLoaderService cacheLoaderService, CacheKeyUtil cacheKeyUtil, @Nullable StringRedisTemplate redisTemplate, OtpFallbackCache otpFallbackCache, TenantResolverHelper tenantResolverHelper, RolePrivilegeHelper rolePrivilegeHelper) {
        this.nettyfishService = nettyfishService;
        this.authAdapter = authAdapter;
        this.userAdapter = userAdapter;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.emailHelper = emailHelper;
        this.cacheLoaderService = cacheLoaderService;
        this.cacheKeyUtil = cacheKeyUtil;
        this.redisTemplate = redisTemplate;
        this.otpFallbackCache = otpFallbackCache;
        this.tenantResolverHelper = tenantResolverHelper;
        this.rolePrivilegeHelper = rolePrivilegeHelper;
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${otp.ttl.minutes}")
    private long otpTtlMinutes;

    @Value("${otp.max.daily.attempts}")
    private int maxDailyAttempts;

    @Value("#{'${otp.test.mobiles}'.split(',')}")
    private List<String> testMobiles;

    @Value("${otp.test}")
    private String testOtp;

    @Override
    @Transactional
    public ResponseEntity<ApiResponse> authenticateUserByEmail(String email, String password,
                                                               HttpServletResponse response,
                                                               HttpServletRequest request) {
        String schemaName = tenantResolverHelper.getUserSchemaByEmail(email);
        log.info("schema name from login:{}", schemaName);
        List<Supplier<UserEntity>> userSuppliers = List.of(
                () -> authAdapter.findByEmail(email),
                () -> authAdapter.findUserByEmail(email)
        );
        log.info("fetch user: {}", userSuppliers);
        List<UserEntity> users = userSuppliers.stream()
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .toList();
        log.info("Streamed users: {}", users);

        if (users.isEmpty()) {
            log.info("user not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(401, "Invalid credentials: User not found", null));
        }

        String key = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.LOGIN_VIA_EMAIL);
        log.info("Privilege key :{}", key);
        log.info("User Role:{}", users.getFirst().getRole().getName());
        boolean hasEmailLoginPrivilege = rolePrivilegeHelper.roleHasPrivilege(users.getFirst().getRole().getName(), key);
        log.info("has email login privilege: {}", hasEmailLoginPrivilege);

        Optional<UserEntity> userWithEmailPrivilege = users.stream()
                .filter(user -> user.getRole().getPrivilegeMappings().stream()
                        .map(mapping -> mapping.getPrivilege().getName())
                        .anyMatch(priv -> priv.equals(key)))
                .findFirst();

        log.info("user with email login privilege: {}", userWithEmailPrivilege);

        if (userWithEmailPrivilege.isEmpty()) {
            log.info("user with email login privilege not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(401, "You are not allowed to login using email.", null));
        }

        UserEntity user = userWithEmailPrivilege.get();
        if(!user.isActive()){
            log.info("User is Inactive");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(401, "Your account is inactive",null));
        }

        boolean isPasswordValid = PasswordUtil.isPasswordMatch(password, user.getPassword());
        log.info("password valid: {}", isPasswordValid);
        if (!isPasswordValid) {
            log.info("password not valid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(401, "Invalid Credentials: Wrong password", null));
        }

        Map<String, Object> userData = new HashMap<>();
        if (user.isDefaultPassword()) {
            log.info("Login with default password is not allowed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(401, "You must change your default password through email before login.", null));
        }

            log.info("Generating JWT Token");
            String jwtToken = jwtUtil.generateToken(email, System.currentTimeMillis(), request, hasEmailLoginPrivilege, schemaName);
            Cookie jwtCookie = new Cookie("JWT_TOKEN", jwtToken);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(true);
            jwtCookie.setPath("/");
            response.addCookie(jwtCookie);

            log.info("Preparing privilege set");
            Set<String> privileges = user.getRole().getPrivilegeMappings().stream()
                    .map(mapping -> mapping.getPrivilege().getName())
                    .collect(Collectors.toSet());

            userData.put("username", user.getUserName());
            userData.put("role", user.getRole().getName());
            userData.put("JWT_TOKEN", jwtToken);
            userData.put("userId", user.getUserId());
            userData.put("isRegisterUser", user.isRegisterUser());
            userData.put("privilage", privileges);
            return ResponseEntity.ok(new ApiResponse(200, "Login Successful", userData));
    }

    @Override
    public ResponseEntity<ApiResponse> sendOtp(String mobile) {
        String schema = TenantUtil.getCurrentTenant();
        log.info("Current send otp tenant:{}", schema);
        UserEntity studentUser;
        String parentUserId = null;
        boolean isParent = false;

        studentUser = authAdapter.findByMobileNumber(mobile);
        if (studentUser == null) {
            studentUser = authAdapter.findStudentIdByMobile(mobile);
            if (studentUser != null) {
                Optional<SecondaryDetailsEntity> parentEntity = authAdapter.findParentByMobile(mobile);
                parentUserId = parentEntity.map(SecondaryDetailsEntity::getId).orElse(null);
                isParent = true;
            }
        }

        if (studentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(401, "Invalid credentials: User not found", null));
        }

        if (!isParent) {
            String key = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.LOGIN_VIA_MOBILE);
            log.info("student role :{}", studentUser.getRole().getName());
            log.info("Privilege Key : {}", key);
            boolean hasMobileLoginPrivilege = rolePrivilegeHelper.roleHasPrivilege(studentUser.getRole().getName(), key);
            log.info("has mobile login privilege: {}", hasMobileLoginPrivilege);
            if (!hasMobileLoginPrivilege) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(401, "You are not allowed to login using mobile.", null));
            }
        } else {
            log.info("Parent login detected. Skipping privilege check.");
        }


        Map<String, Object> response = new HashMap<>();
        response.put("count", 1);
        response.put("time", "1M 6S");

        List<Map<String, Object>> dataList = new ArrayList<>();
        dataList.add(response);

        String userId = studentUser.getUserId();
        String orgId = studentUser.getOrganizationId();
        String otpCountKey;
        if (!isParent) {
            otpCountKey = cacheKeyUtil.getOtpCountKey(orgId, userId,schema);
        } else {
            otpCountKey = cacheKeyUtil.getOtpCountKey(orgId, parentUserId,schema);
        }
        String otpKey = cacheKeyUtil.getOtpKey(orgId, mobile,schema);

        Instant now = Instant.now();
        Instant midnight = LocalDate.now(ZoneOffset.UTC).plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        long secondsUntilMidnight = Duration.between(now, midnight).getSeconds();
        String generatedOtp = testMobiles.contains(mobile.trim()) ? testOtp : nettyfishService.generateOtp();
        log.info("Generated OTP:{}", generatedOtp);
        Integer currentCount = otpFallbackCache.getCount(otpCountKey);
        if (!(testMobiles.contains(mobile) && testOtp.equals(generatedOtp))) {
            if (currentCount != null && currentCount >= maxDailyAttempts) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(new ApiResponse(429, "Daily OTP limit reached, Try again Tomorrow.", response));
            }
        }
        OtpSendResponse otpSendResponse = testMobiles.contains(mobile)
                ? new OtpSendResponse(true, "Test OTP sent successfully")
                : nettyfishService.sendOtp(mobile, generatedOtp);
        if (!otpSendResponse.isSuccess()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(500, "Failed to send OTP: " + otpSendResponse, null));
        }
        otpFallbackCache.saveOtp(otpKey, generatedOtp, otpTtlMinutes);
        otpFallbackCache.incrementCount(otpCountKey, secondsUntilMidnight);
        log.info("Generated OTP for {}: {}", mobile, generatedOtp);
        return ResponseEntity.ok(new ApiResponse(200, "OTP sent successfully", dataList));
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse> authenticateUserByMobile(String mobile, String otp,
                                                                HttpServletResponse response,
                                                                HttpServletRequest request) {
        String schemaName = tenantResolverHelper.getUserSchemaByMobile(mobile);
        List<Supplier<UserEntity>> userSuppliers = List.of(
                () -> authAdapter.findByMobileNumber(mobile),
                () -> authAdapter.findStudentIdByMobile(mobile)
        );

        List<UserEntity> users = userSuppliers.stream()
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .toList();

        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(401, "Invalid credentials: User not found", null));
        }

        String key = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.LOGIN_VIA_MOBILE);
        boolean hasMobileLoginPrivilege = rolePrivilegeHelper
                .roleHasPrivilege(users.getFirst().getRole().getName(), key);
        log.info("has mobile login privilege to login: {}", hasMobileLoginPrivilege);

        Optional<UserEntity> userWithMobilePrivilege = users.stream()
                .filter(user -> user.getRole().getPrivilegeMappings().stream()
                        .map(mapping -> mapping.getPrivilege().getName())
                        .anyMatch(priv -> priv.equals(key)))
                .findFirst();

        if (userWithMobilePrivilege.isEmpty()) {
            log.info("user with mobile login privilege not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(401, "You are not allowed to login using mobile.", null));
        }

        UserEntity user = users.getFirst();
        String userId = user.getUserId();
        String otpKey = cacheKeyUtil.getOtpKey(user.getOrganizationId(), mobile,schemaName);
        if (otpKey == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(400, "OTP expired. Please request a new OTP.", null));
        }

        String otpNumber = otpFallbackCache.getOtp(otpKey);
        log.info("OTP retrieved from fallback cache: {}", otpNumber);

        log.info("OTP (retrieved): {}", otpNumber);
        log.info("UserId: {}", userId);

        if (otpNumber == null || userId == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(400, "OTP expired or not found for user.", null));
        }

        if (!otp.equals(otpNumber)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(400, "Invalid OTP", null));
        }

        String jwtToken = jwtUtil.generateToken(mobile, System.currentTimeMillis(), request, false, schemaName);
        Cookie jwtCookie = new Cookie("JWT_TOKEN", jwtToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        response.addCookie(jwtCookie);

        Set<String> privileges = user.getRole().getPrivilegeMappings().stream()
                .map(mapping -> mapping.getPrivilege().getName())
                .collect(Collectors.toSet());

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", user.getUserName());
        userData.put("role", user.getRole().getName());
        userData.put("JWT_TOKEN", jwtToken);
        userData.put("userId", user.getUserId());
        userData.put("privilage", privileges);

        otpFallbackCache.clearOtp(otpKey);

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
            log.info("Blacklisting JWT Token");
            jwtUtil.blacklistToken(jwtToken);

            // Clear JWT cookie
            Cookie jwtCookie = new Cookie("JWT_TOKEN", "");
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(0);
            jwtCookie.setSecure(true);
            jwtCookie.setAttribute("SameSite", "None");
            response.addCookie(jwtCookie);

            return ResponseEntity.ok(new ApiResponse(200, "Logout Successful", null));
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(500, "Error during logout", null));
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<ApiResponse> resetPassword(String email, ChangePasswordDto request) {
        log.info("About to reset password for tenant: {}", TenantContext.getCurrentTenant());

        log.info("Reset password operation completed for tenant: {}", TenantContext.getCurrentTenant());

        UserEntity user = userAdapter.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Email"));
        log.info("Check password");
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.info("Incorrect password");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            log.info("Correct password");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different from the old password");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setDefaultPassword(false);
        userAdapter.updatePassword(user);
        log.info("Update password success");
        return ResponseEntity.ok(new ApiResponse(201, "Password reset successfully", true));
    }

    public ResponseEntity<ApiResponse> forgotPassword(String email) {
        try {
            UserEntity user = userAdapter.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Email"));
            String defaultPassword = PasswordUtil.generateDefaultPassword();
            user.setPassword(PasswordUtil.encryptPassword(defaultPassword));
            user.setDefaultPassword(true);
            userAdapter.saveUser(user);
            boolean isNewUser = false;
            emailHelper.sendForgotPasswordReminderEmail(user.getEmail(), user.getUserName(), defaultPassword, isNewUser);
            return ResponseEntity.ok(new ApiResponse(200, "New default password sent to email", null));
        } catch (Exception e) {
            log.info("Invalid user: {}", e.getMessage());
            throw new CommonExceptionHandler.SchemaNotFoundException("Invalid User Email");
        }
    }

    @Override
    public OrganizationDropdownDto getDropDowns() {

        List<Map<String, Object>> orgSize = Arrays.stream(OrganizationSizeRangeEnum.values())
                .map(size -> Map.<String, Object>of(
                        "displayValue", size.getDisplayValue(),
                        "maxCount", size.getMaxCount()
                ))
                .toList();

        List<String> country = Arrays.stream(CountryEnum.values())
                .map(CountryEnum::getCounty)
                .toList();

        return new OrganizationDropdownDto(country, orgSize);
    }
}
