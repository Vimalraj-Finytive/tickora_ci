package com.uniq.tms.tms_microservice.adapter;

import com.uniq.tms.tms_microservice.entity.*;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface WorkScheduleAdapter {
    WorkScheduleEntity findDefaultActiveSchedule(String orgId);
    void saveAll(List<FixedWorkScheduleEntity> entities);
    void saveAllFlexible(List<FlexibleWorkScheduleEntity> entities);
    void save(WeeklyWorkScheduleEntity entity);
    WorkScheduleEntity saveWorkSchedule(WorkScheduleEntity entity);
    Optional<WorkScheduleTypeEntity> findById(String type);
    void addType(WorkScheduleTypeEntity entity);
    WorkScheduleEntity findByScheduleId(String scheduleId, String orgId);
    void deleteAllChildren(String scheduleId);
    boolean findByWorkschedule(String scheduleName, String orgId);
    void resetDefaultWorkSchedule(String orgId);
    void updateDefaultWorkSchedule(String orgId, String scheduleId);
    boolean findByScheduleName(String scheduleId , String scheduleName, String orgId);
    WorkScheduleEntity findDefaultScheduleByOrgId(String orgId);
    WorkScheduleEntity getScheduleForUser(String userId);
    List<FlexibleWorkScheduleEntity> findByWorkScheduleId(String scheduleId);
    List<FixedWorkScheduleEntity> findByFixedScheduleId(String scheduleId);
    List<WorkScheduleTypeEntity> findAllType();
    List<WorkScheduleEntity> findAllScheduleById(String orgId);
    int countByOrgId(String orgId);
    Map<String, String> getAllSchedules(String orgId);
    Map<String, Set<DayOfWeek>> resolveWorkingDays(String[] userIds);
    List<FixedWorkScheduleEntity> findFixedSchedulesByUserIds(String[] pagedUserIds);
    List<FlexibleWorkScheduleEntity> findFlexibleSchedulesByUserIds(String[]  pagedUserIds);
}
