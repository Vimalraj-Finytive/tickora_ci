package com.uniq.tms.tms_microservice.modules.organizationManagement.services.impl;

import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.OrganizationAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.PaymentAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.SubscriptionAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PlanAnalyticsDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.SubscriptionDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.UpgradePlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PaymentEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PlanEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.SubscriptionEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.OrganizationStatusEnum;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.PaymentStatus;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.PlanFeaturesEnum;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.PlaneName;
import com.uniq.tms.tms_microservice.modules.organizationManagement.mapper.UpgradeDtoMapper;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.PlanAnalyticsModel;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.PlanStatusModel;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.UpgradePlan;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.PaymentService;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.SubscriptionService;
import com.uniq.tms.tms_microservice.shared.util.TenantUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {
    private static final Logger log = LogManager.getLogger(SubscriptionServiceImpl.class);

    private final SubscriptionAdapter subscriptionAdapter;
    private final PaymentService paymentService;
    private final UpgradeDtoMapper upgradeDtoMapper;
    private final PaymentAdapter paymentAdapter;
    private final OrganizationAdapter organizationAdapter;

    public SubscriptionServiceImpl(SubscriptionAdapter subscriptionAdapter, PaymentService paymentService, UpgradeDtoMapper upgradeDtoMapper, PaymentAdapter paymentAdapter, OrganizationAdapter organizationAdapter){
        this.subscriptionAdapter = subscriptionAdapter;
        this.paymentService = paymentService;
        this.upgradeDtoMapper = upgradeDtoMapper;
        this.paymentAdapter = paymentAdapter;
        this.organizationAdapter = organizationAdapter;
    }

    @PersistenceContext(type = PersistenceContextType.TRANSACTION)
    private EntityManager entityManager;

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
            log.info("Payment isSuccess:  {}",
                    isSuccess);
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

        String message = String.format(
                "Your plan is valid for %d more day%s Only. Renew or upgrade now to continue enjoying our services.",
                daysLeft,
                daysLeft == 1 ? "" : "s"
        );

        return PlanStatusModel.builder()
                .planName(planName)
                .active(isActive)
                .showNotification(showNotification)
                .message(message)
                .daysLeft(Math.max(daysLeft, 0))
                .build();
    }

