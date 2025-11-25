package com.uniq.tms.tms_microservice.modules.leavemanagement.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class TimeOffPolicyBulkAssignRequestDto {
    private List<String> policyIds;
    private List<String> userIds;
    private List<Long> groupIds;

    @NotNull
    private LocalDate userValidFrom;

    private LocalDate userValidTo;

    public List<String> getPolicyIds() {
        return policyIds;
    }

    public void setPolicyIds(List<String> policyIds) {
        this.policyIds = policyIds;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public List<Long> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(List<Long> groupIds) {
        this.groupIds = groupIds;
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
