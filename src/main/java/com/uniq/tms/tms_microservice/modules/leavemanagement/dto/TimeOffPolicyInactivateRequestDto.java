package com.uniq.tms.tms_microservice.modules.leavemanagement.dto;

public class TimeOffPolicyInactivateRequestDto {

    private Boolean isActive;

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
