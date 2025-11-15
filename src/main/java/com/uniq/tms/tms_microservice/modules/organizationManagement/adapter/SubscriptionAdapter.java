package com.uniq.tms.tms_microservice.modules.organizationManagement.adapter;

import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.SubscriptionDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PlanEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.SubscriptionEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionAdapter {
    long countSubscribedUsers(String orgId);
    SubscriptionDto getActivePlan(String orgId);
    List<SubscriptionDto> getPlanHistory(String orgId);
    List<PlanDto> getAllPlans();
    boolean validatePlanAmount(String planId, Integer subscribedUserCount, BigDecimal totalSubscriptionAmount);
    PaymentStatus updatePlan(String orgId, String orgSchema, String planId, Integer subscribedUserCount, String paymentId);
    String getBillingCycle(String planId);
    Optional<SubscriptionEntity> findActiveSubscriptionByOrgId(String orgId);
    Optional<PlanEntity> findById(String planId);
    Optional <SubscriptionEntity>findSubscriptionDetails(String subscriptionId);
    List<SubscriptionEntity> findAllSubscriptionsByOrgId(String orgId);
    boolean updateSubscription(SubscriptionEntity subscription);
    List<SubscriptionEntity> getAllSubscriptionsForOrgBetweenDates(LocalDate fromDate, LocalDate toDate);
    List<String> getAllPlanIds();
    PaymentStatus updateExistingPlan(String orgId, String schema, String planId, Integer userCount, String paymentId);
}
