package com.uniq.tms.tms_microservice.modules.organizationManagement.enums;

public enum PlaneName {

    BASIC_PLAN("Basic plan"),
    ESSENTIAL_PLAN("Essential plan"),
    STANDARD_PLAN("Standard plan"),
    PREMIUM_PLANE("Premium plan");

    private final String planName;

    PlaneName(String planName) {
        this.planName = planName;
    }
}
