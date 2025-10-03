package com.uniq.tms.tms_microservice.modules.userManagement.dto;

import java.util.List;

public class AddGroupDto {

    private String groupName;
    private Long locationId;
    private List<String> supervisorsId;
    private String type;
    private String workScheduleId;

    public List<String> getSupervisorsId() {
        return supervisorsId;
    }

    public void setSupervisorsId(List<String> supervisorsId) {
        this.supervisorsId = supervisorsId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Long getLocationId() {
        return locationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) { this.type = type; }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getWorkScheduleId() {
        return workScheduleId;
    }

    public void setWorkScheduleId(String workScheduleId) {
        this.workScheduleId = workScheduleId;
    }
}
