package com.uniq.tms.tms_microservice.modules.organizationManagement.model;

import java.math.BigDecimal;

public class UpdatePlan {
    private Integer subscribedUserCount;
    private BigDecimal amount;
    private String orderId;
    private Boolean success;

    public Integer getSubscribedUserCount() {
        return subscribedUserCount;
    }

    public void setSubscribedUserCount(Integer subscribedUserCount) {
        this.subscribedUserCount = subscribedUserCount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}

