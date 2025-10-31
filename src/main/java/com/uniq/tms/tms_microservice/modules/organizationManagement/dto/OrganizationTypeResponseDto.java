package com.uniq.tms.tms_microservice.modules.organizationManagement.dto;

import java.util.List;

public class OrganizationTypeResponseDto {
    private List<OrganizationTypeCountDto> typeCounts;

    public OrganizationTypeResponseDto(List<OrganizationTypeCountDto> typeCounts) {
        this.typeCounts = typeCounts;
    }
    public List<OrganizationTypeCountDto> getTypeCounts() { return typeCounts; }
    public void setTypeCounts(List<OrganizationTypeCountDto> typeCounts) { this.typeCounts = typeCounts; }
}

