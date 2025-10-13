package com.uniq.tms.tms_microservice.modules.organizationManagement.services;

import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PlanStatusDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.SubscriptionDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.UpgradePlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.PaymentModel;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.PlanStatusModel;

import java.util.List;

public interface SubscriptionService {
    long  getSubscribedUserCount(String orgId);
    SubscriptionDto getActivePlan(String orgId);
    List<SubscriptionDto> getPlanHistory(String orgId);
    List<PlanDto> getAllPlans();
    String upgradePlan(String orgId,String orgSchema, UpgradePlanDto request);
    PlanStatusModel getCurrentPlanStatus(String orgId);

}

