package com.uniq.tms.tms_microservice.modules.payrollManagement.model;

import java.math.BigDecimal;

public class PayRollPaymentSummary {

    BigDecimal totalPayment;
    int paidCount;
    int unpaidCount;
    int failedCount;

    public PayRollPaymentSummary(BigDecimal totalPayment, int paidCount,
                                 int unpaidCount, int failedCount){
        this.totalPayment = totalPayment;
        this.paidCount = paidCount;
        this.unpaidCount = unpaidCount;
        this.failedCount = failedCount;
    }

    public BigDecimal getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(BigDecimal totalPayment) {
        this.totalPayment = totalPayment;
    }

    public int getPaidCount() {
        return paidCount;
    }

    public void setPaidCount(int paidCount) {
        this.paidCount = paidCount;
    }

    public int getUnpaidCount() {
        return unpaidCount;
    }

    public void setUnpaidCount(int unpaidCount) {
        unpaidCount = unpaidCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }
}
