package com.uniq.tms.tms_microservice.modules.identityManagement.entity;

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

    @Column(name = "last_secondary_user_id")
    private Integer lastSecondaryUserId;

    @Column(name = "last_subscription_id")
    private Integer lastSubscriptionId;

    @Column(name = "last_payment_id")
    private Integer lastPaymentId;

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
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

    public Integer getLastSubscriptionId() {
        return lastSubscriptionId;
    }

    public void setLastSubscriptionId(Integer lastSubscriptionId) {
        this.lastSubscriptionId = lastSubscriptionId;
    }

    public Integer getLastPaymentId() {
        return lastPaymentId;
    }

    public void setLastPaymentId(Integer lastPaymentId) {
        this.lastPaymentId = lastPaymentId;
    }
}
