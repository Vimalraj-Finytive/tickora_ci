package com.uniq.tms.tms_microservice.modules.userManagement.dto;

import java.util.List;

public class BulkUserLocationDto {

    private List<String> memberIds;
    private List<Long> locationIds;

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public List<Long> getLocationIds() {
        return locationIds;
    }

    public void setLocationIds(List<Long> locationIds) {
        this.locationIds = locationIds;
    }
}
