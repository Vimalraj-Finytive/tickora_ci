package com.uniq.tms.tms_microservice.modules.organizationManagement.model;

import java.math.BigDecimal;

public class OrganizationCountResponseModel {

    private long currentMonthCount;
    private long previousMonthCount;
    private BigDecimal currentMonthPercentage;
    private BigDecimal previousMonthPercentage;

    public OrganizationCountResponseModel(long currentMonthCount, long previousMonthCount, BigDecimal currentMonthPercentage, BigDecimal previousMonthPercentage) {
        this.currentMonthCount = currentMonthCount;
        this.previousMonthCount = previousMonthCount;
        this.currentMonthPercentage = currentMonthPercentage;
        this.previousMonthPercentage = previousMonthPercentage;
    }

    public long getCurrentMonthCount() {
        return currentMonthCount;
    }

    public void setCurrentMonthCount(long currentMonthCount) {
        this.currentMonthCount = currentMonthCount;
    }

    public long getPreviousMonthCount() {
        return previousMonthCount;
    }

    public void setPreviousMonthCount(long previousMonthCount) {
        this.previousMonthCount = previousMonthCount;
    }

    public BigDecimal getCurrentMonthPercentage() {
        return currentMonthPercentage;
    }

    public void setCurrentMonthPercentage(BigDecimal currentMonthPercentage) {
        this.currentMonthPercentage = currentMonthPercentage;
    }

    public BigDecimal getPreviousMonthPercentage() {
        return previousMonthPercentage;
    }

    public void setPreviousMonthPercentage(BigDecimal previousMonthPercentage) {
        this.previousMonthPercentage = previousMonthPercentage;
    }

}
