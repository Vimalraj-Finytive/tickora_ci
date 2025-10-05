package com.uniq.tms.tms_microservice.modules.organizationManagement.services;

import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.OrganizationSummaryDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.SubscriptionDto;

public interface SubscriptionService {
    long  getSubscribedUserCount(String orgId);
    SubscriptionDto getActivePlan(String orgId);
}
