package com.uniq.tms.tms_microservice.modules.userManagement.dto;

import java.time.LocalDate;
import java.util.List;

public class UserDto {

    private String userId;
    private String userName;
    private String email;
    private String mobileNumber;
    private Long roleId;
    private List<Long> locationId;
    private LocalDate dateOfJoining;
    private boolean isRegisterUser;
    private List<Long> groupId;
    private boolean active;
    private String workSchedule;
    private String requestApproverId;
    private String calendarId;
    private String policyId;

    public UserDto() {}

    public LocalDate getDateOfJoining() {
        return dateOfJoining;
    }

    public void setDateOfJoining(LocalDate dateOfJoining) {
        this.dateOfJoining = dateOfJoining;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
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

    public List<Long> getGroupId() {
        return groupId;
    }

    public void setGroupId(List<Long> groupId) {
        this.groupId = groupId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isRegisterUser() {
        return isRegisterUser;
    }

    public void setIsRegisterUser(boolean isRegisterUser) {
        this.isRegisterUser = isRegisterUser;
    }

    public void setRegisterUser(boolean registerUser) {
        isRegisterUser = registerUser;
    }

    public String getWorkSchedule() {
        return workSchedule;
    }

    public void setWorkSchedule(String workSchedule) {
        this.workSchedule = workSchedule;
    }

    public String getRequestApproverId() {
        return requestApproverId;
    }

    public void setRequestApproverId(String requestApproverId) {
        this.requestApproverId = requestApproverId;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(String calendarId) {
        this.calendarId = calendarId;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }
}
