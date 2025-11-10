package com.uniq.tms.tms_microservice.modules.organizationManagement.dto;

import java.math.BigDecimal;

public class UpdatePlanDto {
    private Integer subscribedUserCount;
    private BigDecimal totalSubscriptionAmount;
    private String orderID;
    private Boolean status;

    public Integer getSubscribedUserCount() {
        return subscribedUserCount;
    }

    public void setSubscribedUserCount(Integer subscribedUserCount) {
        this.subscribedUserCount = subscribedUserCount;
    }

    public BigDecimal getTotalSubscriptionAmount() {
        return totalSubscriptionAmount;
    }

    public void setTotalSubscriptionAmount(BigDecimal totalSubscriptionAmount) {
        this.totalSubscriptionAmount = totalSubscriptionAmount;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
