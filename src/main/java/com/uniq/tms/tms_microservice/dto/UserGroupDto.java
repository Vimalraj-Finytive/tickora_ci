package com.uniq.tms.tms_microservice.dto;

import java.time.LocalDate;
import java.util.List;

public class UserGroupDto {
    private Long userId;
    private String userName;
    private String role;
    private String type;
    private String email;
    private LocalDate dateOfJoining;
    private boolean active;
    private List<String> location;
    private List<String> groupName;

    public UserGroupDto() {}

    public UserGroupDto(Long userId, String userName, String role, String type, String email, LocalDate dateOfJoining, boolean active, List<String> location, List<String> groupName) {
        this.userId = userId;
        this.userName = userName;
        this.role = role;
        this.type = type;
        this.email = email;
        this.dateOfJoining = dateOfJoining;
        this.active = active;
        this.location = location;
        this.groupName = groupName;
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

    public boolean getActive() {
        return active;
    }

    public List<String> getLocation() {
        return location;
    }

    public void setLocation(List<String> location) {
        this.location = location;
    }

    public List<String> getGroupName() {
        return groupName;
    }

    public void setGroupName(List<String> groupName) {
        this.groupName = groupName;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
