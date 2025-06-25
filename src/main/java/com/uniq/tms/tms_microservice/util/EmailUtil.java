package com.uniq.tms.tms_microservice.util;

import com.uniq.tms.tms_microservice.service.impl.EmailService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class EmailUtil {

    private final Logger log = LogManager.getLogger(EmailUtil.class);
    private final EmailService emailService;

    public EmailUtil(EmailService emailService) {
        this.emailService = emailService;
    }

    public void sendAccountCreationEmail(String toEmail, String userName, String Password, boolean isNewUser) {
        String emailSubject = "Account Created Successfully";
        String emailType = "account_creation";
        emailService.sendEmail(toEmail, emailSubject, userName, Password,isNewUser, emailType);
    }

    public void sendForgotPasswordReminderEmail(String toEmail, String userName, String defaultPassword, boolean isNewUser) {
        String subject = "Reminder: Change Your Password Using this default password";
        String emailType = "forgot_password";
        emailService.sendEmail(toEmail, subject, userName, defaultPassword, isNewUser, emailType);
    }

    public void sendSuccessEmail(String toEmail, String userName, int uploadedCount, int skippedCount) {
        String subject = "Bulk Upload Summary Notification";
        String emailType = "success_mail";
        emailService.sendSuccessEmail(toEmail,subject,userName,uploadedCount,skippedCount,emailType);
    }
}
