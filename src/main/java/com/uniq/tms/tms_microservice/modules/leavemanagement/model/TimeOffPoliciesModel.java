package com.uniq.tms.tms_microservice.modules.leavemanagement.model;

import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.AccrualType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.Compensation;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.EntitledType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TimeOffPoliciesModel {

    private String policyId;
    private String policyName;

    private Compensation compensation;
    private AccrualType accrualType;

    private LocalDate validityStartDate;
    private LocalDate validityEndDate;
    private LocalDate accrualStartDate;

    private AccrualType resetFrequency;

    private Integer entitledUnits;
    private EntitledType entitledType;

    private Status status;

    private Integer maxCarryForwardUnits;
    private Boolean isCarryForward;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<String> assignedUsernames;

    public List<String> getAssignedUsernames() {
        return assignedUsernames;
    }

    public void setAssignedUsernames(List<String> assignedUsernames) {
        this.assignedUsernames = assignedUsernames;
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

    public void setResetFrequency(AccrualType resetFrequency) {
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
}
