package com.uniq.tms.tms_microservice.modules.userManagement.dto;

import java.time.LocalDate;

public class UserPolicyDto {

    private String policyName;
    private LocalDate validFrom;
    private LocalDate validTo;

    public UserPolicyDto(){}

    public UserPolicyDto(String policyName, LocalDate validFrom, LocalDate validTo) {
        this.policyName = policyName;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }
}
