package com.uniq.tms.tms_microservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public class AddGroupDto {

    private String groupName;
    private Long locationId;
    private List<Long> supervisorsId;
    private String type;
    private Long workScheduleId;

    public List<Long> getSupervisorsId() {
        return supervisorsId;
    }

    public void setSupervisorsId(List<Long> supervisorsId) {
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

    public Long getWorkScheduleId() {
        return workScheduleId;
    }

    public void setWorkScheduleId(Long workScheduleId) {
        this.workScheduleId = workScheduleId;
    }
}
