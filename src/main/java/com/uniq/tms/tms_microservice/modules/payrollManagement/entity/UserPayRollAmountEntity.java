package com.uniq.tms.tms_microservice.modules.payrollManagement.entity;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.MonthlySummaryEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.enums.PayRollStatusEnum;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "user_payroll_amount")
public class UserPayRollAmountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "payroll_id",nullable = false)
    private PayRollEntity payroll;

    @Column(name = "month",nullable = false)
    String month;

    @Column(name = "unpaid_leave_deduction",nullable = false)
    BigDecimal unpaidLeaveDeduction;

    @Column(name = "regular_days",nullable = false)
    Integer regularDays;

    @Column(name = "regular_hrs",nullable = false)
    BigDecimal regularHrs;

    @Column(name = "overtime_hrs",nullable = false)
    BigDecimal overtimeHrs;

    @Column(name = "total_hrs",nullable = false)
    BigDecimal totalHrs;

    @Column(name = "regular_payroll_amount",nullable = false)
    BigDecimal regularPayrollAmount;

    @Column(name = "overtime_payroll_amount",nullable = false)
    BigDecimal overtimePayrollAmount;

    @Column(name = "total_payroll_amount",nullable = false)
    BigDecimal totalPayrollAmount;

    @Column(name="total_amount")
    BigDecimal totalAmount;


    @Column(name = "payroll_status")
    @Enumerated(EnumType.STRING)
    PayRollStatusEnum payrollStatus;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @Column(name = "notes")
    String notes;

    @Column(name = "bonus", columnDefinition = "text")
    private String bonus;

    @OneToMany(mappedBy = "userPayrollAmount")
    private List<UserPayRollHistoryEntity> userPayrollHistory = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public PayRollEntity getPayroll() {
        return payroll;
    }

    public void setPayroll(PayRollEntity payroll) {
        this.payroll = payroll;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<UserPayRollHistoryEntity> getUserPayrollHistory() {
        return userPayrollHistory;
    }

    public void setUserPayrollHistory(List<UserPayRollHistoryEntity> userPayrollHistory) {
        this.userPayrollHistory = userPayrollHistory;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getBonus() {
        return bonus;
    }

    public void setBonus(String bonus) {
        this.bonus = bonus;
    }
}
