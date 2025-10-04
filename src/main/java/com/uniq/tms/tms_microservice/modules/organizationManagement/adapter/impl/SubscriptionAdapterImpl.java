package com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.impl;


import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.SubscriptionAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.SubscriptionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "database.type", havingValue = "postgres")
public class SubscriptionAdapterImpl implements SubscriptionAdapter {
    private static final Logger log = LogManager.getLogger(SubscriptionAdapterImpl.class);
    private final SubscriptionRepository subscriptionRepository;
    public SubscriptionAdapterImpl(SubscriptionRepository subscriptionRepository){
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public long countSubscribedUsers(String orgId) {
        return subscriptionRepository.getSubscribedUsersByOrgId(orgId);
    }

}
