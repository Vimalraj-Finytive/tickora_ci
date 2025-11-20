package com.uniq.tms.tms_microservice.modules.leavemanagement.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
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
    private BigDecimal  totalUnits;

    @Column(name = "expired_units", columnDefinition = "NUMERIC(10,2) DEFAULT 0")
    private BigDecimal expiredUnits ;

    @Column(name = "leave_taken_units", columnDefinition = "NUMERIC(10,2) DEFAULT 0")
    private BigDecimal  leaveTakenUnits;

    @Column(name = "balance_units")
    private BigDecimal  balanceUnits;

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

    public BigDecimal getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(BigDecimal totalUnits) {
        this.totalUnits = totalUnits;
    }

    public BigDecimal getExpiredUnits() {
        return expiredUnits;
    }

    public void setExpiredUnits(BigDecimal expiredUnits) {
        this.expiredUnits = expiredUnits;
    }

    public BigDecimal getLeaveTakenUnits() {
        return leaveTakenUnits;
    }

    public void setLeaveTakenUnits(BigDecimal leaveTakenUnits) {
        this.leaveTakenUnits = leaveTakenUnits;
    }

    public BigDecimal getBalanceUnits() {
        return balanceUnits;
    }

    public void setBalanceUnits(BigDecimal balanceUnits) {
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
