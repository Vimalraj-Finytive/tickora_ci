package com.uniq.tms.tms_microservice.shared.helper;

import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.SubscriptionAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.SubscriptionEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.OrganizationStatusEnum;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.UserRole;
import com.uniq.tms.tms_microservice.shared.security.user.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
public class SubscriptionValHelper {

    private final AuthHelper authHelper;
    private final SubscriptionAdapter subscriptionAdapter;


    public SubscriptionValHelper(AuthHelper authHelper, SubscriptionAdapter subscriptionAdapter) {
        this.authHelper = authHelper;
        this.subscriptionAdapter = subscriptionAdapter;
    }

    public boolean hasActiveSubscription() {
        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            return false;
        }
        List<SubscriptionEntity> subscriptions = subscriptionAdapter.findAllSubscriptionsByOrgId(orgId);
        for (SubscriptionEntity subscription : subscriptions) {
            if (OrganizationStatusEnum.ACTIVE.getDisplayValue().equalsIgnoreCase(subscription.getStatus())) {
                return true;
            }
        }
        return false;
    }

    public String getExpiredMessage() {
        CustomUserDetails user = authHelper.getCurrentUser();
        int roleLevel = UserRole.getLevel(user.getRole());
        log.info("Role level: {}", roleLevel);
        int managerRole=UserRole.MANAGER.getHierarchyLevel();
        log.info("Manager:{}",managerRole);

        if (roleLevel < managerRole) {
            return "Plan expired. Please upgrade your plan.";
        } else {
            return "Plan expired. Please contact your admin.";
        }
    }
}



