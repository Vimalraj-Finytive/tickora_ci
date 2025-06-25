package com.uniq.tms.tms_microservice.model;

import java.util.List;

public class UserLocation {
    private Long userId;
    private List<Long> locationId;

    public UserLocation(List<Long> locationId, Long userId) {
        this.locationId = locationId;
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<Long> getLocationId() {
        return locationId;
    }

    public void setLocationId(List<Long> locationId) {
        this.locationId = locationId;
    }
}
