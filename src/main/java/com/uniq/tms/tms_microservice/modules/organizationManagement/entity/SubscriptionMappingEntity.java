package com.uniq.tms.tms_microservice.modules.organizationManagement.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "subscription_mapping")
public class SubscriptionMappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_mapping_id", nullable = false)
    private Integer subscriptionMappingId;

    @Column(name = "subscription_id", nullable = false)
    private String subscriptionId;

    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    public Integer getSubscriptionMappingId() {
        return subscriptionMappingId;
    }

    public void setSubscriptionMappingId(Integer subscriptionMappingId) {
        this.subscriptionMappingId = subscriptionMappingId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
}

