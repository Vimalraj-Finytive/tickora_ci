package com.uniq.tms.tms_microservice.modules.leavemanagement.dto;

import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.AccrualType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.Compensation;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.EntitledType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.ResetFrequency;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class TimeOffPolicyRequestDto {

    private String policyName;
    private Compensation compensation;
    private AccrualType accrualType;

    @NotNull
    private LocalDate validityStartDate;

    @NotNull
    private LocalDate validityEndDate;

    private ResetFrequency resetFrequency;
    private EntitledType entitledType;
    private Integer entitledUnits;
    private Boolean carryForward;
    private Integer maxCarryForwardUnits;
    private Boolean reschedule;

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public Compensation getCompensation() {
        return compensation;
    }

    public void setCompensation(Compensation compensation) {
        this.compensation = compensation;
    }


    public LocalDate getValidityStartDate() {
        return validityStartDate;
    }

    public void setValidityStartDate(LocalDate validityStartDate) {
        this.validityStartDate = validityStartDate;
    }

    public LocalDate getValidityEndDate() {
        return validityEndDate;
    }

    public void setValidityEndDate(LocalDate validityEndDate) {
        this.validityEndDate = validityEndDate;
    }

    public EntitledType getEntitledType() {
        return entitledType;
    }

    public void setEntitledType(EntitledType entitledType) {
        this.entitledType = entitledType;
    }

    public Integer getEntitledUnits() {
        return entitledUnits;
    }

    public void setEntitledUnits(Integer entitledUnits) {
        this.entitledUnits = entitledUnits;
    }

    public Boolean getCarryForward() {
        return carryForward;
    }

    public void setCarryForward(Boolean carryForward) {
        this.carryForward = carryForward;
    }

    public Integer getMaxCarryForwardUnits() {
        return maxCarryForwardUnits;
    }

    public void setMaxCarryForwardUnits(Integer maxCarryForwardUnits) {
        this.maxCarryForwardUnits = maxCarryForwardUnits;
    }

    public ResetFrequency getResetFrequency() {
        return resetFrequency;
    }

    public void setResetFrequency(ResetFrequency resetFrequency) {
        this.resetFrequency = resetFrequency;
    }

    public AccrualType getAccrualType() {
        return accrualType;
    }

    public void setAccrualType(AccrualType accrualType) {
        this.accrualType = accrualType;
    }

    public Boolean getReschedule() {
        return reschedule;
    }

    public void setReschedule(Boolean reschedule) {
        this.reschedule = reschedule;
    }
}
