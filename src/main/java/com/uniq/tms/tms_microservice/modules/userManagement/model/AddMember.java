package com.uniq.tms.tms_microservice.modules.userManagement.model;

import java.util.List;

public class AddMember {
    private Long groupId;
    private List<String> userId;
    private String type;

    public Long getGroupId() {
        return this.groupId;
    }

    public AddMember() {
    }

    public AddMember(Long groupId, List<String> userId, String type) {
        this.groupId = groupId;
        this.userId = userId;
        this.type = type;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public List<String> getUserId() {
        return this.userId;
    }

    public String getType() {return type;}

    public void setType(String type) {this.type = type;}

    public void setUserId(List<String> userId) {
        this.userId = userId;
    }
}
