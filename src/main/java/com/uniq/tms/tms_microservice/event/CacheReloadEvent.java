package com.uniq.tms.tms_microservice.event;

public class CacheReloadEvent {

    private final String cacheName;
    private final String orgId;

    public CacheReloadEvent(String cacheName, String orgId){
        this.cacheName = cacheName;
        this.orgId = orgId;
    }

    public String getCacheName(){
        return cacheName;
    }

    public String getOrgId(){
        return orgId;
    }
}
