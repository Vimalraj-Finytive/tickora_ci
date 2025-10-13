package com.uniq.tms.tms_microservice.modules.organizationManagement.services.impl;

import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.SubscriptionAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PlanStatusDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.SubscriptionDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.UpgradePlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PaymentEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PlanEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.SubscriptionEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.OrganizationStatusEnum;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.PlanFeaturesEnum;
import com.uniq.tms.tms_microservice.modules.organizationManagement.mapper.SubscriptionDtoMapper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.mapper.SubscriptionEntityMapper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.PaymentModel;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.PlanStatusModel;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.PaymentService;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.SubscriptionService;
import org.springframework.beans.factory.annotation.Value;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {



    private static final Logger log = LogManager.getLogger(SubscriptionServiceImpl.class);
    private final SubscriptionDtoMapper subscriptionDtoMapper;
    private final SubscriptionEntityMapper subscriptionEntityMapper;
    private final SubscriptionAdapter subscriptionAdapter;
    private final PaymentService paymentService;


    public SubscriptionServiceImpl(SubscriptionDtoMapper subscriptionDtoMapper, SubscriptionEntityMapper subscriptionEntityMapper, SubscriptionAdapter subscriptionAdapter, PaymentService paymentService){
        this.subscriptionDtoMapper = subscriptionDtoMapper;
        this.subscriptionEntityMapper = subscriptionEntityMapper;
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



        @Value("${subscription.expiring-notificationday}")
        private Integer reminderStartDay;
    @Override
    public PlanStatusModel getCurrentPlanStatus(String orgId) {
        log.info("Fetching current plan status for org: {}", orgId);
        Optional<SubscriptionEntity> subscriptionOpt = subscriptionAdapter.findActiveSubscriptionByOrgId(orgId);
        if (subscriptionOpt.isEmpty()) {
            log.warn("No active subscription found for org {}", orgId);
            return PlanStatusModel.builder()
                    .planName("No Active Plan")
                    .isActive(false)
                    .showNotification(true)
                    .daysLeft(0)
                    .Message("Your Plane is Expired Update Your Plane for Active")
                    .build();
        }

        SubscriptionEntity subscription = subscriptionOpt.get();
        PlanEntity plan = subscriptionAdapter.findById(subscription.getPlanId()).orElse(null);
        String planName = (plan != null) ? plan.getPlanName() : "Unknown Plan";

        LocalDate today = LocalDate.now();
        LocalDate endDate = subscription.getEndDate().toLocalDate();
        long daysLeft = ChronoUnit.DAYS.between(today, endDate);

        boolean isActive = OrganizationStatusEnum.ACTIVE.getDisplayValue().equalsIgnoreCase(subscription.getStatus());
        boolean showNotification;
        String message=null;
        if (plan != null && "Basic plan".equalsIgnoreCase(plan.getPlanName())) {
            showNotification = true;
        } else {
            showNotification = daysLeft <= reminderStartDay;
        }

        if(showNotification==true) {
             message ="Your plan valid untill :"+daysLeft;
        }
        else{
            message="Your plan valid untill :"+daysLeft;
        }
        return

                PlanStatusModel.builder()
                .planName(planName)
                .isActive(isActive)
                .showNotification(showNotification)
                .daysLeft(Math.max(daysLeft, 0))
                .Message(message)
                .build();
    }
}


