package com.uniq.tms.tms_microservice.dto;

import java.util.List;

public class AddMemberDto {

    private Long groupId;
    private List<Long> userId;
    private String type;

    public List<Long> getUserId() {
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

    public void setUserId(List<Long> userId) {
        this.userId = userId;
    }
}
