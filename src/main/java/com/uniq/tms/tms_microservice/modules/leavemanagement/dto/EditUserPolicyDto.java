package com.uniq.tms.tms_microservice.modules.leavemanagement.dto;

import java.time.LocalDate;

public class EditUserPolicyDto {
    private String policyId;
    private String userId;
    private LocalDate validityStartDate;

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getValidityStartDate() {
        return validityStartDate;
    }

    public void setValidityStartDate(LocalDate validityStartDate) {
        this.validityStartDate = validityStartDate;
    }

}
