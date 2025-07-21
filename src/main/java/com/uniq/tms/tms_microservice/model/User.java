package com.uniq.tms.tms_microservice.model;

import java.time.LocalDate;
import java.util.List;

public class User {

    private Long userId;
    private String userName;
    private String email;
    private String mobileNumber;
    private String organizationId;
    private Long roleId;
    private List<Long> locationId;
    private LocalDate dateOfJoining;
    private boolean isRegisterUser;
    private List<Long> groupId;
    private boolean active;
    private String workSchedule;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public List<Long> getLocationId() {
        return locationId;
    }

    public void setLocationId(List<Long> locationId) {
        this.locationId = locationId;
    }

    public boolean isRegisterUser() {
        return isRegisterUser;
    }

    public void setRegisterUser(boolean registerUser) {
        isRegisterUser = registerUser;
    }

    public List<Long> getGroupId() {
        return groupId;
    }

    public void setGroupId(List<Long> groupId) {
        this.groupId = groupId;
    }

    public String getWorkSchedule() {
        return workSchedule;
    }

    public void setWorkSchedule(String workSchedule) {
        this.workSchedule = workSchedule;
    }
}
