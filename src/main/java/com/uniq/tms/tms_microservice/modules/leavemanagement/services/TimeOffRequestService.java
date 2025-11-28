package com.uniq.tms.tms_microservice.modules.leavemanagement.services;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffRequestDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffRequestGroupDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffccDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.*;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TimeOffRequestService {

    void createRequest(TimeOffRequest request);
    void employeeUpdateStatus(EmployeeStatusUpdate model);
    void adminUpdateStatus(AdminStatusUpdate model);
    Map<String, List<TimeOffRequestGroupModel>> filterRequests(TimeOffccDto dto);
    List<TimeOffRequestResponseModel> filterRequestsByRole(LocalDate fromDate, LocalDate toDate, int minRoleLevel);
    List<StatusEnumModel> getStatus();
}
