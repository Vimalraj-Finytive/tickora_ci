package com.uniq.tms.tms_microservice.modules.userManagement.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class DeactivateUserRequestDto {

    @NotEmpty(message="User Ids cannot be empty")
    private List<String> userIds;

    private String comments;

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}