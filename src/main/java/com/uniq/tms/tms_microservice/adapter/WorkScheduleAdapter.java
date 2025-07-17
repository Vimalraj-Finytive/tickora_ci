package com.uniq.tms.tms_microservice.adapter;

import com.uniq.tms.tms_microservice.entity.*;
import java.util.List;
import java.util.Optional;

public interface WorkScheduleAdapter {
    WorkScheduleEntity findByWorkscheduleId(String workScheduleId);
    WorkScheduleEntity findDefaultActiveSchedule(Long orgId);
    void saveAll(List<FixedWorkScheduleEntity> entities);
    void saveAllFlexible(List<FlexibleWorkScheduleEntity> entities);
    void save(WeeklyWorkScheduleEntity entity);
    void saveWorkSchedule(WorkScheduleEntity entity);
    Optional<WorkScheduleTypeEntity> findById(String type);
    void addType(WorkScheduleTypeEntity entity);
    WorkScheduleEntity findByScheduleId(String scheduleId);
    void deleteAllChildren(String scheduleId);
    boolean findByWorkschedule(String scheduleName, Long orgId);
    void resetDefaultWorkSchedule(Long orgId);
    void updateDefaultWorkSchedule(Long orgId, String scheduleId);
    boolean findByScheduleName(String scheduleId , String scheduleName, Long orgId);
    WorkScheduleEntity findDefaultScheduleByOrgId(Long orgId);
    WorkScheduleEntity getScheduleForUser(Long userId);
    List<FlexibleWorkScheduleEntity> findByWorkScheduleId(String scheduleId);
    List<FixedWorkScheduleEntity> findByFixedScheduleId(String scheduleId);
    List<WorkScheduleTypeEntity> findAllType();
}
