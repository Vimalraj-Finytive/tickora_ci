package com.uniq.tms.tms_microservice.modules.organizationManagement.dto;

import java.math.BigDecimal;

public class PlanAnalyticsDto {
    private String planId;
    private String planName;
    private long planCount;
    private BigDecimal usagePercentage;

    public PlanAnalyticsDto(String planId, String planName, long planCount, BigDecimal usagePercentage) {
        this.planId = planId;
        this.planName = planName;
        this.planCount = planCount;
        this.usagePercentage = usagePercentage;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public long getPlanCount() {
        return planCount;
    }

    public void setPlanCount(long planCount) {
        this.planCount = planCount;
    }

    public BigDecimal getUsagePercentage() {
        return usagePercentage;
    }

    public void setUsagePercentage(BigDecimal usagePercentage) {
        this.usagePercentage = usagePercentage;
    }
}
