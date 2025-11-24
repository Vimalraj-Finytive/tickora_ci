package com.uniq.tms.tms_microservice.modules.leavemanagement.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.AccrualType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.Compensation;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.EntitledType;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "timeOff_policies")
public class TimeOffPolicyEntity {

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
    private AccrualType accrualType;

    @Column(name = "validity_start_date")
    private LocalDate validityStartDate;

    @Column(name = "validity_end_date")
    private LocalDate validityEndDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "accrual_start_date")
    private LocalDate accrualStartDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "reset_frequency", length = 10)
    private AccrualType resetFrequency;

    @Column(name = "entitled_units")
    private Integer entitledUnits;

    @Column(name = "entitled_hours")
    private Integer entitledHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "entitled_type", length = 10)
    private EntitledType entitledType;

    @Column(name = "is_active")
    private boolean isActive;

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

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL)
    private List<TimeOffRequestEntity> requests = new ArrayList<>();
    public Integer getEntitledHours() {
        return entitledHours;
    }

    public void setEntitledHours(Integer entitledHours) {
        this.entitledHours = entitledHours;
    }

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

    public AccrualType getAccrualType() {
        return accrualType;
    }

    public void setAccrualType(AccrualType accrualType) {
        this.accrualType = accrualType;
    }

    public void setResetFrequency(AccrualType resetFrequency) {
        this.resetFrequency = resetFrequency;
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

    public AccrualType getResetFrequency() {
        return resetFrequency;
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


    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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

    public List<TimeOffRequestEntity> getRequests() {
        return requests;
    }

    public void setRequests(List<TimeOffRequestEntity> requests) {
        this.requests = requests;
    }

}
