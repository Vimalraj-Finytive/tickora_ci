package com.uniq.tms.tms_microservice.modules.organizationManagement.adapter;

import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PaymentEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.enums.PaymentStatus;

import java.math.BigDecimal;

public interface PaymentAdapter {
    PaymentEntity createPayment(String orgId, String orderId, BigDecimal amount, String billingCycle, PaymentStatus status, String orgSchema);
    PaymentEntity getPaymentById(String paymentId);
}
