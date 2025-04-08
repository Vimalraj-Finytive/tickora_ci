package com.uniq.tms.tms_microservice.model;

import java.util.List;
import java.util.Map;

public class GroupResponse {

    private Long groupId;
    private String groupName;
    private List<String> managerIds;
    private String location;
    private List<Map<String, Object>> groupmember;

    public List<Map<String, Object>> getGroupmember() {
        return groupmember;
    }

    public void setGroupmember(List<Map<String, Object>> groupmember) {
        this.groupmember = groupmember;
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

    public List<String> getManagerIds() {
        return managerIds;
    }

    public void setManagerIds(List<String> managerIds) {
        this.managerIds = managerIds;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


}
