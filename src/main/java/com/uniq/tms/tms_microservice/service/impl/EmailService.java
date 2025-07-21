package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.dto.PrivilegeConstants;
import com.uniq.tms.tms_microservice.entity.RoleEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.repository.RoleRepository;
import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import com.uniq.tms.tms_microservice.util.CacheKeyUtil;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    private final Logger log = LogManager.getLogger(EmailService.class);
    private final TemplateEngine templateEngine;
    private final UserAdapter userAdapter;
    private final CacheLoaderService cacheLoaderService;
    private final CacheKeyUtil cacheKeyUtil;
    private final RoleRepository roleRepository;
    private final RestTemplate restTemplate;

    @Value("${external.mail.service.url}")
    private String mailServiceUrl;

    public EmailService(TemplateEngine templateEngine,
                        UserAdapter userAdapter,
                        CacheLoaderService cacheLoaderService,
                        CacheKeyUtil cacheKeyUtil,
                        RoleRepository roleRepository,
                        @Nullable RestTemplate restTemplate) {
        this.templateEngine = templateEngine;
        this.userAdapter = userAdapter;
        this.cacheLoaderService = cacheLoaderService;
        this.cacheKeyUtil = cacheKeyUtil;
        this.roleRepository = roleRepository;
        this.restTemplate = restTemplate;
    }

    public void sendEmail(String to, String subject, String username, String password, boolean isNewUser, String emailType) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("password", password);
            context.setVariable("email", to);
            context.setVariable("isNewUser", isNewUser);
            context.setVariable("emailType", emailType);

            String htmlContent = templateEngine.process("email-template", context);

            UserEntity user = userAdapter.findByEmail(to).orElse(null);
            if (user == null) throw new RuntimeException("User not found with email: " + to);

            RoleEntity role = roleRepository.findById(user.getRole().getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found with ID: " + user.getRole().getRoleId()));
            String roleName = role.getName();

            String emailLoginKey = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.LOGIN_VIA_EMAIL);
            String mobileLoginKey = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.LOGIN_VIA_MOBILE);
            boolean emailLogin = cacheKeyUtil.roleHasPrivilege(roleName, emailLoginKey);
            boolean mobileLogin = cacheKeyUtil.roleHasPrivilege(roleName, mobileLoginKey);

            log.info("Role name: {}, emailLogin: {}, mobileLogin: {}", roleName, emailLogin, mobileLogin);

            String bodyContent = emailLogin ? htmlContent : "Use your mobile number and OTP to login!";

            // Prepare payload
            Map<String, String> payload = new HashMap<>();
            payload.put("to", to);
            payload.put("subject", subject);
            payload.put("body", bodyContent);

            // Make POST request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(mailServiceUrl, request, String.class);
            log.info("Mail sent to {}. Response: {}", to, response.getStatusCode());

        } catch (Exception e) {
            log.error("Failed to send message to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send message to " + to + ":" + e.getMessage(), e);
        }
    }

    public void sendSuccessEmail(String toEmail, String subject, String username, int uploadCount, int skippedCount, String emailType) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("uploadcount", uploadCount);
            context.setVariable("skippedcount", skippedCount);
            context.setVariable("emailType", emailType);

            String htmlContent = templateEngine.process("email-template", context);

            // Prepare payload
            Map<String, String> payload = new HashMap<>();
            payload.put("to", toEmail);
            payload.put("subject", subject);
            payload.put("body", htmlContent);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(mailServiceUrl, request, String.class);
            log.info("Success email sent to {}. Response: {}", toEmail, response.getStatusCode());

        } catch (Exception e) {
            log.error("Failed to send message to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send message to " + toEmail + ":" + e.getMessage(), e);
        }
    }
}
