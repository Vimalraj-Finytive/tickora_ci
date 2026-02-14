package com.uniq.tms.tms_microservice.modules.organizationManagement.scheduler;

import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.OrganizationStatusEnum;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.SubscriptionDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.SubscriptionService;
import com.uniq.tms.tms_microservice.modules.userManagement.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.UserNameEmailDto;
import com.uniq.tms.tms_microservice.shared.helper.EmailHelper;
import com.uniq.tms.tms_microservice.shared.util.TenantUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class SubscriptionScheduler {

    private static final Logger log = LogManager.getLogger(SubscriptionScheduler.class);

    private final OrganizationRepository organizationRepository;
    private final EmailHelper emailHelper;
    private final SubscriptionService subscriptionService;
    private final UserAdapter userAdapter;

    @Value("${subscription.renew-link}")
    private String renewLink;

    public SubscriptionScheduler(
            OrganizationRepository organizationRepository,
            EmailHelper emailHelper,
            SubscriptionService subscriptionService,
            UserAdapter userAdapter) {
        this.organizationRepository = organizationRepository;
        this.emailHelper = emailHelper;
        this.subscriptionService = subscriptionService;
        this.userAdapter = userAdapter;
    }

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Kolkata")
    public void checkSubscriptions() {
        log.info("Starting daily subscription check...");

        List<OrganizationEntity> organizations = organizationRepository.findAll();

        for (OrganizationEntity org : organizations) {
            try {
                TenantUtil.setCurrentTenant(org.getSchemaName());
                log.info("Checking subscription for organization: {}", org.getOrgName());

                SubscriptionDto subscription = subscriptionService.getActivePlan(org.getOrganizationId());

                if (subscription == null) {
                    log.warn("No active subscription data found for org: {}", org.getOrgName());
                    continue;
                }

                String status = subscription.getStatus();
                String expiryDateStr = subscription.getActiveUntil();

                if (expiryDateStr == null || status == null) {
                    log.warn("Missing subscription details for org: {}", org.getOrgName());
                    continue;
                }

                LocalDate expiryDate = LocalDate.parse(expiryDateStr.substring(0, 10));
                long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);

                boolean isActiveReminder = OrganizationStatusEnum.ACTIVE.name().equalsIgnoreCase(status)
                        && (daysRemaining == 30 || (daysRemaining <= 3 && daysRemaining >= 0));
                boolean isExpiredAlert = OrganizationStatusEnum.EXPIRED.name().equalsIgnoreCase(status) || daysRemaining < 0;

                if (isActiveReminder || isExpiredAlert) {

                    List<UserNameEmailDto> admins = userAdapter.findAdminAndSuperAdminNamesAndEmails();
                    if (admins == null || admins.isEmpty()) {
                        log.warn("No admin/super admin users found for org: {}", org.getOrgName());
                        continue;
                    }

                    for (UserNameEmailDto admin : admins) {
                        emailHelper.sendSubscriptionExpiryReminder(
                                admin.getEmail(),
                                admin.getName(),
                                org.getOrgName(),
                                daysRemaining,
                                expiryDate.toString()
                        );
                    }

                    if (isActiveReminder) {
                        log.info("Sent reminder emails to {} admins for org: {} ({} days remaining)",
                                admins.size(), org.getOrgName(), daysRemaining);
                    } else {
                        log.info("Sent EXPIRED alert emails to {} admins for org: {} (expired on {})",
                                admins.size(), org.getOrgName(), expiryDate);
                    }

                } else {
                    log.info("Subscription for org '{}' is healthy ({} days remaining)",
                            org.getOrgName(), daysRemaining);
                }

            } catch (Exception e) {
                log.error("Error checking subscription for org '{}': {}", org.getOrgName(), e.getMessage(), e);
            } finally {
                TenantUtil.clearTenant();
            }
        }

        log.info("Subscription check completed for all organizations.");
    }
}
