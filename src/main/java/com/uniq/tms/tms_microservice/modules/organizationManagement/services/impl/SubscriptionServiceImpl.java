package com.uniq.tms.tms_microservice.modules.organizationManagement.services.impl;

import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.SubscriptionAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.SubscriptionDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.UpgradePlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PaymentEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PlanEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.SubscriptionEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.OrganizationStatusEnum;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.PaymentStatus;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.PlanFeaturesEnum;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.PlaneName;
import com.uniq.tms.tms_microservice.modules.organizationManagement.mapper.UpgradeDtoMapper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.PlanStatusModel;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.UpgradePlan;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.PaymentService;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.SubscriptionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {
    private static final Logger log = LogManager.getLogger(SubscriptionServiceImpl.class);

    private final SubscriptionAdapter subscriptionAdapter;
    private final PaymentService paymentService;
    private final UpgradeDtoMapper upgradeDtoMapper;

    public SubscriptionServiceImpl(SubscriptionAdapter subscriptionAdapter, PaymentService paymentService, UpgradeDtoMapper upgradeDtoMapper){
        this.subscriptionAdapter = subscriptionAdapter;
        this.paymentService = paymentService;
        this.upgradeDtoMapper = upgradeDtoMapper;
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

    @Value("${subscription.expiring-notificationday}")
    private int expiringNotificationDay;

    @Override
    public PlanStatusModel getCurrentPlanStatus(String orgId) {
        log.info("Fetching current plan status for org: {}", orgId);

        SubscriptionEntity subscription = subscriptionAdapter.findActiveSubscriptionByOrgId(orgId).orElse(null);
        if (subscription == null) {
            return PlanStatusModel.builder()
                    .planName("No Active Plan")
                    .active(false)
                    .showNotification(true)
                    .message("No active subscription found.")
                    .daysLeft(0)
                    .build();
        }

        PlanEntity plan = subscriptionAdapter.findById(subscription.getPlanId()).orElse(null);
        String planName = (plan != null) ? plan.getPlanName() : "Unknown Plan";

        LocalDate today = LocalDate.now();
        LocalDate endDate = subscription.getEndDate().toLocalDate();
        long daysLeft = ChronoUnit.DAYS.between(today, endDate);
        boolean isActive = OrganizationStatusEnum.ACTIVE.getDisplayValue().equalsIgnoreCase(subscription.getStatus());

        boolean showNotification = (plan != null && PlaneName.BASIC_PLAN.name().equalsIgnoreCase(plan.getPlanName()))
                || daysLeft <= expiringNotificationDay;

        String message = String.format("Your plan is valid for %d more day%s.", daysLeft, daysLeft == 1 ? "" : "s");

        return PlanStatusModel.builder()
                .planName(planName)
                .active(isActive)
                .showNotification(showNotification)
                .message(message)
                .daysLeft(Math.max(daysLeft, 0))
                .build();
    }
}
