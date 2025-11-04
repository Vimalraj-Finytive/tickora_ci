package com.uniq.tms.tms_microservice.modules.organizationManagement.model;

import java.math.BigDecimal;

public class PlanAnalyticsModel {

    private String planName;
    private long planCount;
    private BigDecimal usagePercentage;

    public PlanAnalyticsModel(String planName, Long count, BigDecimal usagePercentage) {
        this.planName = planName;
        this.planCount = count;
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
