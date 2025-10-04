package com.uniq.tms.tms_microservice.modules.locationManagement.dto;

import java.util.List;

public class UserLocationDto {

    private  String userId;
    private List<Long> locationId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Long> getLocationId() {
        return locationId;
    }

    public void setLocationId(List<Long> locationId) {
        this.locationId = locationId;
    }
}
