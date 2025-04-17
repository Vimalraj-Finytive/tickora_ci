package com.uniq.tms.tms_microservice.model;

import java.util.List;

public class AddMember {
    private Long groupId;
    private List<Long> userId;
    private String type;

    public Long getGroupId() {
        return this.groupId;
    }

    public AddMember() {
    }

    public AddMember(Long groupId, List<Long> userId, String type) {
        this.groupId = groupId;
        this.userId = userId;
        this.type = type;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public List<Long> getUserId() {
        return this.userId;
    }

    public String getType() {return type;}

    public void setType(String type) {this.type = type;}

    public void setUserId(List<Long> userId) {
        this.userId = userId;
    }
}
