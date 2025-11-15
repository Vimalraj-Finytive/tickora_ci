package com.uniq.tms.tms_microservice.modules.userManagement.dto;

import java.util.List;

public class BulkRoleUpdate {

    private List<String> userIds;
    private Long roleId;

    public String getMessage() {
        return message;
    }

    public void setMessage(String massage) {
        this.message = massage;
    }

    private String message;

    public List<String>  getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}

