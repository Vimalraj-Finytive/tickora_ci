package com.uniq.tms.tms_microservice.event;

public class GroupCacheReloadEvent {

    private final Long orgId;

    public GroupCacheReloadEvent(Long orgId){
        this.orgId = orgId;
    }

    public Long getOrgId(){
        return orgId;
    }
}
