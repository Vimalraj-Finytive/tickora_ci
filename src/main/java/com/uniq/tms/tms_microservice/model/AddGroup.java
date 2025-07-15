package com.uniq.tms.tms_microservice.model;

import java.util.List;

public class AddGroup {

    private String groupName;
    private Long locationId;
    private List<Long> supervisorsId;
    private String type;
    private String workScheduleId;

    public String getType() {return type; }

    public void setType(String type) {this.type = type; }

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
