package com.uniq.tms.tms_microservice.modules.leavemanagement.services;

import com.uniq.tms.tms_microservice.modules.leavemanagement.model.*;
import java.util.List;

public interface TimeOffRequestService {

    void createRequest(TimeOffRequest request);
    void employeeUpdateStatus(EmployeeStatusUpdate model);
    void adminUpdateStatus(AdminStatusUpdate model);
    void updateLeaveBalance();
    List<TimeOffRequestResponseModel> getRequestsByDateRange(RequestFilterModel model);
}
