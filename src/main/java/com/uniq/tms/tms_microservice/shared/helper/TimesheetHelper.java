package com.uniq.tms.tms_microservice.shared.helper;

import com.uniq.tms.tms_microservice.modules.timesheetManagement.adapter.impl.TimesheetAdapterImpl;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.adapter.WorkScheduleAdapter;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.FixedWorkScheduleEntity;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.FlexibleWorkScheduleEntity;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.model.ScheduleTypeInfo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TimesheetHelper {

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
        List<FixedWorkScheduleEntity> fixedSchedules = workScheduleAdapter.findFixedSchedulesByUserIds(userIds);
        var flexibleSchedules = workScheduleAdapter.findFlexibleSchedulesByUserIds(userIds);

        // Build fixed map
        Map<String, Map<DayOfWeek, FixedWorkScheduleEntity>> fixedMap = fixedSchedules.stream()
                .flatMap(f -> f.getWorkScheduleEntity().getUsers().stream()
                        .map(u -> Map.entry(u.getUserId(),
                                Map.entry(DayOfWeek.valueOf(f.getDay().name()), f))))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.toMap(
                                e -> e.getValue().getKey(),
                                e -> e.getValue().getValue(),
                                (existing, replacement) -> existing
                        )
                ));

        // Build flexible map
        Map<String, Map<DayOfWeek, FlexibleWorkScheduleEntity>> flexMap = flexibleSchedules.stream()
                .flatMap(f -> f.getWorkScheduleEntity().getUsers().stream()
                        .map(u -> Map.entry(u.getUserId(),
                                Map.entry(DayOfWeek.valueOf(f.getDay().name()), f))))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.toMap(
                                e -> e.getValue().getKey(),
                                e -> e.getValue().getValue(),
                                (existing, replacement) -> existing
                        )
                ));

        // Resolve working days
        Map<String, Set<DayOfWeek>> userWorkingDaysMap = workScheduleAdapter.resolveWorkingDays(userIds);

        return new WorkScheduleResult(fixedMap, flexMap, userWorkingDaysMap);
    }

    /**
     * DTO to hold all three outputs in one return object
     */
    public static class WorkScheduleResult {
        private final Map<String, Map<DayOfWeek, FixedWorkScheduleEntity>> fixedMap;
        private final Map<String, Map<DayOfWeek, FlexibleWorkScheduleEntity>> flexMap;
        private final Map<String, Set<DayOfWeek>> userWorkingDaysMap;

        public WorkScheduleResult(
                Map<String, Map<DayOfWeek, FixedWorkScheduleEntity>> fixedMap,
                Map<String, Map<DayOfWeek, FlexibleWorkScheduleEntity>> flexMap,
                Map<String, Set<DayOfWeek>> userWorkingDaysMap
        ) {
            this.fixedMap = fixedMap;
            this.flexMap = flexMap;
            this.userWorkingDaysMap = userWorkingDaysMap;
        }

        public Map<String, Map<DayOfWeek, FixedWorkScheduleEntity>> getFixedMap() {
            return fixedMap;
        }

        public Map<String, Map<DayOfWeek, FlexibleWorkScheduleEntity>> getFlexMap() {
            return flexMap;
        }

        public Map<String, Set<DayOfWeek>> getUserWorkingDaysMap() {
            return userWorkingDaysMap;
        }
    }

    public static ScheduleTypeInfo getScheduledHoursForUser(
            String userId,
            LocalDate date,
            Map<String, Map<DayOfWeek, FixedWorkScheduleEntity>> fixedMap,
            Map<String, Map<DayOfWeek, FlexibleWorkScheduleEntity>> flexMap
    ) {
        DayOfWeek day = date.getDayOfWeek();

        if (fixedMap.containsKey(userId) && fixedMap.get(userId).containsKey(day)) {
            FixedWorkScheduleEntity fixed = fixedMap.get(userId).get(day);
            return ScheduleTypeInfo.fixed(
                    fixed.getStartTime().toLocalTime(),
                    fixed.getEndTime().toLocalTime()
            );
        }

        if (flexMap.containsKey(userId) && flexMap.get(userId).containsKey(day)) {
            FlexibleWorkScheduleEntity flex = flexMap.get(userId).get(day);
            Duration duration = Duration.ofMinutes((long) (flex.getDuration() * 60));
            return ScheduleTypeInfo.flexible(duration);
        }

        return null;
    }
}
