package com.uniq.tms.tms_microservice.modules.leavemanagement.services;

import com.uniq.tms.tms_microservice.modules.leavemanagement.model.LeaveBalanceModel;

import java.util.List;

public interface LeaveBalanceService {

    List<LeaveBalanceModel> getLeaveBalance(String userId,String year);
    void updateMonthlyLeaveBalance();
    void updateYearlyLeaveBalance();
    void  updateMonthlyLeaveSummary();
    void updateDailyLeaveSummary();
}
