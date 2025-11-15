package com.uniq.tms.tms_microservice.modules.payrollManagement.model;

import com.uniq.tms.tms_microservice.modules.payrollManagement.enums.PayRollStatusEnum;
import java.math.BigDecimal;

public class UserPayRollModel {
    private String userId;
    private BigDecimal totalPayrollAmount;
    private PayRollStatusEnum payrollStatus;
    private String notes;

    public String getUserId() {return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public BigDecimal getTotalPayrollAmount() { return totalPayrollAmount; }
    public void setTotalPayrollAmount(BigDecimal totalPayrollAmount) { this.totalPayrollAmount = totalPayrollAmount; }
    public PayRollStatusEnum getPayrollStatus() { return payrollStatus; }
    public void setPayrollStatus(PayRollStatusEnum payrollStatus) { this.payrollStatus = payrollStatus; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
