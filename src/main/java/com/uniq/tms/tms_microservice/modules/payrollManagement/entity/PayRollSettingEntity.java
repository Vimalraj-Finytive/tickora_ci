package com.uniq.tms.tms_microservice.modules.payrollManagement.entity;

import com.uniq.tms.tms_microservice.modules.payrollManagement.enums.PayRollSettingEnum;
import jakarta.persistence.*;

@Entity
@Table(name = "Payroll_Settings")
public class PayRollSettingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "payroll_calculation")
    private PayRollSettingEnum payrollCalculation;
    @Column(name = "is_overtime")
    private boolean isOvertime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PayRollSettingEnum getPayrollCalculation() {
        return payrollCalculation;
    }

    public void setPayrollCalculation(PayRollSettingEnum payrollCalculation) {
        this.payrollCalculation = payrollCalculation;
    }

    public boolean isOvertime() {
        return isOvertime;
    }

    public void setOvertime(boolean overtime) {
        isOvertime = overtime;
    }
}
