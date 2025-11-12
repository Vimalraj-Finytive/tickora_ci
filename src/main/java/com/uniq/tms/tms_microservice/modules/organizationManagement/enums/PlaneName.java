package com.uniq.tms.tms_microservice.modules.organizationManagement.enums;

public enum PlaneName {

    BASIC_PLAN("Basic plan", "PL01"),
    ESSENTIAL_PLAN("Essential plan", "PL02"),
    STANDARD_PLAN("Standard plan","PL03"),
    PREMIUM_PLANE("Premium plan", "PL04");

    private final String planName;
    private final String planId;

    PlaneName(String planName, String planId) {
        this.planName = planName;
        this.planId = planId;
    }

    public String getPlanName() {
        return planName;
    }

    public String getPlanId(){
        return planId;
    }
}
