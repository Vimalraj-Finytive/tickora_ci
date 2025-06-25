package com.uniq.tms.tms_microservice.dto;

import java.util.List;

public class UserLocationDto {

    private  Long userId;
    private List<Long> locationId;

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
