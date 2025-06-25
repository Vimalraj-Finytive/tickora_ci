package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.dto.PrivilegeConstants;
import com.uniq.tms.tms_microservice.entity.PrivilegeEntity;
import com.uniq.tms.tms_microservice.entity.RoleEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.repository.RoleRepository;
import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import com.uniq.tms.tms_microservice.util.CacheKeyUtil;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailService {

    private final Logger log = LogManager.getLogger(EmailService.class);
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final UserAdapter userAdapter;
    private final CacheLoaderService cacheLoaderService;
    private final CacheKeyUtil cacheKeyUtil;
    private final RoleRepository roleRepository;

    public EmailService(JavaMailSender javaMailSender, TemplateEngine templateEngine, UserAdapter userAdapter, CacheLoaderService cacheLoaderService, CacheKeyUtil cacheKeyUtil, RoleRepository roleRepository) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.userAdapter = userAdapter;
        this.cacheLoaderService = cacheLoaderService;
        this.cacheKeyUtil = cacheKeyUtil;
        this.roleRepository = roleRepository;
    }

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String username, String password, boolean isNewUser, String emailType) {
        try{
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("password", password);
            context.setVariable("email", to);
            context.setVariable("isNewUser", isNewUser);
            context.setVariable("emailType", emailType);
            String htmlContent = templateEngine.process("email-template", context);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);

            UserEntity user = userAdapter.findByEmail(to).orElse(null);
            log.info("user {}", user);
            List<String> privilegeNames = userAdapter.getRoleWithPrivileges(user.getRole().getRoleId()).getPrivilegeEntities().stream().map(PrivilegeEntity::getName).collect(Collectors.toList());
            RoleEntity role = roleRepository.findById(user.getRole().getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found with ID: " + user.getRole().getRoleId()));;
            log.info("roleName {}", role);
            Long roleId = user.getRole().getRoleId();
            log.info("roleId {}", roleId);
            String roleName = role.getName();
            log.info("Role name string : {}" ,roleName);
            String EmailLoginKey  = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.LOGIN_VIA_EMAIL);
            boolean emailLogin = cacheKeyUtil.roleHasPrivilege(String.valueOf(roleName), EmailLoginKey);
            String MobileLoginKey = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.LOGIN_VIA_MOBILE);
            boolean mobileLogin = cacheKeyUtil.roleHasPrivilege(String.valueOf(roleName), MobileLoginKey);

            log.info("Privileges of user {} are {}", String.valueOf(roleName), privilegeNames);
            log.info("emailLogin {} , mobileLogin {}", emailLogin, mobileLogin);
            if (emailLogin) {
                helper.setText(htmlContent, true);
            } else if (mobileLogin) {
                helper.setText("Use your mobile number and OTP to login!");
            } else {
                throw new RuntimeException("No email content template for user privileges");
            }
            javaMailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message to " + to +":"+ e.getMessage(),e );
        }
    }

    public void sendSuccessEmail(String toEmail, String subject, String username, int uploadCount, int skippedCount, String emailType) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("uploadcount", uploadCount);
            context.setVariable("skippedcount", skippedCount);
            context.setVariable("emailType", emailType);

            String htmlContent = templateEngine.process("email-template", context);
            helper.setTo(toEmail);
            helper.setFrom(fromEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send message to" + toEmail + ":" + e.getMessage(), e);
        }
    }
}
