package com.uniq.tms.tms_microservice.modules.userManagement.projections;

import java.time.LocalDate;

public interface UserHolidayProjection {
    String getUserId();
    LocalDate getDate();
}
