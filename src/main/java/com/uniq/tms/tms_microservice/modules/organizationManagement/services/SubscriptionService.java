package com.uniq.tms.tms_microservice.modules.organizationManagement.services;

import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.*;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.PlanAnalyticsModel;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.PlanStatusModel;
import java.time.LocalDate;
import java.util.List;

public interface SubscriptionService {
    long  getSubscribedUserCount(String orgId);
    SubscriptionDto getActivePlan(String orgId);
    List<SubscriptionDto> getPlanHistory(String orgId);
    List<PlanDto> getAllPlans();
    boolean amountValidation(String orgId,String orgSchema, UpgradePlanDto request);
    boolean upgradePlan(String orgId, String orgSchema, UpgradePlanDto upgradePlanDto);
    PlanStatusModel getCurrentPlanStatus(String orgId);
    CalculatedAmountDto calculateProratedAmount(int additionalUsers, String orgId);
    boolean addSubscribedUsers(String orgId, String orgSchema, UpgradePlanDto dto);
     List<PlanAnalyticsModel> calculatePlanUsage(LocalDate fromDate, LocalDate toDate);
    boolean updateSubscription(String orgId, String orgSchema, UpdatePlanDto dto);
}
