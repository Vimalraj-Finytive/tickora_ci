package com.uniq.tms.tms_microservice.modules.organizationManagement.dto;

import java.math.BigDecimal;
import java.util.List;

public class OrganizationUsageResponseDto {
    private List<OrganizationUsageDto> organizationUsage;
    private BigDecimal overallCurrentAveragePercentage;
    private BigDecimal overallPreviousAveragePercentage;

    public OrganizationUsageResponseDto(List<OrganizationUsageDto> organizationUsage,
                                        BigDecimal overallCurrentAveragePercentage,
                                        BigDecimal overallPreviousAveragePercentage) {
        this.organizationUsage = organizationUsage;
        this.overallCurrentAveragePercentage = overallCurrentAveragePercentage;
        this.overallPreviousAveragePercentage = overallPreviousAveragePercentage;
    }

    public List<OrganizationUsageDto> getOrganizationUsage() {
        return organizationUsage;
    }

    public void setOrganizationUsage(List<OrganizationUsageDto> organizationUsage) {
        this.organizationUsage = organizationUsage;
    }

    public BigDecimal getOverallCurrentAveragePercentage() {
        return overallCurrentAveragePercentage;
    }

    public void setOverallCurrentAveragePercentage(BigDecimal overallCurrentAveragePercentage) {
        this.overallCurrentAveragePercentage = overallCurrentAveragePercentage;
    }

    public BigDecimal getOverallPreviousAveragePercentage() {
        return overallPreviousAveragePercentage;
    }

    public void setOverallPreviousAveragePercentage(BigDecimal overallPreviousAveragePercentage) {
        this.overallPreviousAveragePercentage = overallPreviousAveragePercentage;
    }
}
