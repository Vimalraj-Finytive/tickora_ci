package com.uniq.tms.tms_microservice.shared.security.jwt;

import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.SubscriptionEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.SubscriptionRepository;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.UserRole;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import com.uniq.tms.tms_microservice.shared.security.user.CustomUserDetails;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SubscriptionValidationService {

    private final  SubscriptionRepository subscriptionRepository;
    private final AuthHelper authHelper;

    public SubscriptionValidationService(SubscriptionRepository subscriptionRepository, AuthHelper authHelper) {
        this.subscriptionRepository = subscriptionRepository;
        this.authHelper = authHelper;
    }

    public boolean hasActiveSubscription() {
        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            return false;
        }
        List<SubscriptionEntity> subscriptions = subscriptionRepository.findAllSubscriptionsByOrgId(orgId);
        for (SubscriptionEntity sub : subscriptions) {
            if ("Active".equalsIgnoreCase(sub.getStatus())) {
                return true;
            }
        }
        return false;
    }

    public String getExpiredMessage() {
        CustomUserDetails user = authHelper.getCurrentUser();
        int roleLevel = UserRole.getLevel(user.getRole());
        if (roleLevel > UserRole.MANAGER.getHierarchyLevel()) {
            return "Plan expired. Please upgrade your plan.";
        } else {
            return "Plan expired. Please contact your admin.";
        }
    }
}




//    if (!subscriptionValidationService.hasActiveSubscription()) {
//        String message = subscriptionValidationService.getExpiredMessage();
//        return ResponseEntity.status(403).body(new ApiResponse(403, message, null));
//        }

