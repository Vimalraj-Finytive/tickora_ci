package com.uniq.tms.tms_microservice.modules.userManagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserResponseDto {

    private String userId;
    private String userName;
    private String email;
    private String mobileNumber;
    private List<String> groupName;
    private String roleName;
    private List<String> locationName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfJoining;
    private SecondaryDetailsDto secondaryDetails;
    private String scheduleName;
    private List<UserPolicyDto> policies;
    private String calendarName;
    private String calendarId;
    private String requestApproverName;
    private String payrollName;
    private String organizationName;
    private String orgType;

    public UserResponseDto() {
    }

    public UserResponseDto(UserResponseDto source) {
        if (source == null) {
            return;
        }
        this.userId = source.getUserId();
        this.userName = source.getUserName();
        this.email = source.getEmail();
        this.mobileNumber = source.getMobileNumber();
        this.roleName = source.getRoleName();
        this.dateOfJoining = source.getDateOfJoining();
        this.scheduleName = source.getScheduleName();
        this.calendarName = source.getCalendarName();
        this.calendarId = source.getCalendarId();
        this.requestApproverName = source.getRequestApproverName();
        this.payrollName = source.getPayrollName();
        this.organizationName = source.getOrganizationName();
        this.orgType = source.getOrgType();
        this.groupName = source.getGroupName() != null
                ? new ArrayList<>(source.getGroupName())
                : new ArrayList<>();
        this.locationName = source.getLocationName() != null
                ? new ArrayList<>(source.getLocationName())
                : new ArrayList<>();

        this.policies = source.getPolicies() != null
                ? new ArrayList<>(source.getPolicies())
                : new ArrayList<>();
        this.secondaryDetails = source.getSecondaryDetails();
    }

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
    public List<String> getGroupName() {
        return groupName;
    }
    public void setGroupName(List<String> groupName) {
        this.groupName = groupName;
    }
    public String getRoleName() {
        return roleName;
    }
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    public List<String> getLocationName() {
        return locationName;
    }
    public void setLocationName(List<String> locationName) {
        this.locationName = locationName;
    }
    public SecondaryDetailsDto getSecondaryDetails() {
        return secondaryDetails;
    }
    public void setSecondaryDetails(SecondaryDetailsDto secondaryDetails) {
        this.secondaryDetails = secondaryDetails;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public String getCalendarName() {
        return calendarName;
    }

    public void setCalendarName(String calendarName) {
        this.calendarName = calendarName;
    }

    public List<UserPolicyDto> getPolicies() {
        return policies;
    }

    public void setPolicies(List<UserPolicyDto> policies) {
        this.policies = policies;
    }

    public String getRequestApproverName() {
        return requestApproverName;
    }

    public void setRequestApproverName(String requestApproverName) {
        this.requestApproverName = requestApproverName;
    }

    public String getPayrollName() {
        return payrollName;
    }

    public void setPayrollName(String payrollName) {
        this.payrollName = payrollName;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrgType() {
        return orgType;
    }

    public void setOrgType(String orgType) {
        this.orgType = orgType;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(String calendarId) {
        this.calendarId = calendarId;
    }
}
