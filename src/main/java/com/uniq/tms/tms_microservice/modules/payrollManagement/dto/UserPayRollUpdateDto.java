package com.uniq.tms.tms_microservice.modules.payrollManagement.dto;

import com.uniq.tms.tms_microservice.modules.payrollManagement.enums.PayRollStatusEnum;
import java.math.BigDecimal;
import java.util.Map;

public class UserPayRollUpdateDto {

    private PayRollStatusEnum payrollStatus;
    private String notes;
    private  BigDecimal totalAmount;
    private Object bonus;

    public UserPayRollUpdateDto() {
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

    public Object getBonus() {
        return bonus;
    }

    public void setBonus(Object bonus) {
        this.bonus = bonus;
    }
}
