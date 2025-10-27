package com.uniq.tms.tms_microservice.modules.timesheetManagement.dto;

import java.time.LocalDate;
import java.util.List;

public class DashboardSummaryDto {

    private LocalDate date;
    private List<DashboardOrganizationSummaryDto> organizations;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<DashboardOrganizationSummaryDto> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<DashboardOrganizationSummaryDto> organizations) {
        this.organizations = organizations;
    }
}
