package com.uniq.tms.tms_microservice.model;

import java.util.List;

public class AddGroup {

    private String groupName;
    private List<Long> managerIds;
    private Long locationId;
    private List<Long> supervisorsId;

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

    public List<Long> getManagerIds() {
        return managerIds;
    }

    public void setManagerIds(List<Long> managerIds) {
        this.managerIds = managerIds;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

}
