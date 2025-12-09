package com.uniq.tms.tms_microservice.modules.userManagement.model;

import java.time.LocalDate;

public class UserPolicyModel {

    private String policyName;
    private LocalDate userStartDate;
    private LocalDate userEndDate;

    public UserPolicyModel() {

    }

    public UserPolicyModel(String policyName, LocalDate userStartDate, LocalDate userEndDate) {
        this.policyName = policyName;
        this.userStartDate = userStartDate;
        this.userEndDate = userEndDate;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public LocalDate getUserStartDate() {
        return userStartDate;
    }

    public void setUserStartDate(LocalDate userStartDate) {
        this.userStartDate = userStartDate;
    }

    public LocalDate getUserEndDate() {
        return userEndDate;
    }

    public void setUserEndDate(LocalDate userEndDate) {
        this.userEndDate = userEndDate;
    }
}
