package com.uniq.tms.tms_microservice.dto;

import java.time.LocalDate;

public class UserGroupDto {
    private Long userId;
    private String userName;
    private String role;
    private String type;
    private String email;
    private LocalDate dateOfJoining;

    public UserGroupDto() {}

    public UserGroupDto(Long userId, String userName, String role, String type, LocalDate dateOfJoining, String email) {
        this.userId = userId;
        this.userName = userName;
        this.role = role;
        this.type = type;
        this.dateOfJoining = dateOfJoining;
        this.email = email;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDateOfJoining() {
        return dateOfJoining;
    }

    public void setDateOfJoining(LocalDate dateOfJoining) {
        this.dateOfJoining = dateOfJoining;
    }
}
