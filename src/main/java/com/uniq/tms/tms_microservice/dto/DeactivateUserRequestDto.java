package com.uniq.tms.tms_microservice.dto;

public class DeactivateUserRequestDto {

    private String userId;

    private String comments;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
