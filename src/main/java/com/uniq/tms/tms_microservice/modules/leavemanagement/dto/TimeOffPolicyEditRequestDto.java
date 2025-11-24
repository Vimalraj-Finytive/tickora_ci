package com.uniq.tms.tms_microservice.modules.leavemanagement.dto;

public class TimeOffPolicyEditRequestDto {

    private String policyName;
    private String policyId;
    private Integer entitledUnits;
    private Integer entitledHours;
    private Boolean carryForward;
    private Integer maxCarryForwardUnits;

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
}

