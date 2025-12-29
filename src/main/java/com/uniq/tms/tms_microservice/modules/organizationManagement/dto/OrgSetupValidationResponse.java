package com.uniq.tms.tms_microservice.modules.organizationManagement.dto;

public class OrgSetupValidationResponse {

    private boolean hasLocation;
    private boolean hasWorkSchedule;
    private boolean hasCalendar;

    public OrgSetupValidationResponse(boolean hasLocation, boolean hasWorkSchedule, boolean hasCalendar) {
        this.hasLocation = hasLocation;
        this.hasWorkSchedule = hasWorkSchedule;
        this.hasCalendar = hasCalendar;
    }

    public boolean isHasLocation() {
        return hasLocation;
    }

    public void setHasLocation(boolean hasLocation) {
        this.hasLocation = hasLocation;
    }

    public boolean isHasWorkSchedule() {
        return hasWorkSchedule;
    }

    public void setHasWorkSchedule(boolean hasWorkSchedule) {
        this.hasWorkSchedule = hasWorkSchedule;
    }

    public boolean isHasCalendar() {
        return hasCalendar;
    }

    public void setHasCalendar(boolean hasCalendar) {
        this.hasCalendar = hasCalendar;
    }
}
