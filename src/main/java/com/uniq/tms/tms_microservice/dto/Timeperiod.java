package com.uniq.tms.tms_microservice.dto;


import com.uniq.tms.tms_microservice.service.impl.TimesheetServiceImpl;

import java.time.LocalDate;
import java.util.function.Function;

public enum Timeperiod {
    DAY(date -> date),
    WEEK(date -> date.plusDays(6)),
    MONTH(date -> date.withDayOfMonth(date.lengthOfMonth()));

    private final Function<LocalDate, LocalDate> endDateFunction;

    Timeperiod(Function<LocalDate, LocalDate> endDateFunction) {
        this.endDateFunction = endDateFunction;
    }

    public TimesheetServiceImpl.LocalDateRange calculateDateRange(LocalDate date) {
        return new TimesheetServiceImpl.LocalDateRange(date, endDateFunction.apply(date));
    }

    public static Timeperiod fromString(String timePeriod) {
        for (Timeperiod period : Timeperiod.values()) {
            if (period.name().equalsIgnoreCase(timePeriod)) {
                return period;
            }
        }
        throw new IllegalArgumentException("Invalid timePeriod: " + timePeriod + ". Allowed values: DAY, WEEK, MONTH.");
    }
}
