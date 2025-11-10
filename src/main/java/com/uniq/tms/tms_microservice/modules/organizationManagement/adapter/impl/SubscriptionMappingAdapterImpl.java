package com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.SubscriptionMappingAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.SubscriptionMappingEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.SubscriptionMappingRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "database.type", havingValue = "postgres")
public class SubscriptionMappingAdapterImpl implements SubscriptionMappingAdapter {

    private final SubscriptionMappingRepository subscriptionMappingRepository;

    public SubscriptionMappingAdapterImpl(SubscriptionMappingRepository subscriptionMappingRepository) {
        this.subscriptionMappingRepository = subscriptionMappingRepository;
    }

    @Override
    public List<SubscriptionMappingEntity> findBySubscriptionId(String subscriptionId) {
        return subscriptionMappingRepository.findBySubscriptionId(subscriptionId);
    }
}
