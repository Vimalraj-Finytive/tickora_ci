package com.uniq.tms.tms_microservice.modules.payrollManagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_history")
public class PayRollHistoryEntity {
    @Id
    Integer id;

    @Column(name = "action_at")
    LocalDateTime actionAt;

    @Column(name = "action_type")
    String actionType;

    @Column(name = "action_by")
    String actionBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_id", nullable = false, unique = true)
    PayRollEntity payroll;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getActionAt() {
        return actionAt;
    }

    public void setActionAt(LocalDateTime actionAt) {
        this.actionAt = actionAt;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionBy() {
        return actionBy;
    }

    public void setActionBy(String actionBy) {
        this.actionBy = actionBy;
    }

    public PayRollEntity getPayroll() {
        return payroll;
    }

    public void setPayroll(PayRollEntity payroll) {
        this.payroll = payroll;
    }

}
