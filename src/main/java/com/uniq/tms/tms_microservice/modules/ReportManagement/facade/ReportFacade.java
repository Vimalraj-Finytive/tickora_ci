package com.uniq.tms.tms_microservice.modules.ReportManagement.facade;

import com.uniq.tms.tms_microservice.modules.ReportManagement.enums.ReportType;
import com.uniq.tms.tms_microservice.modules.ReportManagement.factory.ReportStrategyFactory;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffExportRequestDto;
import com.uniq.tms.tms_microservice.modules.payrollManagement.dto.PayRollExportDto;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.TimesheetReportDto;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import org.springframework.stereotype.Component;

@Component
public class ReportFacade {

    private final ReportStrategyFactory reportStrategyFactory;

    public ReportFacade(ReportStrategyFactory reportStrategyFactory) {
        this.reportStrategyFactory = reportStrategyFactory;
    }

    public ApiResponse<String> start(ReportType reportType, TimesheetReportDto reportDto) {
        return reportStrategyFactory.get(reportType).startExport(reportDto);
    }

    public ApiResponse<String> status(ReportType type, String exportId) {
        return reportStrategyFactory.get(type).checkStatus(exportId,type);
    }

    public ApiResponse<String> start(ReportType reportType, PayRollExportDto reportDto) {
        return reportStrategyFactory.get(reportType).startExport(reportDto);
    }

    public ApiResponse<String> start(ReportType reportType, TimeOffExportRequestDto reportDto) {
        return reportStrategyFactory.get(reportType).startExport(reportDto);
    }
}
