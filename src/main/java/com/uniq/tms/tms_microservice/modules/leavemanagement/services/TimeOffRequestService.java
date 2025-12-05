package com.uniq.tms.tms_microservice.modules.leavemanagement.services;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffExportRequestDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.*;
import com.uniq.tms.tms_microservice.shared.dto.EnumModel;
import org.springframework.core.io.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TimeOffRequestService {

    void createRequest(TimeOffRequest request);
    void employeeUpdateStatus(EmployeeStatusUpdate model);
    void adminUpdateStatus(AdminStatusUpdate model);
    Map<String, List<TimeOffRequestGroupModel>> filterRequests(TimeOffExportRequest dto,String loggedUserId);
    List<TimeOffRequestResponseModel> filterRequestsByRole(LocalDate fromDate, LocalDate toDate, int minRoleLevel);
    List<EnumModel> getStatus();
    String startExporting(TimeOffExportRequestDto request, String schema, String orgId);
    String exportStatus(String exportId, String schema, String orgId);
    Resource downloadReport(String exportId, String schema, String orgId, String type);
    List<EnumModel> getHourType();
}
