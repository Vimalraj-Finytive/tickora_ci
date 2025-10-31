package com.uniq.tms.tms_microservice.modules.organizationManagement.dto;
import java.math.BigDecimal;

public class OrganizationUsageDto {
    private String organizationId;
    private String orgName;
    private BigDecimal currentUsagePercentage;
    private BigDecimal previousUsagePercentage;

    public OrganizationUsageDto(String organizationId, String orgName, BigDecimal currentUsagePercentage, BigDecimal previousUsagePercentage) {
        this.organizationId = organizationId;
        this.orgName = orgName;
        this.currentUsagePercentage = currentUsagePercentage;
        this.previousUsagePercentage = previousUsagePercentage;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public BigDecimal getCurrentUsagePercentage() {
        return currentUsagePercentage;
    }

    public void setCurrentUsagePercentage(BigDecimal currentUsagePercentage) {
        this.currentUsagePercentage = currentUsagePercentage;
    }

    public BigDecimal getPreviousUsagePercentage() {
        return previousUsagePercentage;
    }

    public void setPreviousUsagePercentage(BigDecimal previousUsagePercentage) {
        this.previousUsagePercentage = previousUsagePercentage;
    }
}

