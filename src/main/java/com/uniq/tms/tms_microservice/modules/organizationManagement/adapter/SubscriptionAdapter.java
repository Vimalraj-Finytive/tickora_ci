package com.uniq.tms.tms_microservice.modules.organizationManagement.adapter;

import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.SubscriptionDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.UpgradePlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PaymentEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PlanEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.SubscriptionEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SubscriptionAdapter {
    long countSubscribedUsers(String orgId);
    SubscriptionDto getActivePlan(String orgId);
    List<SubscriptionDto> getPlanHistory(String orgId);
    List<PlanDto> getAllPlans();
    boolean validatePlanAmount(String planId, Integer subscribedUserCount, BigDecimal totalSubscriptionAmount);
    String updatePlan(String orgId,String orgSchema, String planId, Integer subscribedUserCount);
    String getBillingCycle(String planId);
    Optional<SubscriptionEntity> findActiveSubscriptionByOrgId(String orgId);
    Optional<PlanEntity> findById(String planId);
}
