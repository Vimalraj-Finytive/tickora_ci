package com.uniq.tms.tms_microservice.modules.organizationManagement.enums;

import java.util.Arrays;
import java.util.List;

public enum PlanFeaturesEnum {
    PL02("per user/month", Arrays.asList(
            "Geo Fencing",
            "Face Recognition",
            "Multiple Location",
            "Multiple Work Schedule",
            "Reports And Dashboard",
            "Mobile and Web App"
    )),

    PL03("per user/month", Arrays.asList(
            "Geo Fencing",
            "Face Recognition",
            "Multiple Location",
            "Multiple Work Schedule",
            "Reports And Dashboard",
            "Mobile and Web App"
    )),

    PL04("per user/month", Arrays.asList(
            "Geo Fencing",
            "Face Recognition",
            "Multiple Location",
            "Multiple Work Schedule",
            "Reports And Dashboard",
            "Mobile and Web App"
    ));

    private final String priceTerm;
    private final List<String> features;


    PlanFeaturesEnum(String priceTerm, List<String> features) {
        this.priceTerm = priceTerm;
        this.features = features;
    }

    public List<String> getFeatures() {
        return features;
    }

    public String getPriceTerm() {
        return priceTerm;
    }

    public static PlanFeaturesEnum getByPlanId(String planId) {
        for (PlanFeaturesEnum planFeature : values()) {
            if (planFeature.name().equalsIgnoreCase(planId)) {
                return planFeature;
            }
        }
        return null;
    }
}
