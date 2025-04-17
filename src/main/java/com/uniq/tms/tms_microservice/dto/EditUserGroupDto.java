package com.uniq.tms.tms_microservice.dto;

public class EditUserGroupDto {
    private Long groupId;
    private Long userId;
    private String type;

    public EditUserGroupDto() {
    }
    public EditUserGroupDto(Long groupId, Long userId, String type) {
        this.groupId = groupId;
        this.userId = userId;
        this.type = type;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}
