package com.uniq.tms.tms_microservice.modules.leavemanagement.services;

import com.uniq.tms.tms_microservice.modules.leavemanagement.model.*;
import java.time.LocalDate;
import java.util.List;

public interface TimeOffRequestService {

    void createRequest(TimeOffRequest request);
    void employeeUpdateStatus(EmployeeStatusUpdate model);
    void adminUpdateStatus(AdminStatusUpdate model);
    List<TimeOffRequestResponseModel> getRequestsByDateRange(LocalDate fromDate, LocalDate toDate);
    List<TimeOffRequestResponseModel> filterRequestsByRole(LocalDate fromDate, LocalDate toDate, int minRoleLevel);
    List<StatusEnumModel> getStatus();
}
