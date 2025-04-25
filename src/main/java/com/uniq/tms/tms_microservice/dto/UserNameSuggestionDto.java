package com.uniq.tms.tms_microservice.dto;

public class UserNameSuggestionDto {
    private Long userId;
    private String userName;

    public UserNameSuggestionDto(Long userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
