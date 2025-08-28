package com.uniq.tms.tms_microservice.projection;

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
