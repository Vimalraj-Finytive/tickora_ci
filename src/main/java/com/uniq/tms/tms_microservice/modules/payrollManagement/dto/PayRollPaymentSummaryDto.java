package com.uniq.tms.tms_microservice.modules.payrollManagement.dto;

import java.math.BigDecimal;

public class PayRollPaymentSummaryDto {

    BigDecimal totalPayment;
    int paidCount;
    int UnpaidCount;
    int failedCount;

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
        return UnpaidCount;
    }

    public void setUnpaidCount(int unpaidCount) {
        UnpaidCount = unpaidCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }
}
