package com.uniq.tms.tms_microservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;

public class UserProfileResponse {

    private Long userId;
    private String userName;
    private String email;
    private String mobileNumber;
    private String roleName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfJoining;
    private LocationDto location;
    private List<UserGroupProfileDto> groupDtos;

    public UserProfileResponse(Long userId, String userName, String email, String mobileNumber, String roleName, LocalDate dateOfJoining, LocationDto location, List<UserGroupProfileDto> groupDtos) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.roleName = roleName;
        this.dateOfJoining = dateOfJoining;
        this.location = location;
        this.groupDtos = groupDtos;
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
    public String getRoleName() {
        return roleName;
    }
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    public LocationDto getLocation() {return location;}
    public void setLocation(LocationDto location) {this.location = location;}

    public List<UserGroupProfileDto> getGroupDtos() {
        return groupDtos;
    }

    public void setGroupDtos(List<UserGroupProfileDto> groupDtos) {
        this.groupDtos = groupDtos;
    }
}
