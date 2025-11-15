package com.uniq.tms.tms_microservice.modules.leavemanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "leave_balance")
public class LeaveBalanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_balance_id")
    private Long leaveBalanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private TimeoffPolicyEntity policy;

    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;

    @Column(name = "period_start_date")
    private LocalDate periodStartDate;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "total_units")
    private Integer totalUnits;

    @Column(name = "expired_units")
    private Integer expiredUnits = 0;

    @Column(name = "leave_taken_units")
    private Integer leaveTakenUnits = 0;

    @Column(name = "balance_units")
    private Integer balanceUnits;

    @Column(name = "next_accrual_date")
    private LocalDate nextAccrualDate;

    @Column(name = "last_accrual_date")
    private LocalDate lastAccrualDate;

    @Column(name = "carry_forward_units")
    private Integer carryForwardUnits = 0;


    public Long getLeaveBalanceId() {
        return leaveBalanceId;
    }

    public void setLeaveBalanceId(Long leaveBalanceId) {
        this.leaveBalanceId = leaveBalanceId;
    }

    public TimeoffPolicyEntity getPolicy() {
        return policy;
    }

    public void setPolicy(TimeoffPolicyEntity policy) {
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

    public Integer getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(Integer totalUnits) {
        this.totalUnits = totalUnits;
    }

    public Integer getExpiredUnits() {
        return expiredUnits;
    }

    public void setExpiredUnits(Integer expiredUnits) {
        this.expiredUnits = expiredUnits;
    }

    public Integer getLeaveTakenUnits() {
        return leaveTakenUnits;
    }

    public void setLeaveTakenUnits(Integer leaveTakenUnits) {
        this.leaveTakenUnits = leaveTakenUnits;
    }

    public Integer getBalanceUnits() {
        return balanceUnits;
    }

    public void setBalanceUnits(Integer balanceUnits) {
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

    public Integer getCarryForwardUnits() {
        return carryForwardUnits;
    }

    public void setCarryForwardUnits(Integer carryForwardUnits) {
        this.carryForwardUnits = carryForwardUnits;
    }

}
