package com.uniq.tms.tms_microservice.service;

import com.uniq.tms.tms_microservice.dto.FileExportResponseDto;
import com.uniq.tms.tms_microservice.dto.TimesheetReportDto;

public interface ReportService {
    FileExportResponseDto generateTimesheetFile(TimesheetReportDto request, String userIdFromToken, String orgId, String role, String predefinedFileName);
}
