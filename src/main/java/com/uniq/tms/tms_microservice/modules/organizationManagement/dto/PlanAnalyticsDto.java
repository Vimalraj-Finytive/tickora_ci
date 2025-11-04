package com.uniq.tms.tms_microservice.modules.organizationManagement.dto;

import java.math.BigDecimal;

public class PlanAnalyticsDto {
    private String planName;
    private long planCount;
    private BigDecimal usagePercentage;

    public PlanAnalyticsDto(String planName, long planCount, BigDecimal usagePercentage) {
        this.planName = planName;
        this.planCount = planCount;
        this.usagePercentage = usagePercentage;
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
