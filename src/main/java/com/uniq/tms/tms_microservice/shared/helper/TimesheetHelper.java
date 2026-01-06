package com.uniq.tms.tms_microservice.shared.helper;

import com.uniq.tms.tms_microservice.modules.workScheduleManagement.adapter.WorkScheduleAdapter;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.projection.FixedWorkScheduleProjection;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.model.ScheduleTypeInfo;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.projection.FlexibleScheduleProjection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TimesheetHelper {

    private static final Logger log = LoggerFactory.getLogger(TimesheetHelper.class);

    private WorkScheduleAdapter workScheduleAdapter;

    public TimesheetHelper(WorkScheduleAdapter workScheduleAdapter) {
        this.workScheduleAdapter = workScheduleAdapter;
    }

    /**
     * Fetches fixed and flexible work schedules for the given userIds and
     * returns both maps and resolved working days.
     */
    @Transactional(readOnly = true)
    public WorkScheduleResult fetchWorkSchedulesAndDays(String[] userIds) {

        // Fetch schedules
        List<FixedWorkScheduleProjection> fixedSchedules =
                workScheduleAdapter.findFixedSchedulesByUserIds(userIds);
        log.info("fetched fixed schedulees for a users");
        var flexibleSchedules = workScheduleAdapter.findFlexibleSchedulesByUserIds(userIds);
        log.info("fetched flexible schedulees for a users");

        // Build fixed map
        Map<String, Map<DayOfWeek, FixedWorkScheduleProjection>> fixedMap =
                fixedSchedules.stream()
                        .filter(f -> f.getUserId() != null)
                        .filter(f -> f.getDay() != null && !f.getDay().isBlank())
                        .collect(Collectors.groupingBy(
                                FixedWorkScheduleProjection::getUserId,
                                Collectors.toMap(
                                        f -> DayOfWeek.valueOf(f.getDay()),
                                        f -> f,
                                        (existing, replacement) -> existing // condition logic
                                )
                        ));

        log.info("mapping fixed schedulees for a users");

        // Build flexible map
        Map<String, Map<DayOfWeek, FlexibleScheduleProjection>> flexMap =
                Optional.ofNullable(flexibleSchedules)
                        .orElse(Collections.emptyList())
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(fs -> fs.getUserId() != null)
                        .filter(fs -> fs.getDay() != null)
                        .collect(Collectors.groupingBy(
                                FlexibleScheduleProjection::getUserId,
                                Collectors.toMap(
                                        fs -> DayOfWeek.valueOf(fs.getDay()),
                                        Function.identity(),
                                        (existing, replacement) -> existing
                                )
                        ));


        log.info("fetched flexible schedulees for a users");

        // Resolve working days
        Map<String, Set<DayOfWeek>> userWorkingDaysMap = workScheduleAdapter.resolveWorkingDays(userIds);
        log.info("fetched userWorking day map");

        return new WorkScheduleResult(fixedMap, flexMap, userWorkingDaysMap);
    }

    /**
     * DTO to hold all three outputs in one return object
     */
    public static class WorkScheduleResult {
        private final Map<String, Map<DayOfWeek, FixedWorkScheduleProjection>> fixedMap;
        private final Map<String, Map<DayOfWeek, FlexibleScheduleProjection>> flexMap;
        private final Map<String, Set<DayOfWeek>> userWorkingDaysMap;

        public WorkScheduleResult(
                Map<String, Map<DayOfWeek, FixedWorkScheduleProjection>> fixedMap,
                Map<String, Map<DayOfWeek, FlexibleScheduleProjection>> flexMap,
                Map<String, Set<DayOfWeek>> userWorkingDaysMap
        ) {
            this.fixedMap = fixedMap;
            this.flexMap = flexMap;
            this.userWorkingDaysMap = userWorkingDaysMap;
        }

        public Map<String, Map<DayOfWeek, FixedWorkScheduleProjection>> getFixedMap() {
            return fixedMap;
        }

        public Map<String, Map<DayOfWeek, FlexibleScheduleProjection>> getFlexMap() {
            return flexMap;
        }

        public Map<String, Set<DayOfWeek>> getUserWorkingDaysMap() {
            return userWorkingDaysMap;
        }
    }

    public static ScheduleTypeInfo getScheduledHoursForUser(
            String userId,
            LocalDate date,
            Map<String, Map<DayOfWeek, FixedWorkScheduleProjection>> fixedMap,
            Map<String, Map<DayOfWeek, FlexibleScheduleProjection>> flexMap
    ) {
        DayOfWeek day = date.getDayOfWeek();
        if (fixedMap.containsKey(userId) && fixedMap.get(userId).containsKey(day)) {
            FixedWorkScheduleProjection fixed = fixedMap.get(userId).get(day);
            return ScheduleTypeInfo.fixed(
                    fixed.getStartTime(),
                    fixed.getEndTime()
            );
        }
        if (flexMap.containsKey(userId) && flexMap.get(userId).containsKey(day)) {
            FlexibleScheduleProjection flex = flexMap.get(userId).get(day);
            Duration duration = Duration.ofMinutes((long) (flex.getDuration() * 60));
            return ScheduleTypeInfo.flexible(duration);
        }
        return null;
    }
}
