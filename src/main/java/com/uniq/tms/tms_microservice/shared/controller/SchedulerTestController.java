package com.uniq.tms.tms_microservice.shared.controller;

import com.uniq.tms.tms_microservice.modules.leavemanagement.schedular.LeaveBalanceSummarySchedular;
import com.uniq.tms.tms_microservice.modules.payrollManagement.schedular.PayRollSchedular;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class SchedulerTestController {

    private final LeaveBalanceSummarySchedular leaveBalanceSummarySchedular;
    private final PayRollSchedular payRollScheduler;

    public SchedulerTestController(LeaveBalanceSummarySchedular leaveBalanceSummarySchedular, PayRollSchedular payRollScheduler) {
        this.leaveBalanceSummarySchedular = leaveBalanceSummarySchedular;
        this.payRollScheduler = payRollScheduler;
    }

    @PostMapping("/daily/summary")
    public String runSchedulerManually(){
        leaveBalanceSummarySchedular.autoUpdateLeaveSummary();
        return "Daily Scheduler Run Successfully";
    }

    @PostMapping("/payroll/calculate")
    public String runPayRollSchedulerManually(){
        payRollScheduler.autoCalculatePayrollAmountForAllEmployees();
        return "PayRoll Scheduler Run Successfully";
    }
}
