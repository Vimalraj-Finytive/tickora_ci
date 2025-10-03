package com.uniq.tms.tms_microservice.modules.userManagement.dto;

import java.util.List;

public class EditUserDto {

    private List<String> userId;
    private boolean active;
    private String comments;

    public List<String> getUserId() {
        return userId;
    }

    public void setUserId(List<String> userId) {
        this.userId = userId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
