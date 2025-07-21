package com.uniq.tms.tms_microservice.model;

public class TimesheetStatus {

    private String statusId;
    private String statusName;
    private boolean statusEdit;

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public String getstatusName() {
        return statusName;
    }

    public void setstatusName(String statusName) {
        this.statusName = statusName;
    }

    public boolean isStatusEdit() {
        return statusEdit;
    }

    public void setStatusEdit(boolean statusEdit) {
        this.statusEdit = statusEdit;
    }
}
