package com.uniq.tms.tms_microservice.modules.userManagement.model;

import java.util.List;
import java.util.Map;

public class GroupResponse {

    private Long groupId;
    private String groupName;
    private String location;
    private String workSchedule;
    private List<Map<String, Object>> groupMember;

    public List<Map<String, Object>> getGroupMember() {
        return groupMember;
    }

    public void setGroupMember(List<Map<String, Object>> groupMember) {
        this.groupMember = groupMember;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWorkSchedule() {
        return workSchedule;
    }

    public void setWorkSchedule(String workSchedule) {
        this.workSchedule = workSchedule;
    }
}
