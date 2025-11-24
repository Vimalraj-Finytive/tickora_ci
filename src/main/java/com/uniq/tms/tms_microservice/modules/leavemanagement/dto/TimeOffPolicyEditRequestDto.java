package com.uniq.tms.tms_microservice.modules.leavemanagement.dto;


import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class TimeOffPolicyEditRequestDto {

    private String policyName;
    private String policyId;
    private Integer entitledUnits;
    private Integer entitledHours;
    private Boolean carryForward;
    private Integer maxCarryForwardUnits;

    @NotNull
    private LocalDate validityStartDate;

    private LocalDate validityEndDate;

    @NotNull
    private LocalDate userValidFrom;

    private LocalDate userValidTo;

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
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

    public LocalDate getUserValidFrom() {
        return userValidFrom;
    }

    public void setUserValidFrom(LocalDate userValidFrom) {
        this.userValidFrom = userValidFrom;
    }

    public LocalDate getUserValidTo() {
        return userValidTo;
    }

    public void setUserValidTo(LocalDate userValidTo) {
        this.userValidTo = userValidTo;
    }
}

