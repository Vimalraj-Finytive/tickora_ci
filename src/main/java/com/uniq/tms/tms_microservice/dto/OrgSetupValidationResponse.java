package com.uniq.tms.tms_microservice.dto;

public class OrgSetupValidationResponse {

    private boolean hasLocation;
    private boolean hasWorkSchedule;

    public OrgSetupValidationResponse(boolean hasLocation, boolean hasWorkSchedule) {
        this.hasLocation = hasLocation;
        this.hasWorkSchedule = hasWorkSchedule;
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

}
