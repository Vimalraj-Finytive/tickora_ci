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
    public boolean amountValidation(String orgId, String orgSchema, UpgradePlanDto request) {
        boolean isValid = subscriptionAdapter.validatePlanAmount(
                request.getPlanId(),
                request.getSubscribedUserCount(),
                request.getTotalSubscriptionAmount()
        );

        if (!isValid) {
            return false;
        }

        return true;

    }

    @Override
    public boolean upgradePlan(String orgId, String orgSchema, UpgradePlanDto upgradePlanDto) {
        try {
            UpgradePlan model = upgradeDtoMapper.toModel(upgradePlanDto);
            boolean isSuccess = Boolean.TRUE.equals(model.getSuccess());
            PaymentStatus status = isSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
            PaymentEntity payment = paymentService.createPayment(
                    orgId,
                    model.getOrderId(),
                    model.getAmount(),
                    subscriptionAdapter.getBillingCycle(model.getPlanId()),
                    orgSchema,
                    status
            );
            if (isSuccess) {

                if (payment != null && payment.getPaymentId() != null) {
                    log.info("Payment created successfully for Order ID: {} | Payment ID: {} | Payment Entity: {}",
                            model.getOrderId(), payment.getPaymentId(), payment);
                    log.info("Subscription activated for Order ID: {} | Plan ID: {}",
                            model.getOrderId(), model.getPlanId());
                    PaymentStatus isSubscribed = subscriptionAdapter.updatePlan(orgId,orgSchema,model.getPlanId(), model.getSubscribedUserCount(), payment.getPaymentId());
                    if (PaymentStatus.SUCCESS.equals(isSubscribed)) {
                        log.info("Subscription update successful for Org ID: {} | Payment ID: {}", orgId, payment.getPaymentId());
                        return true;
                    } else {
                        log.warn("Subscription update failed for Org ID: {} | Payment ID: {}", orgId, payment.getPaymentId());
                        return false;
                    }
                } else {
                    log.warn("Payment creation failed or returned null for Order ID: {} | Plan ID: {}",
                            model.getOrderId(), model.getPlanId());
                    return false;
                }

            } else {
                log.warn("Payment failed, subscription not activated for Order ID: {} | Plan ID: {}",
                        model.getOrderId(), model.getPlanId());
                return false;
            }
        } catch (Exception e) {
            log.error("Error occurred while upgrading plan for Org ID: {} | Error: {}", orgId, e.getMessage(), e);
            return false;
        }
    }


    @Override
    public List<SubscriptionDto> getPlanHistory(String orgId) {
        return subscriptionAdapter.getPlanHistory(orgId);
    }

}
