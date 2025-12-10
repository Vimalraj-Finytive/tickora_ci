package com.uniq.tms.tms_microservice.modules.leavemanagement.model;

import java.time.LocalDate;
import java.util.List;

public class TimeOffPolicyBulkAssignModel {
    private String policyId;
    private List<String> userIds;
    private List<Long> groupIds;
    private LocalDate userValidFrom;

    public LocalDate getUserValidFrom() {
        return userValidFrom;
    }

    public void setUserValidFrom(LocalDate userValidFrom) {
        this.userValidFrom = userValidFrom;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
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
}
