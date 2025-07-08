package com.uniq.tms.tms_microservice.event;

public class LocationCacheReloadEvent {

    private final Long orgId;

    public LocationCacheReloadEvent(Long orgId){
        this.orgId = orgId;
    }

    public Long getOrgId(){
        return orgId;
    }
}
