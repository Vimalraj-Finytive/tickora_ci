package com.uniq.tms.tms_microservice.modules.organizationManagement.adapter;

import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.OrganizationSummaryDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.SubscriptionDto;

public interface SubscriptionAdapter {
    long countSubscribedUsers(String orgId);
    SubscriptionDto getActivePlan(String orgId);
}
