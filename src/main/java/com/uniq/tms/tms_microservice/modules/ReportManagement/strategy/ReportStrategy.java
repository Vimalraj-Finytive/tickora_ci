package com.uniq.tms.tms_microservice.modules.ReportManagement.strategy;

import com.uniq.tms.tms_microservice.modules.ReportManagement.enums.ReportType;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;

public interface ReportStrategy {
    ReportType getType();
    ApiResponse<String> startExport(Object request);
    ApiResponse<String> checkStatus(String exportId, ReportType type);
}
