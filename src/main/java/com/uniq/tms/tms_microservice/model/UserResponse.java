package com.uniq.tms.tms_microservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uniq.tms.tms_microservice.dto.SecondaryDetailsDto;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserResponse {

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
    private String scheduleName;

    public UserResponse(Long userId, String userName, String email, String mobileNumber, String scheduleName, String groupName, String roleName,
                        String locationName, LocalDate dateOfJoining,String secName, String secMobile, String secEmail, String relation) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.scheduleName = scheduleName;
        this.groupName = new ArrayList<>();
        this.groupName.add(groupName);
        this.roleName = roleName;
        this.locationName = new ArrayList<>();
        this.locationName.add(locationName);
        this.dateOfJoining = dateOfJoining;

        if (secName != null && secMobile != null){
            this.secondaryDetails = new SecondaryDetailsDto();
            this.secondaryDetails.setUserName(secName);
            this.secondaryDetails.setMobile(secMobile);
            this.secondaryDetails.setEmail(secEmail);
            this.secondaryDetails.setRelation(relation);
        }
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

    public LocalDate getDateOfJoining() {
        return dateOfJoining;
    }

    public void setDateOfJoining(LocalDate dateOfJoining) {
        this.dateOfJoining = dateOfJoining;
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
}
