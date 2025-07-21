package com.uniq.tms.tms_microservice.adapter;

import com.uniq.tms.tms_microservice.entity.*;
import java.util.List;
import java.util.Optional;

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
    WorkScheduleEntity getScheduleForUser(Long userId);
    List<FlexibleWorkScheduleEntity> findByWorkScheduleId(String scheduleId);
    List<FixedWorkScheduleEntity> findByFixedScheduleId(String scheduleId);
    List<WorkScheduleTypeEntity> findAllType();
    List<WorkScheduleEntity> findAllScheduleById(String orgId);
    int countByOrgId(String orgId);
}
