package com.uniq.tms.tms_microservice.modules.organizationManagement.dto;

import java.math.BigDecimal;

public class UpgradePlanDto {

    private String planId;
    private Integer subscribedUserCount;
    private BigDecimal totalSubscriptionAmount;

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

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
}
