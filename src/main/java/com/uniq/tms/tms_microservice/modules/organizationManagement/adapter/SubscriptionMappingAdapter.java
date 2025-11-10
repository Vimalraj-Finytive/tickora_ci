package com.uniq.tms.tms_microservice.modules.organizationManagement.adapter;

import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.SubscriptionMappingEntity;

import java.util.List;

public interface SubscriptionMappingAdapter {
    List<SubscriptionMappingEntity> findBySubscriptionId(String subscriptionId);
}
