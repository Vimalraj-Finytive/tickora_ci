package com.uniq.tms.tms_microservice.modules.organizationManagement.model;

import java.math.BigDecimal;

public class TopCustomersModel {
    private String organizationName;
    private BigDecimal amount;

    public TopCustomersModel(String organizationName, BigDecimal amount) {
        this.organizationName = organizationName;
        this.amount = amount;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
