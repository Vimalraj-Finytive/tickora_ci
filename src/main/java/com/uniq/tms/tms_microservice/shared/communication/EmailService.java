package com.uniq.tms.tms_microservice.shared.communication;

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
    private final RestTemplate restTemplate;

    @Value("${external.mail.service.url}")
    private String mailServiceUrl;

    public EmailService(TemplateEngine templateEngine,
                        @Nullable RestTemplate restTemplate) {
        this.templateEngine = templateEngine;
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

            Map<String, String> payload = new HashMap<>();
            payload.put("to", to);
            payload.put("subject", subject);
            payload.put("body", htmlContent);

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
