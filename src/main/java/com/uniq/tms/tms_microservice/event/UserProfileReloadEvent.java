package com.uniq.tms.tms_microservice.event;

public class UserProfileReloadEvent {

    private final Long orgId;

    public UserProfileReloadEvent(Long orgId){
        this.orgId = orgId;
    }

    public Long getOrgId(){
        return orgId;
    }
}
