package com.uniq.tms.tms_microservice.adapter.impl;

import com.uniq.tms.tms_microservice.adapter.WorkScheduleAdapter;
import com.uniq.tms.tms_microservice.entity.*;
import com.uniq.tms.tms_microservice.repository.*;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public WorkScheduleEntity findDefaultActiveSchedule(String orgId) {
        return workScheduleRepository.findDefaultActiveSchedule(orgId);
    }

    @Override
    public WorkScheduleEntity saveWorkSchedule(WorkScheduleEntity entity) {
        return workScheduleRepository.save(entity);
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
    public Optional<WorkScheduleTypeEntity> findById(String type) {
        return workScheduleTypeRepository.findById(type);
    }

    @Override
    public void addType(WorkScheduleTypeEntity entity) {
        workScheduleTypeRepository.save(entity);
    }

    @Override
    public WorkScheduleEntity findByScheduleId(String scheduleId, String orgId) {
        return workScheduleRepository.findByScheduleIdAndOrganizationEntity_OrganizationId(scheduleId, orgId);
    }

    @Override
    public void deleteAllChildren(String scheduleId) {
        fixedWorkScheduleRepository.deleteByScheduleId(scheduleId);
        flexibleWorkScheduleRepository.deleteByScheduleId(scheduleId);
        weeklyWorkScheduleRepository.deleteByScheduleId(scheduleId);
    }

    @Override
    public boolean findByWorkschedule(String scheduleName, String orgId) {
        return workScheduleRepository.findBySchedule(scheduleName, orgId).isPresent();
    }

    @Override
    public void resetDefaultWorkSchedule(String orgId) {
        workScheduleRepository.resetDefaultLocation(orgId);
    }

    @Override
    public void updateDefaultWorkSchedule(String orgId, String scheduleId){
        workScheduleRepository.updateDefaultWorkSchedule(orgId, scheduleId);
    }

    @Override
    public boolean findByScheduleName(String scheduleId, String scheduleName, String orgId){
        return workScheduleRepository.findByScheduleName(scheduleId,scheduleName, orgId).isPresent();
    }

    @Override
    public WorkScheduleEntity findDefaultScheduleByOrgId(String orgId) {
        return workScheduleRepository.findByIsDefaultTrueAndOrganizationEntity_OrganizationId(orgId);
    }

    @Override
    public WorkScheduleEntity getScheduleForUser(String userId) {
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

    @Override
    public List<WorkScheduleEntity> findAllScheduleById(String orgId) {
        return workScheduleRepository.findScheduleByOrganizationEntity_OrganizationId(orgId);
    }

    @Override
    public int countByOrgId(String orgId) {
        return workScheduleRepository.countByOrganizationEntity_OrganizationId(orgId);
    }

    @Override
    public Map<String, String> getAllSchedules(String orgId) {
        List<Object[]> workSchedules = workScheduleRepository.findSchedule(orgId);
        Map<String, String> workScheduleMap = new HashMap<>();
        for(Object[] workSchedule : workSchedules){
            workScheduleMap.put(((String) workSchedule[1]).toLowerCase(), (String) workSchedule[0]);
        }
        return workScheduleMap;
    }
}
