package com.uniq.tms.tms_microservice.modules.leavemanagement.model;

import java.util.List;

public class TimeOffPolicyBulkAssignModel {
    private List<String> policyIds;
    private List<String> userIds;
    private List<Long> groupIds;

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
}
