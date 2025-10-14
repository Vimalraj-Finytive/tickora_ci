package com.uniq.tms.tms_microservice.modules.organizationManagement.services;

import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.SubscriptionDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.UpgradePlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.PlanStatusModel;

import java.util.List;

public interface SubscriptionService {
    long  getSubscribedUserCount(String orgId);
    SubscriptionDto getActivePlan(String orgId);
    List<SubscriptionDto> getPlanHistory(String orgId);
    List<PlanDto> getAllPlans();
    boolean amountValidation(String orgId,String orgSchema, UpgradePlanDto request);
    boolean upgradePlan(String orgId, String orgSchema, UpgradePlanDto upgradePlanDto);
        PlanStatusModel getCurrentPlanStatus(String orgId);
}
