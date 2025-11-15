package com.uniq.tms.tms_microservice.modules.payrollManagement.dto;

import java.math.BigDecimal;

public class UserPayRollAmountDto {

    private String userId;
    private String userName;
     private BigDecimal unpaidLeaveDeduction;
     private String regularDays;
     private String regularHrs;
     private String overtimeHrs;
     private String totalHrs;
     private BigDecimal regularPayrollAmount;
     private BigDecimal overtimePayrollAmount;
     private BigDecimal totalPayrollAmount;
     private BigDecimal monthlyNetSalary;
     private String payrollStatus;
     private String notes;
     private BigDecimal totalAmount;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNotes() {
        return notes;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getUnpaidLeaveDeduction() {
        return unpaidLeaveDeduction;
    }

    public void setUnpaidLeaveDeduction(BigDecimal unpaidLeaveDeduction) {
        this.unpaidLeaveDeduction = unpaidLeaveDeduction;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRegularDays() {
        return regularDays;
    }

    public void setRegularDays(String regularDays) {
        this.regularDays = regularDays;
    }

    public String getRegularHrs() {
        return regularHrs;
    }

    public void setRegularHrs(String regularHrs) {
        this.regularHrs = regularHrs;
    }

    public String getOvertimeHrs() {
        return overtimeHrs;
    }

    public void setOvertimeHrs(String overtimeHrs) {
        this.overtimeHrs = overtimeHrs;
    }

    public String getTotalHrs() {
        return totalHrs;
    }

    public void setTotalHrs(String totalHrs) {
        this.totalHrs = totalHrs;
    }

    public BigDecimal getRegularPayrollAmount() {
        return regularPayrollAmount;
    }

    public void setRegularPayrollAmount(BigDecimal regularPayrollAmount) {
        this.regularPayrollAmount = regularPayrollAmount;
    }

    public BigDecimal getOvertimePayrollAmount() {
        return overtimePayrollAmount;
    }

    public void setOvertimePayrollAmount(BigDecimal overtimePayrollAmount) {
        this.overtimePayrollAmount = overtimePayrollAmount;
    }

    public BigDecimal getTotalPayrollAmount() {
        return totalPayrollAmount;
    }

    public void setTotalPayrollAmount(BigDecimal totalPayrollAmount) {
        this.totalPayrollAmount = totalPayrollAmount;
    }

    public BigDecimal getMonthlyNetSalary() {
        return monthlyNetSalary;
    }

    public void setMonthlyNetSalary(BigDecimal monthlyNetSalary) {
        this.monthlyNetSalary = monthlyNetSalary;
    }

    public String getPayrollStatus() {
        return payrollStatus;
    }

    public void setPayrollStatus(String payrollStatus) {
        this.payrollStatus = payrollStatus;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
