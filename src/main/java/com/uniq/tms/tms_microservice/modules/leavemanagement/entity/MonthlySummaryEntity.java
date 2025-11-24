package com.uniq.tms.tms_microservice.modules.leavemanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_summary")
public class MonthlySummaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private TimeOffPolicyEntity policy;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "total_leaves_taken", nullable = false)
    private Integer totalLeavesTaken = 0;

    @Column(name = "paid_leaves_taken", nullable = false)
    private Integer paidLeavesTaken = 0;

    @Column(name = "unpaid_leaves_taken", nullable = false)
    private Integer unpaidLeavesTaken = 0;

    @Column(name = "total_units_available", nullable = false)
    private Integer totalUnitsAvailable = 0;

    @Column(name = "balance_units", nullable = false)
    private Integer balanceUnits = 0;

    @Column(name = "half_day_units", nullable = false)
    private Integer halfDayUnits = 0;

    @Column(name = "full_day_units", nullable = false)
    private Integer fullDayUnits = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public TimeOffPolicyEntity getPolicy() {
        return policy;
    }

    public void setPolicy(TimeOffPolicyEntity policy) {
        this.policy = policy;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getTotalLeavesTaken() {
        return totalLeavesTaken;
    }

    public void setTotalLeavesTaken(Integer totalLeavesTaken) {
        this.totalLeavesTaken = totalLeavesTaken;
    }

    public Integer getPaidLeavesTaken() {
        return paidLeavesTaken;
    }

    public void setPaidLeavesTaken(Integer paidLeavesTaken) {
        this.paidLeavesTaken = paidLeavesTaken;
    }

    public Integer getUnpaidLeavesTaken() {
        return unpaidLeavesTaken;
    }

    public void setUnpaidLeavesTaken(Integer unpaidLeavesTaken) {
        this.unpaidLeavesTaken = unpaidLeavesTaken;
    }

    public Integer getTotalUnitsAvailable() {
        return totalUnitsAvailable;
    }

    public void setTotalUnitsAvailable(Integer totalUnitsAvailable) {
        this.totalUnitsAvailable = totalUnitsAvailable;
    }

    public Integer getBalanceUnits() {
        return balanceUnits;
    }

    public void setBalanceUnits(Integer balanceUnits) {
        this.balanceUnits = balanceUnits;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getHalfDayUnits() {
        return halfDayUnits;
    }

    public void setHalfDayUnits(Integer halfDayUnits) {
        this.halfDayUnits = halfDayUnits;
    }

    public Integer getFullDayUnits() {
        return fullDayUnits;
    }

    public void setFullDayUnits(Integer fullDayUnits) {
        this.fullDayUnits = fullDayUnits;
    }
}
