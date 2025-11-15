package com.uniq.tms.tms_microservice.modules.leavemanagement.entity;

import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.AccuralType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.Compensation;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.EntitledType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.Status;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserGroupEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "timeoff_policies")
public class TimeoffPolicyEntity {

    @Id
    @Column(name = "policy_id", length = 20)
    private String policyId;

    @Column(name = "policy_name", nullable = false, length = 255)
    private String policyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "compensation", length = 10)
    private Compensation compensation;

    @Enumerated(EnumType.STRING)
    @Column(name = "accrual_type", length = 10)
    private AccuralType accrualType;

    @Column(name = "validity_start_date")
    private LocalDate validityStartDate;

    @Column(name = "validity_end_date")
    private LocalDate validityEndDate;

    @Column(name = "accrual_start_date")
    private LocalDate accrualStartDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "reset_frequency", length = 10)
    private AccuralType resetFrequency;

    @Column(name = "entitled_units")
    private Integer entitledUnits;

    @Enumerated(EnumType.STRING)
    @Column(name = "entitled_type", length = 10)
    private EntitledType entitledType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private Status status;

    @Column(name = "max_carry_forward_units")
    private Integer maxCarryForwardUnits;

    @Column(name = "is_carry_forward")
    private Boolean isCarryForward;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserPolicyEntity> userPolicies;

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public Compensation getCompensation() {
        return compensation;
    }

    public void setCompensation(Compensation compensation) {
        this.compensation = compensation;
    }

    public AccuralType getAccrualType() {
        return accrualType;
    }

    public void setAccrualType(AccuralType accrualType) {
        this.accrualType = accrualType;
    }

    public LocalDate getValidityStartDate() {
        return validityStartDate;
    }

    public void setValidityStartDate(LocalDate validityStartDate) {
        this.validityStartDate = validityStartDate;
    }

    public LocalDate getValidityEndDate() {
        return validityEndDate;
    }

    public void setValidityEndDate(LocalDate validityEndDate) {
        this.validityEndDate = validityEndDate;
    }

    public LocalDate getAccrualStartDate() {
        return accrualStartDate;
    }

    public void setAccrualStartDate(LocalDate accrualStartDate) {
        this.accrualStartDate = accrualStartDate;
    }

    public AccuralType getResetFrequency() {
        return resetFrequency;
    }

    public void setResetFrequency(AccuralType resetFrequency) {
        this.resetFrequency = resetFrequency;
    }

    public Integer getEntitledUnits() {
        return entitledUnits;
    }

    public void setEntitledUnits(Integer entitledUnits) {
        this.entitledUnits = entitledUnits;
    }

    public EntitledType getEntitledType() {
        return entitledType;
    }

    public void setEntitledType(EntitledType entitledType) {
        this.entitledType = entitledType;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Integer getMaxCarryForwardUnits() {
        return maxCarryForwardUnits;
    }

    public void setMaxCarryForwardUnits(Integer maxCarryForwardUnits) {
        this.maxCarryForwardUnits = maxCarryForwardUnits;
    }

    public Boolean getCarryForward() {
        return isCarryForward;
    }

    public void setCarryForward(Boolean carryForward) {
        isCarryForward = carryForward;
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

    public List<UserPolicyEntity> getUserPolicies() {
        return userPolicies;
    }

    public void setUserPolicies(List<UserPolicyEntity> userPolicies) {
        this.userPolicies = userPolicies;
    }
}
