package com.uniq.tms.tms_microservice.service;

import com.uniq.tms.tms_microservice.dto.UserTimesheetResponseDto;
import java.util.List;

public interface ReportService {
     byte[] exportTimesheetDayCsv(List<UserTimesheetResponseDto> timesheets, String sheetName);

    byte[] exportTimesheetDayXlsx(List<UserTimesheetResponseDto> timesheets);

    byte[] exportTimesheetWeekXlsx(List<UserTimesheetResponseDto> timesheets);

    byte[] exportTimesheetWeekCsv(List<UserTimesheetResponseDto> timesheets);
}
