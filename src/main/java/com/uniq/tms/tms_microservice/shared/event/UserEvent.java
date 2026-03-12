package com.uniq.tms.tms_microservice.shared.event;

public record UserEvent(String orgId , String schema, String userId) {
    public UserEvent(String orgId, String schema) {
        this(orgId, schema, null);
    }
}
