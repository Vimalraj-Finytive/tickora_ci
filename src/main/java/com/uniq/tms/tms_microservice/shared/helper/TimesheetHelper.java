package com.uniq.tms.tms_microservice.shared.helper;

import com.uniq.tms.tms_microservice.modules.workScheduleManagement.adapter.WorkScheduleAdapter;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.FixedWorkScheduleEntity;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.FlexibleWorkScheduleEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
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
}
