package com.uniq.tms.tms_microservice.dto;

import java.util.List;

public class AddMemberDto {

    private Long groupId;
    private List<String> userId;
    private String type;

    public List<String> getUserId() {
        return userId;
    }

    public String getType() {return type;}

    public Long getGroupId() {
        return this.groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public void setType(String type) {this.type = type;}

    public void setUserId(List<String> userId) {
        this.userId = userId;
    }
}
