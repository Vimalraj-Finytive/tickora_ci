package com.uniq.tms.tms_microservice.model;

public class Group {

    private Long groupId;
    private String groupName;
    private String workScheduleId;
    private Long locationId;
    private String organizationId;

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

    public String getWorkScheduleId() {
        return workScheduleId;
    }

    public void setWorkScheduleId(String workScheduleId) {
        this.workScheduleId = workScheduleId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getOrganizationId() {return organizationId;}

    public void setOrganizationId(String organizationId) {this.organizationId = organizationId;}
}
