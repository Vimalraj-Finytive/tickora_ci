package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.WorkScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkScheduleEntity, Long> {

    @Query("SELECT w FROM WorkScheduleEntity w WHERE w.isDefault = true AND w.isActive = true")
    WorkScheduleEntity findDefaultActiveSchedule();
    WorkScheduleEntity findByScheduleId(Long scheduleId);

    List<WorkScheduleEntity> findAllByOrganizationEntity_OrganizationId(Long orgId);
}
