package com.uniq.tms.tms_microservice.modules.leavemanagement.services;

import com.uniq.tms.tms_microservice.modules.leavemanagement.model.LeaveBalanceModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.UserWithLeaveBalanceModel;

import java.util.List;

public interface LeaveBalanceService {

    List<LeaveBalanceModel> getLeaveBalance(String userId);
    List<UserWithLeaveBalanceModel> getSupervisorLeave(String userId);

}
