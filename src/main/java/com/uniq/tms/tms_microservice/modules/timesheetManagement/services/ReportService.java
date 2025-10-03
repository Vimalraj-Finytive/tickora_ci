package com.uniq.tms.tms_microservice.modules.timesheetManagement.services;

import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.FileExportResponseDto;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.TimesheetReportDto;

public interface ReportService {
    FileExportResponseDto generateTimesheetFile(TimesheetReportDto request, String userIdFromToken, String orgId, String role, String predefinedFileName);
}
