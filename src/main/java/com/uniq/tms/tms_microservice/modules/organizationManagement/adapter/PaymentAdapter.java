package com.uniq.tms.tms_microservice.modules.organizationManagement.adapter;

import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PaymentEntity;

import java.math.BigDecimal;

public interface PaymentAdapter     {
    PaymentEntity createPayment(String orgId,String subId,String orderId, String billingCycle, BigDecimal subscriptionAmount, Integer subscribedUserCount, String planId, String orgSchema);
}
