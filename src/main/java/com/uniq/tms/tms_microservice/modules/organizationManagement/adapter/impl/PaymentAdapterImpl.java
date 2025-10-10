package com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.identityManagement.service.IdGenerationService;
import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.PaymentAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PaymentEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.PaymentRepository;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.impl.SubscriptionServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Component
@ConditionalOnProperty(name = "database.type", havingValue = "postgres")
public class PaymentAdapterImpl implements PaymentAdapter {
    private static final Logger log = LogManager.getLogger(PaymentAdapterImpl.class);
    private final IdGenerationService idGenerationService;
    private final PaymentRepository paymentRepository;

    public PaymentAdapterImpl(IdGenerationService idGenerationService, PaymentRepository paymentRepository) {
        this.idGenerationService = idGenerationService;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public PaymentEntity createPayment(String orgId,String subId,String orderId, String billingCycle, BigDecimal subscriptionAmount, Integer subscribedUserCount, String planId, String orgSchema) {
        try {

            PaymentEntity payment = new PaymentEntity();
            payment.setPaymentId(idGenerationService.generateNextPaymentID(orgId));
            payment.setSubscriptionId(subId);
            payment.setOrderId(orderId);
            payment.setAmount(subscriptionAmount);
            payment.setBillingPeriod(billingCycle);
            payment.setPaymentStatus("COMPLETED");
            payment.setPaymentDate(LocalDateTime.now());
            payment.setSchemaName(orgSchema);
            payment.setCreatedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());

            return paymentRepository.save(payment);
        } catch (Exception e) {
            log.error("Failed to create payment for Org: {}, Plan: {}", orgId, planId, e);
            throw new RuntimeException("Error while creating payment record: " + e.getMessage());
        }
    }
}
