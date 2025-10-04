package com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.impl;


import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.SubscriptionAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.OrganizationSummaryDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.SubscriptionDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PlanEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.mapper.SubscriptionDtoMapper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.mapper.SubscriptionEntityMapper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.PlanRepository;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.SubscriptionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@ConditionalOnProperty(name = "database.type", havingValue = "postgres")
public class SubscriptionAdapterImpl implements SubscriptionAdapter {
    private static final Logger log = LogManager.getLogger(SubscriptionAdapterImpl.class);
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionEntityMapper subscriptionEntityMapper;
    private final SubscriptionDtoMapper subscriptionDtoMapper;
    private final PlanRepository planRepository;
    public SubscriptionAdapterImpl(SubscriptionRepository subscriptionRepository, SubscriptionEntityMapper subscriptionEntityMapper, SubscriptionDtoMapper subscriptionDtoMapper, PlanRepository planRepository){
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionEntityMapper = subscriptionEntityMapper;
        this.subscriptionDtoMapper = subscriptionDtoMapper;
        this.planRepository = planRepository;
    }

    @Override
    public long countSubscribedUsers(String orgId) {
        return subscriptionRepository.getSubscribedUsersByOrgId(orgId);
    }

    @Override
    public SubscriptionDto getActivePlan(String orgId) {
        return subscriptionRepository.findActiveSubscription()
                .map(entity -> subscriptionEntityMapper.toModel(entity))   // Entity → Model
                .map(model -> {
                    SubscriptionDto dto = subscriptionDtoMapper.toDto(model); // Model → DTO

                    // Fetch plan name from PlanEntity using planId
                    Optional<PlanEntity> planOpt = planRepository.findById(model.getPlanName()); // assuming planName currently holds planId
                    planOpt.ifPresent(plan -> dto.setCurrentPlan(plan.getPlanName())); // overwrite with actual plan name

                    return dto;
                })
                .orElse(null);
    }




}
