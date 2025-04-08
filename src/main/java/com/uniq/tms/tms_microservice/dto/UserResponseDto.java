package com.uniq.tms.tms_microservice.dto;

import java.time.LocalDate;

public class UserResponseDto {

    private Long userId;
    private String userName;
    private String email;
    private String mobile_number;
    private String groupName;
    private String roleName;
    private String locationName;
    private LocalDate dateOfJoining;

    public UserResponseDto(Long userId, String userName, String email, String mobile, String teamName, String roleName, String locationName, LocalDate dateOfJoining) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.mobile_number = mobile;
        this.groupName = teamName;
        this.roleName = roleName;
        this.locationName = locationName;
        this.dateOfJoining = dateOfJoining;
    }

    public LocalDate getDateOfJoining() {
        return dateOfJoining;
    }

    public void setDateOfJoining(LocalDate dateOfJoining) {
        this.dateOfJoining = dateOfJoining;
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
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getMobile_number() {
        return mobile_number;
    }
    public void setMobile_number(String mobile_number) {
        this.mobile_number = mobile_number;
    }

    public String getGroupName() {
        return groupName;
    }
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    public String getRoleName() {
        return roleName;
    }
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    public String getLocationName() {
        return locationName;
    }
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

}
