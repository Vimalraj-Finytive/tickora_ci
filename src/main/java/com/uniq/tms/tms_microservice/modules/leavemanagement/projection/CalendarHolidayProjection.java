package com.uniq.tms.tms_microservice.modules.leavemanagement.projection;

import java.time.LocalDate;

public interface CalendarHolidayProjection {
    String getCalendarId();
    LocalDate getDate();
}
