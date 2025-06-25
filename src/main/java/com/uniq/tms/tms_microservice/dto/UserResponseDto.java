package com.uniq.tms.tms_microservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserResponseDto {

    private Long userId;
    private String userName;
    private String email;
    private String mobileNumber;
    private List<String> groupName;
    private String roleName;
    private List<String> locationName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfJoining;
    private SecondaryDetailsDto secondaryDetails;

    public UserResponseDto() {
    }

    public UserResponseDto(Long userId, String userName, String email, String mobileNumber,List<String> groupName, String roleName, LocalDate dateOfJoining, List<String> locationName) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.groupName = new ArrayList<>();
        this.groupName.addAll(groupName);
        this.roleName = roleName;
        this.dateOfJoining = dateOfJoining;
        this.locationName = new ArrayList<>();
        this.locationName.addAll(locationName);
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
}
