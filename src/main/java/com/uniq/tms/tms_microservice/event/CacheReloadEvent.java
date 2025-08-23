package com.uniq.tms.tms_microservice.event;

public class CacheReloadEvent {

    private final String cacheName;
    private final String orgId;
    private final String schema;

    public CacheReloadEvent(String cacheName, String orgId, String schema){
        this.cacheName = cacheName;
        this.orgId = orgId;
        this.schema = schema;
    }

    public String getCacheName(){
        return cacheName;
    }

    public String getOrgId(){
        return orgId;
    }

    public String getSchema(){ return schema;}
}
