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
    private List<String> policyName;
    private String calendarName;

    public UserResponseDto() {
    }

    public UserResponseDto(String userId, String userName, String email, String mobileNumber, String scheduleName, List<String> groupName,
                           String roleName, LocalDate dateOfJoining, List<String> locationName, String secName, String secMobile, String secEmail, String relation,List<String> policyName,String calendarName) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.scheduleName = scheduleName;
        this.groupName = new ArrayList<>();
        this.groupName.addAll(groupName);
        this.roleName = roleName;
        this.dateOfJoining = dateOfJoining;
        this.locationName = new ArrayList<>();
        this.locationName.addAll(locationName);
        this.policyName = new ArrayList<>();
        this.policyName.addAll(policyName);
        this.calendarName =calendarName;

        if(secName!= null && secMobile != null){
            this.secondaryDetails = new SecondaryDetailsDto();
            this.secondaryDetails.setUserName(secName);
            this.secondaryDetails.setMobile(secMobile);
            this.secondaryDetails.setEmail(secEmail);
            this.secondaryDetails.setRelation(relation);
        }
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

    public List<String> getPolicyName() {
        return policyName;
    }

    public void setPolicyName(List<String> policyName) {
        this.policyName = policyName;
    }

    public String getCalendarName() {
        return calendarName;
    }

    public void setCalendarName(String calendarName) {
        this.calendarName = calendarName;
    }
}
