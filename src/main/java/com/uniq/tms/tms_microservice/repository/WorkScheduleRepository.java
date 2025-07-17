package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.dto.WorkScheduleData;
import com.uniq.tms.tms_microservice.entity.WorkScheduleEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkScheduleEntity, Long> {

    @Query("SELECT w FROM WorkScheduleEntity w WHERE w.isDefault = true AND w.isActive = true AND w.organizationEntity.organizationId = :orgId")
    WorkScheduleEntity findDefaultActiveSchedule(@Param("orgId") Long orgId);


    WorkScheduleEntity findByScheduleId(String scheduleId);

    @Query("SELECT MAX(w.scheduleId) FROM WorkScheduleEntity w WHERE w.scheduleId LIKE CONCAT(:prefix, '%')")
    String findMaxIdByPrefix(@Param("prefix") String prefix);

    @Query(value = """
    SELECT
      w.work_schedule_id as workScheduleId,
      w.work_schedule_name as workScheduleName,
      COALESCE(w.is_default, false) as isDefault,
      COALESCE(w.is_active, false) as isActive,
      w.organization_id as organizationId,
      w.work_schedule_type as workScheduleType,
      w.work_schedule_name as workScheduleName,

      (
        SELECT json_agg(jsonb_build_object(
          'day', fws.day,
          'startTime', fws.start_time::text,
          'endTime', fws.end_time::text,
          'duration', fws.duration::text
        ))
        FROM fixed_work_schedule fws
        WHERE fws.work_schedule_id = w.work_schedule_id
      )::text AS fixedSchedule,

      (
        SELECT json_agg(jsonb_build_object(
          'day', fls.day,
          'duration', fls.duration::text
        ))
        FROM flexible_work_schedule fls
        WHERE fls.work_schedule_id = w.work_schedule_id
      )::text AS flexibleSchedule,

      (
        SELECT jsonb_build_object(
          'duration', COALESCE(wws.duration, 0)::text,
          'startDay', wws.start_day,
          'endDay', wws.end_day
        )
        FROM weekly_work_schedule wws
        WHERE wws.work_schedule_id = w.work_schedule_id
        LIMIT 1
      )::text AS weeklySchedule

    FROM work_schedule w
    WHERE w.organization_id = :orgId
      AND w.is_active = true
""", nativeQuery = true)
    List<WorkScheduleData> findAllWithChildrenByOrgId(@Param("orgId") Long orgId);

    @Query("SELECT w FROM WorkScheduleEntity w WHERE w.scheduleName = :scheduleName AND w.organizationEntity.organizationId = :orgId")
    Optional<WorkScheduleEntity> findBySchedule(@Param("scheduleName") String scheduleName, @Param("orgId") Long orgId);

    @Modifying
    @Transactional
    @Query("UPDATE WorkScheduleEntity w SET w.isDefault = false " +
            " WHERE w.organizationEntity.organizationId = :orgId AND w.isDefault = true ")
    void resetDefaultLocation(@Param("orgId") Long orgId);

    @Modifying
    @Transactional
    @Query("UPDATE WorkScheduleEntity w SET w.isDefault = false " +
            " WHERE w.organizationEntity.organizationId = :orgId AND w.isDefault = true " +
            " AND w.scheduleId = :scheduleId")
    void updateDefaultWorkSchedule(Long orgId, String scheduleId);

    @Query("SELECT w FROM WorkScheduleEntity w WHERE w.scheduleName = :scheduleName " +
            "AND w.organizationEntity.organizationId = :orgId " +
            "AND w.scheduleId <> :scheduleId")
    Optional<WorkScheduleEntity> findByScheduleName(
            @Param("scheduleId") String scheduleId,
            @Param("scheduleName") String scheduleName,
            @Param("orgId") Long orgId);

    @Modifying
    @Transactional
    @Query("UPDATE WorkScheduleEntity w SET w.isActive = false WHERE w.scheduleId = :scheduleId AND w.organizationEntity.organizationId = :orgId")
    void deactivateScheduleById(@Param("orgId") Long orgId, @Param("scheduleId") String scheduleId);


    WorkScheduleEntity findByIsDefaultTrueAndOrganizationEntity_OrganizationId(Long orgId);

    @Query("SELECT u.workSchedule FROM UserEntity u " +
            "WHERE u.userId = :userId AND u.workSchedule.isActive = true")
    WorkScheduleEntity findActiveWorkScheduleByUserId(@Param("userId") Long userId);

}
