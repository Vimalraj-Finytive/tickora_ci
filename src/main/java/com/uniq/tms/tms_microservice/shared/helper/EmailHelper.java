package com.uniq.tms.tms_microservice.shared.helper;

import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.RoleEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.OrganizationCacheService;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.PrivilegeConstants;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.RoleRepository;
import com.uniq.tms.tms_microservice.shared.communication.EmailService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class EmailHelper {

    private final Logger log = LogManager.getLogger(EmailHelper.class);
    private final EmailService emailService;
    private final RoleRepository roleRepository;
    private final OrganizationCacheService organizationCacheService;
    private final RolePrivilegeHelper rolePrivilegeHelper;

    public EmailHelper(EmailService emailService, RoleRepository roleRepository, OrganizationCacheService organizationCacheService, RolePrivilegeHelper rolePrivilegeHelper) {
        this.emailService = emailService;
        this.roleRepository = roleRepository;
        this.organizationCacheService = organizationCacheService;
        this.rolePrivilegeHelper = rolePrivilegeHelper;
    }

    public void sendAccountCreationEmail(String toEmail, String userName, String Password, boolean isNewUser, Long roleId) {
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));
        String roleName = role.getName();
        String emailLoginKey = organizationCacheService.getPrivilegeKey(PrivilegeConstants.LOGIN_VIA_EMAIL);
        String mobileLoginKey = organizationCacheService.getPrivilegeKey(PrivilegeConstants.LOGIN_VIA_MOBILE);
        boolean emailLogin = rolePrivilegeHelper.roleHasPrivilege(roleName, emailLoginKey);
        boolean mobileLogin = rolePrivilegeHelper.roleHasPrivilege(roleName, mobileLoginKey);
        log.info("Role name: {}, emailLogin: {}, mobileLogin: {}", roleName, emailLogin, mobileLogin);
        String emailSubject = "Account Created Successfully";
        String emailType = emailLogin ? "account_creation" : mobileLogin ? "mobile_login" : "default";
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
