package com.uniq.tms.tms_microservice.dto;

public class TimesheetStatusDto {

    private String statusId;
    private String statusName;

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
}
