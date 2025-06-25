package com.uniq.tms.tms_microservice.dto;

import com.uniq.tms.tms_microservice.service.impl.TimesheetServiceImpl;
import java.time.LocalDate;
import java.time.Year;
import java.util.function.Function;

public enum Timeperiod {
    DAY(fromdate -> fromdate),
    WEEK(fromdate -> fromdate.plusDays(6)),
    MONTH(fromdate -> fromdate.withDayOfMonth(fromdate.lengthOfMonth())),
    YEAR(fromdate -> fromdate.with(Year.from(fromdate).atMonth(12).atDay(31)));

    private final Function<LocalDate, LocalDate> endDateFunction;

    Timeperiod(Function<LocalDate, LocalDate> endDateFunction) {
        this.endDateFunction = endDateFunction;
    }

    public TimesheetServiceImpl.LocalDateRange calculateDateRange(LocalDate date) {
        return new TimesheetServiceImpl.LocalDateRange(date, endDateFunction.apply(date));
    }

    public static Timeperiod fromString(String timePeriod) {
        if (timePeriod == null || timePeriod.trim().isEmpty()) {
            return null;
        }
        for (Timeperiod period : Timeperiod.values()) {
            if (period.name().equalsIgnoreCase(timePeriod)) {
                return period;
            }
        }
        throw new IllegalArgumentException("Invalid timePeriod: " + timePeriod + ". Allowed values: DAY, WEEK, MONTH, YEAR.");
    }
}
