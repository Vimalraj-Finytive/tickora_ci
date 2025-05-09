package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.constant.PrivilegeConstant;
import com.uniq.tms.tms_microservice.entity.PrivilegeEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import jakarta.mail.internet.MimeMessage;
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

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final UserAdapter userAdapter;

    public EmailService(JavaMailSender javaMailSender, TemplateEngine templateEngine, UserAdapter userAdapter) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.userAdapter = userAdapter;
    }

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String username, String password, boolean isNewUser) {
        try{
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("password", password);
            context.setVariable("email", to);
            context.setVariable("isNewUser", isNewUser);
            String htmlContent = templateEngine.process("email-template", context);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);

            UserEntity user = userAdapter.findByEmail(to).orElse(null);
                List<String> privilegeNames = userAdapter.getRoleWithPrivileges(user.getRole().getRoleId()).getPrivilegeEntities().stream().map(PrivilegeEntity::getName).collect(Collectors.toList());

                boolean hasPrivilege_2 = privilegeNames.contains(PrivilegeConstant.Privilege_2);
                boolean hasPrivilege_1 = privilegeNames.contains(PrivilegeConstant.Privilege_1);

                if (hasPrivilege_2) helper.setText(htmlContent, true);
                else if (hasPrivilege_1) helper.setText("User your mobile number and OTP to login!");
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message" + e.getMessage());
        }
    }
}
