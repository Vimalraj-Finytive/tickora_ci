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

    @Column(name = "last_number", nullable = false)
    private Integer lastNumber;

    // Getters and setters
    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public Integer getLastNumber() {
        return lastNumber;
    }

    public void setLastNumber(Integer lastNumber) {
        this.lastNumber = lastNumber;
    }
}
