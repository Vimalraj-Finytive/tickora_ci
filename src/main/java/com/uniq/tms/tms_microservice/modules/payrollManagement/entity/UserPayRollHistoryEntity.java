package com.uniq.tms.tms_microservice.modules.payrollManagement.entity;

import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_payroll_history")
public class UserPayRollHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "action_at")
    LocalDateTime actionAt;

    @Column(name = "action_type")
    String actionType;

    @Column(name = "action_by")
    String actionBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_payroll_amount_id", nullable = false)
    private UserPayRollAmountEntity userPayrollAmount;

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

    public UserPayRollAmountEntity getUserPayrollAmount() {
        return userPayrollAmount;
    }

    public void setUserPayrollAmount(UserPayRollAmountEntity userPayrollAmount) {
        this.userPayrollAmount = userPayrollAmount;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
}
