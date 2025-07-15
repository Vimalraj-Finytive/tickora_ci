package com.uniq.tms.tms_microservice.adapter.impl;

import com.uniq.tms.tms_microservice.adapter.WorkScheduleAdapter;
import com.uniq.tms.tms_microservice.entity.*;
import com.uniq.tms.tms_microservice.repository.*;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class WorkScheduleAdapterImpl implements WorkScheduleAdapter {

    private final WorkScheduleRepository workScheduleRepository;
    private final FixedWorkScheduleRepository fixedWorkScheduleRepository;
    private final FlexibleWorkScheduleRepository flexibleWorkScheduleRepository;
    private final WeeklyWorkScheduleRepository weeklyWorkScheduleRepository;
    private final WorkScheduleTypeRepository workScheduleTypeRepository;

    public WorkScheduleAdapterImpl(WorkScheduleRepository workScheduleRepository, FixedWorkScheduleRepository fixedWorkScheduleRepository, FlexibleWorkScheduleRepository flexibleWorkScheduleRepository, WeeklyWorkScheduleRepository weeklyWorkScheduleRepository, WorkScheduleTypeRepository workScheduleTypeRepository) {
        this.workScheduleRepository = workScheduleRepository;
        this.fixedWorkScheduleRepository = fixedWorkScheduleRepository;
        this.flexibleWorkScheduleRepository = flexibleWorkScheduleRepository;
        this.weeklyWorkScheduleRepository = weeklyWorkScheduleRepository;
        this.workScheduleTypeRepository = workScheduleTypeRepository;
    }

    public List<WorkScheduleEntity> getWorkSchedule(Long orgId) {
        return workScheduleRepository.findAllWithChildrenByOrgId(orgId);
    }


    @Override
    public WorkScheduleEntity findByWorkscheduleId(String workScheduleId) {
        return workScheduleRepository.findByScheduleId(workScheduleId);
    }

    @Override
    public WorkScheduleEntity findDefaultActiveSchedule(Long orgId) {
        return workScheduleRepository.findDefaultActiveSchedule(orgId);
    }

    @Override
    public void saveWorkSchedule(WorkScheduleEntity entity) {
         workScheduleRepository.save(entity);
    }

    @Override
    public void saveAll(List<FixedWorkScheduleEntity> entities) {
         fixedWorkScheduleRepository.saveAll(entities);
    }

    @Override
    public void saveAllFlexible(List<FlexibleWorkScheduleEntity> entities) {
        flexibleWorkScheduleRepository.saveAll(entities);
    }

    @Override
    public void save(WeeklyWorkScheduleEntity entity) {
        weeklyWorkScheduleRepository.save(entity);
    }

    @Override
    public Optional<WorkScheduleTypeEntity> findById(java.lang.String type) {
        return workScheduleTypeRepository.findById(type);
    }

    @Override
    public void addType(WorkScheduleTypeEntity entity) {
        workScheduleTypeRepository.save(entity);
    }

    @Override
    public WorkScheduleEntity findByScheduleId(String scheduleId) {
        return workScheduleRepository.findByScheduleId(scheduleId);
    }

    @Override
    public void deleteAllChildren(String scheduleId) {
        fixedWorkScheduleRepository.deleteByScheduleId(scheduleId);
        flexibleWorkScheduleRepository.deleteByScheduleId(scheduleId);
        weeklyWorkScheduleRepository.deleteByScheduleId(scheduleId);
    }

    @Override
    public boolean findByWorkschedule(String scheduleName, Long orgId) {
        return workScheduleRepository.findBySchedule(scheduleName, orgId).isPresent();
    }

    @Override
    public void resetDefaultWorkSchedule(Long orgId) {
        workScheduleRepository.resetDefaultLocation(orgId);
    }

    @Override
    public void updateDefaultWorkSchedule(Long orgId, String scheduleId){
        workScheduleRepository.updateDefaultWorkSchedule(orgId, scheduleId);
    }

    @Override
    public boolean findByScheduleName(String scheduleId, String scheduleName, Long orgId){
        return workScheduleRepository.findByScheduleName(scheduleId,scheduleName, orgId).isPresent();
    }

    @Override
    public WorkScheduleEntity findDefaultScheduleByOrgId(Long orgId) {
        return workScheduleRepository.findByIsDefaultTrueAndOrganizationEntity_OrganizationId(orgId);
    }

    @Override
    public WorkScheduleEntity getScheduleForUser(Long userId) {
        return workScheduleRepository.findActiveWorkScheduleByUserId(userId);
    }

    @Override
    public List<FlexibleWorkScheduleEntity> findByWorkScheduleId(String scheduleId) {
        return flexibleWorkScheduleRepository.findByworkScheduleEntity_scheduleId(scheduleId);
    }

    @Override
    public List<FixedWorkScheduleEntity> findByFixedScheduleId(String scheduleId) {
        return fixedWorkScheduleRepository.findByworkScheduleEntity_scheduleId(scheduleId);
    }

    @Override
    public List<WorkScheduleTypeEntity> findAllType() {
        return workScheduleTypeRepository.findAll();
    }
}
