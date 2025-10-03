package com.uniq.tms.tms_microservice.modules.userManagement.model;

import java.util.List;

public class UserLocation {
    private String userId;
    private List<Long> locationId;

    public UserLocation(List<Long> locationId, String userId) {
        this.locationId = locationId;
        this.userId = userId;
    }

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
