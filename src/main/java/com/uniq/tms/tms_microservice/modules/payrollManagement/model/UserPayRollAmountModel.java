package com.uniq.tms.tms_microservice.modules.payrollManagement.model;

import com.uniq.tms.tms_microservice.modules.payrollManagement.enums.PayRollStatusEnum;

import java.math.BigDecimal;

public class UserPayRollAmountModel {

    private String userId;
    private String userName;
    private BigDecimal unpaidLeaveDeduction;
    private Integer regularDays;
    private BigDecimal regularHrs;
    private BigDecimal overtimeHrs;
    private BigDecimal totalHrs;
    private BigDecimal regularPayrollAmount;
    private BigDecimal overtimePayrollAmount;
    private BigDecimal totalPayrollAmount;
    private BigDecimal monthlyNetSalary;
    private PayRollStatusEnum payrollStatus;
    private String notes;
    private BigDecimal totalAmount;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public BigDecimal getUnpaidLeaveDeduction() {
        return unpaidLeaveDeduction;
    }

    public void setUnpaidLeaveDeduction(BigDecimal unpaidLeaveDeduction) {
        this.unpaidLeaveDeduction = unpaidLeaveDeduction;
    }

    public Integer getRegularDays() {
        return regularDays;
    }

    public void setRegularDays(Integer regularDays) {
        this.regularDays = regularDays;
    }

    public BigDecimal getRegularHrs() {
        return regularHrs;
    }

    public void setRegularHrs(BigDecimal regularHrs) {
        this.regularHrs = regularHrs;
    }

    public BigDecimal getOvertimeHrs() {
        return overtimeHrs;
    }

    public void setOvertimeHrs(BigDecimal overtimeHrs) {
        this.overtimeHrs = overtimeHrs;
    }

    public BigDecimal getTotalHrs() {
        return totalHrs;
    }

    public void setTotalHrs(BigDecimal totalHrs) {
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

    public PayRollStatusEnum getPayrollStatus() {
        return payrollStatus;
    }

    public void setPayrollStatus(PayRollStatusEnum payrollStatus) {
        this.payrollStatus = payrollStatus;
    }

    public BigDecimal getMonthlyNetSalary() {
        return monthlyNetSalary;
    }

    public void setMonthlyNetSalary(BigDecimal monthlyNetSalary) {
        this.monthlyNetSalary = monthlyNetSalary;
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
