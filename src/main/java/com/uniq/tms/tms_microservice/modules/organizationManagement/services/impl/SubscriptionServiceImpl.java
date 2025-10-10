package com.uniq.tms.tms_microservice.modules.organizationManagement.services.impl;

import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.SubscriptionAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.SubscriptionDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.UpgradePlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PaymentEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PlanEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.PlanFeaturesEnum;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.PaymentService;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.SubscriptionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {
    private static final Logger log = LogManager.getLogger(SubscriptionServiceImpl.class);

    private final SubscriptionAdapter subscriptionAdapter;
    private final PaymentService paymentService;

    public SubscriptionServiceImpl(SubscriptionAdapter subscriptionAdapter, PaymentService paymentService){
        this.subscriptionAdapter = subscriptionAdapter;
        this.paymentService = paymentService;
    }

    @Override
    public long getSubscribedUserCount(String orgId) {
        return subscriptionAdapter.countSubscribedUsers(orgId);
    }


    @Override
    public SubscriptionDto getActivePlan(String orgId) {
        return subscriptionAdapter.getActivePlan(orgId);
    }

    @Override
    public List<PlanDto> getAllPlans() {
        List<PlanDto> plans = subscriptionAdapter.getAllPlans();
        plans.forEach(plan -> {
            PlanFeaturesEnum planEnum = PlanFeaturesEnum.getByPlanId(plan.getPlanId());
            if (planEnum != null) {
                plan.setFeatures(planEnum.getFeatures());
                plan.setPriceTerm(planEnum.getPriceTerm());
            }
        });
        return plans;
    }

    @Override
    public String upgradePlan(String orgId,String orgSchema, UpgradePlanDto request) {
        boolean isValid = subscriptionAdapter.validatePlanAmount(
                request.getPlanId(),
                request.getSubscribedUserCount(),
                request.getTotalSubscriptionAmount()
        );

        if (!isValid) {
            return "The requested amount is mismatching with actual calculated amount";
        }

        String billingCycle = subscriptionAdapter.getBillingCycle(request.getPlanId());

//        BigDecimal amount = new BigDecimal("1.00");
//        String paymentResponse = paymentService.createPaymentOrder(orgId, amount);
//        String orderId = extractOrderId(paymentResponse);

        String orderId = "order_RQZ0IvCr651OlG";
        if (orderId != null) {
            String newSubId  = subscriptionAdapter.updatePlan(orgId,orgSchema , request.getPlanId(), request.getSubscribedUserCount());

            if (newSubId != null) {
                PaymentEntity payment = paymentService.createPayment(orgId,newSubId,orderId, billingCycle,request.getTotalSubscriptionAmount(), request.getSubscribedUserCount(), request.getPlanId(), orgSchema);
                return "Subscription upgraded successfully!";
            } else {
                return "Payment completed, but the plan was not updated (already expired or up-to-date).";
            }
        } else {
            return "Failed to create payment order: " ;
        }
    }

    private String extractOrderId(String paymentResponse) {
        try {
            JSONObject json = new JSONObject(paymentResponse);
            return json.optString("orderId");
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<SubscriptionDto> getPlanHistory(String orgId) {
        return subscriptionAdapter.getPlanHistory(orgId);
    }

}
