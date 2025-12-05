package com.uniq.tms.tms_microservice.modules.payrollManagement.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payroll")
public class PayRollEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "payroll_name", nullable = false)
    private String payrollName;

    @Column(name = "yearly_salary", nullable = false)
    private BigDecimal yearlySalary;

    @Column(name = "monthly_salary", nullable = false)
    private BigDecimal monthlySalary;

    @Column(name = "pf", nullable = false)
    private BigDecimal pf;

    @Column(name = "others", nullable = false)
    private BigDecimal others;

    @Column(name = "overtime_amount", nullable = false)
    private BigDecimal overtimeAmount;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "payroll", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserPayRollEntity> users = new ArrayList<>();

    @OneToMany(mappedBy = "payroll", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserPayRollAmountEntity> userPayrollAmounts = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPayrollName() {
        return payrollName;
    }

    public void setPayrollName(String payrollName) {
        this.payrollName = payrollName;
    }

    public BigDecimal getPf() {
        return pf;
    }

    public void setPf(BigDecimal pf) {
        this.pf = pf;
    }

    public BigDecimal getYearlySalary() {
        return yearlySalary;
    }

    public void setYearlySalary(BigDecimal yearlySalary) {
        this.yearlySalary = yearlySalary;
    }

    public BigDecimal getMonthlySalary() {
        return monthlySalary;
    }

    public void setMonthlySalary(BigDecimal monthlySalary) {
        this.monthlySalary = monthlySalary;
    }

    public BigDecimal getOthers() {
        return others;
    }

    public void setOthers(BigDecimal others) {
        this.others = others;
    }

    public BigDecimal getOvertimeAmount() {
        return overtimeAmount;
    }

    public void setOvertimeAmount(BigDecimal overtimeAmount) {
        this.overtimeAmount = overtimeAmount;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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

    public List<UserPayRollEntity> getUsers() {
        return users;
    }

    public void setUsers(List<UserPayRollEntity> users) {
        this.users = users;
    }

    public List<UserPayRollEntity> getUserMappings() {
        return users;
    }

    public void setUserMappings(List<UserPayRollEntity> userMappings) {
        this.users = userMappings;
    }

    public List<UserPayRollAmountEntity> getUserPayrollAmounts() {
        return userPayrollAmounts;
    }

    public void setUserPayrollAmounts(List<UserPayRollAmountEntity> userPayrollAmounts) {
        this.userPayrollAmounts = userPayrollAmounts;
    }

}
