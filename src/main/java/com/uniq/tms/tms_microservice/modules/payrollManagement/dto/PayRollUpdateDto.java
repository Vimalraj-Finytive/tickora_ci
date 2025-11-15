package com.uniq.tms.tms_microservice.modules.payrollManagement.dto;

import java.util.List;

public class PayRollUpdateDto {

    List<String> userId;
    String payRollId;

    public List<String> getUserId() {
        return userId;
    }

    public void setUserId(List<String> userId) {
        this.userId = userId;
    }

    public String getPayRollId() {
        return payRollId;
    }

    public void setPayRollId(String payRollId) {
        this.payRollId = payRollId;
    }
}
