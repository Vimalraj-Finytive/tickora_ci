package com.uniq.tms.tms_microservice.modules.organizationManagement.dto;

import java.math.BigDecimal;

public class OrganizationTypeCountDto {
    private String orgTypeId;
    private String orgTypeName;
    private long count;
    private BigDecimal percentage;

    public OrganizationTypeCountDto(String orgTypeId, String orgTypeName, long count, BigDecimal percentage) {
        this.orgTypeId = orgTypeId;
        this.orgTypeName = orgTypeName;
        this.count = count;
        this.percentage = percentage;
    }

    public String getOrgTypeId() {
        return orgTypeId;
    }

    public void setOrgTypeId(String orgTypeId) {
        this.orgTypeId = orgTypeId;
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
