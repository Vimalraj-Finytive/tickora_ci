package com.uniq.tms.tms_microservice.dto;

import java.util.List;

public class UserTimesheetResponseDto {

    private TimesheetSummaryDto summary;
    private List<TimesheetDto> timesheets;

    public TimesheetSummaryDto getSummary() {
        return summary;
    }

    public void setSummary(TimesheetSummaryDto summary) {
        this.summary = summary;
    }

    public List<TimesheetDto> getTimesheets() {
        return timesheets;
    }

    public void setTimesheets(List<TimesheetDto> timesheets) {
        this.timesheets = timesheets;
    }
}
