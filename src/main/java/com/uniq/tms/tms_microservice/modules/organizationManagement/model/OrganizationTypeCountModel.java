package com.uniq.tms.tms_microservice.modules.organizationManagement.model;

import java.math.BigDecimal;

public class OrganizationTypeCountModel {

    private String orgTypeName;
    private long count;
    private BigDecimal percentage;

    public OrganizationTypeCountModel(String orgTypeName, long count, BigDecimal percentage) {
        this.orgTypeName = orgTypeName;
        this.count = count;
        this.percentage = percentage;
    }

    public String getOrgTypeName() {
        return orgTypeName;
    }

    public void setOrgTypeName(String orgTypeName) {
        this.orgTypeName = orgTypeName;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }
}
