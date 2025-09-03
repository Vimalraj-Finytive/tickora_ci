package com.uniq.tms.tms_microservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserClockStatusDto {

    private String userId;
    @JsonProperty("isFaceMatch")
    private boolean faceMatch;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isFaceMatch() {
        return faceMatch;
    }

    public void setFaceMatch(boolean faceMatch) {
        this.faceMatch = faceMatch;
    }
}
