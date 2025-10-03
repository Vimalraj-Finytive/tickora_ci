package com.uniq.tms.tms_microservice.modules.userManagement.dto;

import com.uniq.tms.tms_microservice.modules.locationManagement.dto.LocationDto;

public class UserGroupProfileDto {
    private Long groupId;
    private String groupName;
    private LocationDto location;
    private String workSchedule;
    private String userType;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public LocationDto getLocation() {
        return location;
    }

    public void setLocation(LocationDto location) {
        this.location = location;
    }

    public String getWorkSchedule() {
        return workSchedule;
    }

    public void setWorkSchedule(String workSchedule) {
        this.workSchedule = workSchedule;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
