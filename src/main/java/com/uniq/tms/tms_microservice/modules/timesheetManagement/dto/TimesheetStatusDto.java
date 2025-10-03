package com.uniq.tms.tms_microservice.modules.timesheetManagement.dto;

public class TimesheetStatusDto {

    private String statusId;
    private String statusName;
    private boolean statusEdit;

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

    public boolean isStatusEdit() {
        return statusEdit;
    }

    public void setStatusEdit(boolean statusEdit) {
        this.statusEdit = statusEdit;
    }
}
