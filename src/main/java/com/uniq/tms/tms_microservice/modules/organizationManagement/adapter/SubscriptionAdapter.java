package com.uniq.tms.tms_microservice.modules.organizationManagement.adapter;

import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.SubscriptionDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.UpgradePlanDto;

import java.math.BigDecimal;
import java.util.List;

public interface SubscriptionAdapter {
    long countSubscribedUsers(String orgId);
    SubscriptionDto getActivePlan(String orgId);
    List<SubscriptionDto> getPlanHistory(String orgId);
    List<PlanDto> getAllPlans();
    boolean validatePlanAmount(String planId, Integer subscribedUserCount, BigDecimal totalSubscriptionAmount);
    PaymentStatus updatePlan(String orgId, String orgSchema, String planId, Integer subscribedUserCount, String paymentId);
    String getBillingCycle(String planId);
}
