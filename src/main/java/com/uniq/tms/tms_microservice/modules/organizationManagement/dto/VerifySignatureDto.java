package com.uniq.tms.tms_microservice.modules.organizationManagement.dto;

import org.springframework.web.bind.annotation.RequestParam;

public class VerifySignatureDto {
    String orderId;
    String paymentId;
    String signature;

    public VerifySignatureDto(String orderId, String paymentId, String signature) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.signature = signature;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
