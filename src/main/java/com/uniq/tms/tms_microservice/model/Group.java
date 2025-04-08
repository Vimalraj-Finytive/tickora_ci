package com.uniq.tms.tms_microservice.model;

import java.util.List;

public class Group {

    private Long groupId;
    private String groupName;
    private List<Long> managerIds;
    private Long workScheduleId;
    private Long locationId;
    private List<Long> groupMemberIds;

    public List<Long> getGroupMemberIds() {
        return groupMemberIds;
    }

    public void setGroupMemberIds(List<Long> groupMemberIds) {
        this.groupMemberIds = groupMemberIds;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<Long> getManagerIds() {
        return managerIds;
    }

    public void setManagerIds(List<Long> managerIds) {
        this.managerIds = managerIds;
    }

    public Long getWorkScheduleId() {
        return workScheduleId;
    }

    public void setWorkScheduleId(Long workScheduleId) {
        this.workScheduleId = workScheduleId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }
}
