package com.uniq.tms.tms_microservice.modules.leavemanagement.dto;

import java.time.LocalDate;

public class LeaveBalanceDto {
    private String policyName;
    private LocalDate periodStartDate;
    private LocalDate periodEnd;

    private Double totalUnits;
    private Double expiredUnits;
    private Double leaveTakenUnits;
    private Double balanceUnits;

    private LocalDate nextAccrualDate;
    private LocalDate lastAccrualDate;

    private Double carryForwardUnits;

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public LocalDate getPeriodStartDate() {
        return periodStartDate;
    }

    public void setPeriodStartDate(LocalDate periodStartDate) {
        this.periodStartDate = periodStartDate;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public Double getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(Double totalUnits) {
        this.totalUnits = totalUnits;
    }

    public Double getExpiredUnits() {
        return expiredUnits;
    }

    public void setExpiredUnits(Double expiredUnits) {
        this.expiredUnits = expiredUnits;
    }

    public Double getLeaveTakenUnits() {
        return leaveTakenUnits;
    }

    public void setLeaveTakenUnits(Double leaveTakenUnits) {
        this.leaveTakenUnits = leaveTakenUnits;
    }

    public Double getBalanceUnits() {
        return balanceUnits;
    }

    public void setBalanceUnits(Double balanceUnits) {
        this.balanceUnits = balanceUnits;
    }

    public LocalDate getNextAccrualDate() {
        return nextAccrualDate;
    }

    public void setNextAccrualDate(LocalDate nextAccrualDate) {
        this.nextAccrualDate = nextAccrualDate;
    }

    public LocalDate getLastAccrualDate() {
        return lastAccrualDate;
    }

    public void setLastAccrualDate(LocalDate lastAccrualDate) {
        this.lastAccrualDate = lastAccrualDate;
    }

    public Double getCarryForwardUnits() {
        return carryForwardUnits;
    }

    public void setCarryForwardUnits(Double carryForwardUnits) {
        this.carryForwardUnits = carryForwardUnits;
    }
}