package com.uniq.tms.tms_microservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UserTimesheetResponseDto {

    private TimesheetSummaryDto summary;
    private List<TimesheetDto> timesheets;

    public UserTimesheetResponseDto(TimesheetSummaryDto value, List<TimesheetDto> timesheetDtos) {
        this.summary = value;
        this.timesheets = timesheetDtos;
    }

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
