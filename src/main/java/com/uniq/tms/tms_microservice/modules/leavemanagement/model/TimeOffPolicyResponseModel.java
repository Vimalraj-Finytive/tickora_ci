package com.uniq.tms.tms_microservice.modules.leavemanagement.model;

import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.AccrualType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.Compensation;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.EntitledType;

import java.time.LocalDate;

public class TimeOffPolicyResponseModel {

    private String policyId;
    private String policyName;

    private Compensation compensation;
    private AccrualType accrualType;

    private LocalDate validityStartDate;
    private LocalDate validityEndDate;

    private EntitledType entitledType;
    private Integer entitledUnits;
    private Integer entitledHours;

    private Boolean carryForward;
    private Integer maxCarryForwardUnits;

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

    public EntitledType getEntitledType() {
        return entitledType;
    }

    public void setEntitledType(EntitledType entitledType) {
        this.entitledType = entitledType;
    }

    public Integer getEntitledUnits() {
        return entitledUnits;
    }

    public void setEntitledUnits(Integer entitledUnits) {
        this.entitledUnits = entitledUnits;
    }

    public Integer getEntitledHours() {
        return entitledHours;
    }

    public void setEntitledHours(Integer entitledHours) {
        this.entitledHours = entitledHours;
    }

    public Boolean getCarryForward() {
        return carryForward;
    }

    public void setCarryForward(Boolean carryForward) {
        this.carryForward = carryForward;
    }

    public Integer getMaxCarryForwardUnits() {
        return maxCarryForwardUnits;
    }

    public void setMaxCarryForwardUnits(Integer maxCarryForwardUnits) {
        this.maxCarryForwardUnits = maxCarryForwardUnits;
    }
}