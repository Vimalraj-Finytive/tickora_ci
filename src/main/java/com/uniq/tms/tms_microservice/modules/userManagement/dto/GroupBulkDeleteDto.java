package com.uniq.tms.tms_microservice.modules.userManagement.dto;

import java.util.List;

public class GroupBulkDeleteDto {
    private List<Long> groupIds;

    public List<Long> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(List<Long> groupIds) {
        this.groupIds = groupIds;
    }
}
