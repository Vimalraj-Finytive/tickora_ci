package com.uniq.tms.tms_microservice.modules.userManagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uniq.tms.tms_microservice.modules.locationManagement.dto.LocationDto;
import java.time.LocalDate;
import java.util.List;

public class UserProfileResponseDto {

    private String userId;
    private String userName;
    private String email;
    private String mobileNumber;
    private String roleName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfJoining;
    private List<LocationDto> location;
    private List<UserGroupProfileDto> groupDtos;
    private String organizationName;
    private String scheduleName;
    private String orgType;
    private List<UserPolicyDto> policies;
    private String calendarName;
    private String requestApproverName;
    private String payrollName;
    private List<ParentDto> parent;

    public UserProfileResponseDto(){}

    public UserProfileResponseDto(String userId, String userName, String email, String mobileNumber, String roleName,
                                  LocalDate dateOfJoining, List<LocationDto> location, List<UserGroupProfileDto> groupDtos,
                                  String organizationName, String scheduleName, String orgType,
                                  String calendarName, String requestApproverName, String payrollName,
                                  List<UserPolicyDto> policies,List<ParentDto> parent) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.roleName = roleName;
        this.dateOfJoining = dateOfJoining;
        this.location = location;
        this.groupDtos = groupDtos;
        this.organizationName = organizationName;
        this.scheduleName = scheduleName;
        this.orgType = orgType;
        this.calendarName = calendarName;
        this.requestApproverName = requestApproverName;
        this.payrollName = payrollName;
        this.policies = policies;
        this.parent = parent;
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
    public String getRoleName() {
        return roleName;
    }
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    public List<LocationDto> getLocation() {return location;}
    public void setLocation(List<LocationDto> location) {this.location = location;}
    public List<UserGroupProfileDto> getGroupDtos() {
        return groupDtos;
    }
    public void setGroupDtos(List<UserGroupProfileDto> groupDtos) {
        this.groupDtos = groupDtos;
    }
    public String getOrganizationName(){
        return organizationName;
    }
    public void setOrganizationName(String organizationName){
        this.organizationName = organizationName;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public String getOrgType() {
        return orgType;
    }

    public void setOrgType(String orgType) {
        this.orgType = orgType;
    }

    public List<UserPolicyDto> getPolicies() {
        return policies;
    }

    public void setPolicies(List<UserPolicyDto> policies) {
        this.policies = policies;
    }

    public String getCalendarName() {
        return calendarName;
    }

    public void setCalendarName(String calendarName) {
        this.calendarName = calendarName;
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

    public List<ParentDto> getParent() {
        return parent;
    }

    public void setParent(List<ParentDto> parent) {
        this.parent = parent;
    }
}
