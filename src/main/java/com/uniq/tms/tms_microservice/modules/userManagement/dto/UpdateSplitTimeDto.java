package com.uniq.tms.tms_microservice.modules.userManagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateSplitTimeDto {

    @JsonProperty("isSplitTimeEnabled")
    private Boolean isSplitTimeEnabled;

    public Boolean getSplitTimeEnabled() {
        return isSplitTimeEnabled;
    }

    public void setSplitTimeEnabled(Boolean isSplitTimeEnabled) {
        this.isSplitTimeEnabled = isSplitTimeEnabled;
    }

}
