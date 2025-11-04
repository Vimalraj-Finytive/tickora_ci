package com.uniq.tms.tms_microservice.shared.util;

import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class DateUtil {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Kolkata");

    /**
     * Converts LocalDate to LocalDateTime at 00:00:00 for the configured zone.
     */
    public static LocalDateTime toStartDate(LocalDate startDate) {
        return startDate != null ? startDate.atStartOfDay(ZONE_ID).toLocalDateTime() : null;
    }

    /**
     * Converts LocalDate to LocalDateTime at 23:59:59 for the configured zone.
     */
    public static LocalDateTime toEndDate(LocalDate endDate) {
        return endDate != null ? endDate.atTime(LocalTime.MAX) : null;
    }

    /**
     * Compute previous date range from the given start and end date
     */
    public static Pair<LocalDateTime, LocalDateTime> computePreviousRange(LocalDateTime from, LocalDateTime to) {
        long days = ChronoUnit.DAYS.between(from.toLocalDate(), to.toLocalDate());
        LocalDateTime prevTo = from.minusDays(1);
        LocalDateTime prevFrom = prevTo.minusDays(days);
        return Pair.of(prevFrom, prevTo);
    }

    /**
     * Converts long to bigDecimal
     * returns percentage for the given counts
     */

    public static BigDecimal calculatePercentage(Long counts, Long totalCounts) {
        return totalCounts > 0 ? BigDecimal.valueOf((counts * 100.0) / totalCounts)
                .setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }
}
