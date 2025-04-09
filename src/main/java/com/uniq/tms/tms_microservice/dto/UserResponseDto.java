package com.uniq.tms.tms_microservice.dto;

import java.time.LocalDate;

public class UserResponseDto {

    private Long userId;
    private String userName;
    private String email;
    private String mobileNumber;
    private String groupName;
    private Long orgId;
    private String roleName;
    private String locationName;
    private LocalDate dateOfJoining;

    public UserResponseDto(Long userId, String userName, String email, String mobileNumber, String groupName, Long orgId, String roleName, LocalDate dateOfJoining, String locationName) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.groupName = groupName;
        this.orgId = orgId;
        this.roleName = roleName;
        this.dateOfJoining = dateOfJoining;
        this.locationName = locationName;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
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

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
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