//    @Override
//    public double calculateProratedAmount(int additionalUsers, String orgId) {
//        SubscriptionEntity subscription = subscriptionAdapter.findActiveSubscriptionByOrgId(orgId)
//                .orElseThrow(() -> new RuntimeException("No active or expired subscription found for org: " + orgId));
//
//        PlanEntity plan = subscriptionAdapter.findById(subscription.getPlanId())
//                .orElseThrow(() -> new RuntimeException("Plan not found for planId: " + subscription.getPlanId()));
//
//        double pricePerUser = plan.getPricePerUser().doubleValue();
//
//        LocalDate today = LocalDate.now();
//        LocalDate endDate = subscription.getEndDate().toLocalDate();
//
//        if (!today.isBefore(endDate)) {
//            throw new RuntimeException("Subscription has already expired");
//        }
//
//        long remainingDays = ChronoUnit.DAYS.between(today, endDate);
//
//        long months = remainingDays / 30;
//        long extraDays = remainingDays % 30;
//
//        double amountForMonths = additionalUsers * pricePerUser * months;
//        double amountForExtraDays = additionalUsers * pricePerUser * ((double) extraDays / 30);
//
//        double totalAmount = amountForMonths + amountForExtraDays;
//
//        return totalAmount;
//    }
//
//    @Override
//    public boolean addSubscribedUsers(String orgId, String orgSchema, UpgradePlanDto dto) {
//        boolean isUpdated = false;
//        PaymentStatus historyStatus;
//
//        try {
//            SubscriptionEntity subscription = subscriptionAdapter.findActiveSubscriptionByOrgId(orgId)
//                    .orElse(null);
//
//            if (subscription == null) {
//                log.warn("No active subscription found for orgID: {}", orgId);
//                historyStatus = PaymentStatus.FAILED;
//            } else {
//                if (Boolean.TRUE.equals(dto.getStatus())) {
//
//                    int totalSubscribedUsers = subscription.getSubscribedUsers() + dto.getSubscribedUserCount();
//                    subscription.setSubscribedUsers(totalSubscribedUsers);
//                    subscription.setUpdatedAt(LocalDateTime.now());
//
//                    boolean updated = subscriptionAdapter.updateSubscription(subscription);
//
//                    if (updated) {
//                        isUpdated = true;
//                        historyStatus = PaymentStatus.SUCCESS;
//                        log.info("Subscribed users updated successfully for orgID: {} | SubscriptionID: {} | New Count: {}",
//                                orgId, subscription.getSubId(), totalSubscribedUsers);
//                    } else {
//                        historyStatus = PaymentStatus.FAILED;
//                        log.warn("Failed to update subscribed users for orgID: {}", orgId);
//                    }
//
//                } else {
//                    historyStatus = PaymentStatus.FAILED;
//                    log.info("dto.status is FALSE — skipping subscribed user update.");
//                }
//            }
//
//            PaymentEntity paymentHistory = paymentAdapter.createPayment(
//                    orgId,
//                    dto.getOrderID(),
//                    dto.getTotalSubscriptionAmount(),
//                    subscriptionAdapter.getBillingCycle(dto.getPlanId()),
//                    historyStatus,
//                    orgSchema
//            );
//
//            log.info("Payment history saved for orgID: {} | PaymentHistoryID: {} | Status: {}",
//                    orgId, paymentHistory.getPaymentId(), historyStatus.getDisplayValue());
//
//            return isUpdated;
//
//        } catch (Exception e) {
//            log.error("Error while adding subscribed users for orgID: {} | Error: {}", orgId, e.getMessage(), e);
//
//            try {
//                PaymentEntity paymentHistory = paymentAdapter.createPayment(
//                        orgId,
//                        dto.getOrderID(),
//                        dto.getTotalSubscriptionAmount(),
//                        subscriptionAdapter.getBillingCycle(dto.getPlanId()),
//                        PaymentStatus.FAILED,
//                        orgSchema
//                );
//                log.info("Payment history saved with FAILED status for orgID: {} | PaymentHistoryID: {}",
//                        orgId, paymentHistory.getPaymentId());
//            } catch (Exception ex) {
//                log.error("Failed to save payment history after exception for orgID: {} | Error: {}", orgId, ex.getMessage(), ex);
//            }
//
//            return false;
//        }
//    }

    @Override
    public List<PlanAnalyticsModel> calculatePlanUsage(LocalDate fromDate, LocalDate toDate) {

        List<OrganizationEntity> organizations = organizationAdapter.findAll();

        Map<String, Long> planCountMap = new HashMap<>();

        for (OrganizationEntity org : organizations) {
            List<SubscriptionEntity> subscriptions = subscriptionAdapter.getAllSubscriptionsForOrgBetweenDates(
                    fromDate, toDate);

            if (subscriptions.isEmpty()) {
                System.out.println(" No subscriptions found for this organization in given period.");
            } else {
                System.out.println(" Found " + subscriptions.size() + " subscriptions:");
            }

            for (SubscriptionEntity subscription : subscriptions) {
                planCountMap.merge(subscription.getPlanId(), 1L, Long::sum);
            }
        }

        long totalSubscriptions = planCountMap.values().stream().mapToLong(Long::longValue).sum();
        System.out.println("\nTotal subscriptions across all orgs: " + totalSubscriptions);

        List<PlanAnalyticsModel> result = new ArrayList<>();

        planCountMap.forEach((planId, count) -> {
            String planName = subscriptionAdapter.findById(planId)
                    .map(PlanEntity::getPlanName)
                    .orElse(planId);

            BigDecimal usagePercentage = totalSubscriptions > 0
                    ? BigDecimal.valueOf(count * 100.0 / totalSubscriptions).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            System.out.println("Plan Summary: " + planName + " | Count: " + count + " | Usage: " + usagePercentage + "%");

            result.add(new PlanAnalyticsModel(planId, planName, count, usagePercentage));
        });

        return result;
    }


}
