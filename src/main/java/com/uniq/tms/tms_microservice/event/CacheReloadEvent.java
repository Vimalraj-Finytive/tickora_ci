package com.uniq.tms.tms_microservice.event;

public class CacheReloadEvent {

    private final String cacheName;
    private final Long orgId;

    public CacheReloadEvent(String cacheName, Long orgId){
        this.cacheName = cacheName;
        this.orgId = orgId;
    }

    public String getCacheName(){
        return cacheName;
    }

    public Long getOrgId(){
        return orgId;
    }
}
