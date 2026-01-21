package com.uniq.tms.tms_microservice.modules.leavemanagement.dto;

import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.GenderApplicability;

public class TimeOffPolicyTemplateDto {
    private String policyCode;
    private String policyName;
    private Integer entitledUnits;
    private GenderApplicability genderApplicability;

    public TimeOffPolicyTemplateDto(String policyCode, String policyName, Integer entitledUnits, GenderApplicability genderApplicability) {
        this.policyCode = policyCode;
        this.policyName = policyName;
        this.entitledUnits = entitledUnits;
        this.genderApplicability = genderApplicability;
    }

    public String getPolicyCode() {
        return policyCode;
    }

    public void setPolicyCode(String policyCode) {
        this.policyCode = policyCode;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public Integer getEntitledUnits() {
        return entitledUnits;
    }

    public void setEntitledUnits(Integer entitledUnits) {
        this.entitledUnits = entitledUnits;
    }

    public GenderApplicability getGenderApplicability() {
        return genderApplicability;
    }

    public void setGenderApplicability(GenderApplicability genderApplicability) {
        this.genderApplicability = genderApplicability;
    }

}
