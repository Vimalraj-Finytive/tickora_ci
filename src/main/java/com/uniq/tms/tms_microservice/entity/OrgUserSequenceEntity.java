package com.uniq.tms.tms_microservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "org_user_sequence")
public class OrgUserSequenceEntity {

    @Id
    @Column(name = "org_id", nullable = false)
    private String orgId;

    @Column(name = "last_user_id", nullable = false)
    private Integer lastUserId;

    @Column(name = "last_secondary_user_id", nullable = false)
    private Integer lastSecondaryUserId;

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public Integer getLastNumber() {
        return lastUserId;
    }

    public void setLastNumber(Integer lastNumber) {
        this.lastUserId = lastNumber;
    }

    public Integer getLastUserId() {
        return lastUserId;
    }

    public void setLastUserId(Integer lastUserId) {
        this.lastUserId = lastUserId;
    }

    public Integer getLastSecondaryUserId() {
        return lastSecondaryUserId;
    }

    public void setLastSecondaryUserId(Integer lastSecondaryUserId) {
        this.lastSecondaryUserId = lastSecondaryUserId;
    }
}
