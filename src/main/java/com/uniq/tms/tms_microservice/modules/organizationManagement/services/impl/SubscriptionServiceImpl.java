package com.uniq.tms.tms_microservice.modules.organizationManagement.services.impl;

import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.SubscriptionAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.OrganizationSummaryDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.SubscriptionDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.SubscriptionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {
    private static final Logger log = LogManager.getLogger(SubscriptionServiceImpl.class);

    private final SubscriptionAdapter subscriptionAdapter;

    public SubscriptionServiceImpl(SubscriptionAdapter subscriptionAdapter){
        this.subscriptionAdapter = subscriptionAdapter;
    }

    @Override
    public long getSubscribedUserCount(String orgId) {
        return subscriptionAdapter.countSubscribedUsers(orgId);
    }


    @Override
    public SubscriptionDto getActivePlan(String orgId) {
        return subscriptionAdapter.getActivePlan(orgId);
    }

}
