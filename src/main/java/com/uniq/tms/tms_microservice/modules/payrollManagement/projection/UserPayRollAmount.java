package com.uniq.tms.tms_microservice.modules.payrollManagement.projection;

import java.math.BigDecimal;

public interface UserPayRollAmount {

    String getUserId();
    String getUserName();
    BigDecimal getUnpaidLeaveDeduction();
    String getRegularDays();
    String getRegularHrs();
    String getOvertimeHrs();
    String getTotalHrs();
    BigDecimal getRegularPayrollAmount();
    BigDecimal getOvertimePayrollAmount();
    BigDecimal getTotalPayrollAmount();
    BigDecimal getMonthlyNetSalary();
    String getPayrollStatus();
    String getNotes();
    BigDecimal getTotalAmount();
    String getPayrollName();
    String getMonth();
}
