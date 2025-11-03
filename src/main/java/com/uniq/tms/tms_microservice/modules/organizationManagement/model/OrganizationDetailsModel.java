package com.uniq.tms.tms_microservice.modules.organizationManagement.model;

import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.SubscriptionDto;
import java.time.LocalDateTime;

public class OrganizationDetailsModel {

    private String organizationId;
    private String orgName;
    private String orgType;
    private Integer orgSize;
    private String country;
    private String schemaName;
    private String timeZone;
    private LocalDateTime createdAt;
    private int activeUsers;
    private int inactiveUsers;
    private SubscriptionDto subscriptionSummary;

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }

    public String getOrgType() { return orgType; }
    public void setOrgType(String orgType) { this.orgType = orgType; }

    public Integer getOrgSize() { return orgSize; }
    public void setOrgSize(Integer orgSize) { this.orgSize = orgSize; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getSchemaName() { return schemaName; }
    public void setSchemaName(String schemaName) { this.schemaName = schemaName; }

    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getActiveUsers() { return activeUsers; }
    public void setActiveUsers(int activeUsers) { this.activeUsers = activeUsers; }

    public int getInactiveUsers() { return inactiveUsers; }
    public void setInactiveUsers(int inactiveUsers) { this.inactiveUsers = inactiveUsers; }

    public SubscriptionDto getSubscriptionSummary() {
        return subscriptionSummary;
    }

    public void setSubscriptionSummary(SubscriptionDto subscriptionSummary) {
        this.subscriptionSummary = subscriptionSummary;
    }
}

