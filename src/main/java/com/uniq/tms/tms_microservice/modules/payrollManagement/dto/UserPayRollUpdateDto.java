package com.uniq.tms.tms_microservice.modules.payrollManagement.dto;

import com.uniq.tms.tms_microservice.modules.payrollManagement.enums.PayRollStatusEnum;
import java.math.BigDecimal;

public class UserPayRollUpdateDto {
    private String userId;
    private PayRollStatusEnum payrollStatus;
    private String notes;
    private  BigDecimal totalAmount;

    public UserPayRollUpdateDto() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public PayRollStatusEnum getPayrollStatus() {
        return payrollStatus;
    }

    public void setPayrollStatus(PayRollStatusEnum payrollStatus) {
        this.payrollStatus = payrollStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
