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
    private String requestApproverName;
    private String payrollName;
    private String organizationName;
    private String orgType;

    public UserResponseDto() {
    }

    public UserResponseDto(
            String userId,
            String userName,
            String email,
            String mobileNumber,
            String scheduleName,
            String groupName,
            String roleName,
            String locationName,
            LocalDate dateOfJoining,
            String secName,
            String secMobile,
            String secEmail,
            String relation,
            String policyId,
            String policyName,
            LocalDate validFrom,
            LocalDate validTo,
            String calendarName,
            String requestApproverName,
            String payRollName,
            String organizationName,
            String orgType
    ) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.scheduleName = scheduleName;
        this.groupName = new ArrayList<>();
        if (groupName != null) this.groupName.add(groupName);
        this.roleName = roleName;
        this.locationName = new ArrayList<>();
        if (locationName != null) this.locationName.add(locationName);

        this.dateOfJoining = dateOfJoining;
        if (secName != null || secMobile != null || secEmail != null) {
            this.secondaryDetails = new SecondaryDetailsDto();
            this.secondaryDetails.setUserName(secName);
            this.secondaryDetails.setMobile(secMobile);
            this.secondaryDetails.setEmail(secEmail);
            this.secondaryDetails.setRelation(relation);
        }
        this.policies = new ArrayList<>();
        if (policyName != null) {
            this.policies.add(new UserPolicyDto(policyId,policyName, validFrom, validTo));
        }
        this.calendarName = calendarName;
        this.requestApproverName = requestApproverName;
        this.payrollName = payRollName;
        this.organizationName = organizationName;
        this.orgType = orgType;
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
}
