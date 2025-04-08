package com.uniq.tms.tms_microservice.util;

import com.uniq.tms.tms_microservice.service.impl.EmailService;
import org.springframework.stereotype.Component;

@Component
public class EmailUtil {

    private final EmailService emailService;


    public EmailUtil(EmailService emailService) {
        this.emailService = emailService;
    }

    public void sendAccountCreationEmail(String toEmail, String userName, String Password, boolean isNewUser) {
        String emailSubject = "Account Created Successfully";
        emailService.sendEmail(toEmail, emailSubject, userName, Password,isNewUser);
    }

    public void sendDefaultPasswordReminderEmail(String toEmail, String userName, String password, boolean isNewUser) {
        String subject = "Reminder: Change Your Default Password";
        String body = "You are still using the default password. Please reset it immediately for security reasons.";
        emailService.sendEmail(toEmail, subject, userName, password, isNewUser);
    }

    public void sendForgotPasswordReminderEmail(String toEmail, String userName, String defaultPassword, boolean isNewUser) {
        String subject = "Reminder: Change Your Password Using this default password";
        String body = "Reset your password by using this default password.";
        emailService.sendEmail(toEmail, subject, userName, defaultPassword, isNewUser);
    }



}
