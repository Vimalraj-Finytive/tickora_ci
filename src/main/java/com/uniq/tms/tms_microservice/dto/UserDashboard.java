package com.uniq.tms.tms_microservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;

public interface UserDashboard {

    @JsonIgnore
    Long getUserId();
    @JsonIgnore
    Integer getStatusId();
    @JsonIgnore
    LocalDate getLogDate();
}
