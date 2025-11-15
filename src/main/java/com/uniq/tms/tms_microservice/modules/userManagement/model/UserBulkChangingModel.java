package com.uniq.tms.tms_microservice.modules.userManagement.model;

import org.springframework.stereotype.Component;

@Component
public class UserBulkChangingModel {
    private String userId;
    private Long newRoleId;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String message;

    // getters & setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Long getNewRoleId() { return newRoleId; }
    public void setNewRoleId(Long newRoleId) { this.newRoleId = newRoleId; }
}

