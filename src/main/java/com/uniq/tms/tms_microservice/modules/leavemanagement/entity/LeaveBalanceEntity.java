package com.uniq.tms.tms_microservice.modules.leavemanagement.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "leave_balance")
public class LeaveBalanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_balance_id")
    private Long leaveBalanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private TimeOffPolicyEntity policy;

    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;

    @Column(name = "period_start_date")
    private LocalDate periodStartDate;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "total_units")
    private Double totalUnits;

    @Column(name = "expired_units")
    private Double expiredUnits = 0.0;

    @Column(name = "leave_taken_units")
    private Double leaveTakenUnits = 0.0;

    @Column(name = "balance_units")
    private Double balanceUnits;

    @Column(name = "next_accrual_date")
    private LocalDate nextAccrualDate;

    @Column(name = "last_accrual_date")
    private LocalDate lastAccrualDate;

    @Column(name = "carry_forward_units")
    private Double carryForwardUnits = 0.0;


    public Long getLeaveBalanceId() {
        return leaveBalanceId;
    }

    public void setLeaveBalanceId(Long leaveBalanceId) {
        this.leaveBalanceId = leaveBalanceId;
    }

    public TimeOffPolicyEntity getPolicy() {
        return policy;
    }

    public void setPolicy(TimeOffPolicyEntity policy) {
        this.policy = policy;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
