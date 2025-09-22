package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.FixedWorkScheduleEntity;
import com.uniq.tms.tms_microservice.entity.FlexibleWorkScheduleEntity;
import com.uniq.tms.tms_microservice.projection.WorkScheduleData;
import com.uniq.tms.tms_microservice.entity.WorkScheduleEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkScheduleEntity, Long> {

    @Query("SELECT w FROM WorkScheduleEntity w WHERE w.isDefault = true AND w.isActive = true AND w.organizationEntity.organizationId = :orgId")
    WorkScheduleEntity findDefaultActiveSchedule(@Param("orgId") String orgId);


    WorkScheduleEntity findByScheduleIdAndOrganizationEntity_OrganizationId(String scheduleId, String orgId);

    @Query("SELECT MAX(w.scheduleId) FROM WorkScheduleEntity w WHERE w.scheduleId LIKE CONCAT(:prefix, '%')")
    String findMaxIdByPrefix(@Param("prefix") String prefix);

    @Query(value = """
            SELECT
              w.work_schedule_id AS workScheduleId,
              w.work_schedule_name AS workScheduleName,
              COALESCE(w.is_default, false) AS isDefault,
              COALESCE(w.is_active, false) AS isActive,
              w.organization_id AS organizationId,
              w.work_schedule_type AS workScheduleType,
            
              (
                SELECT json_agg(jsonb_build_object(
                  'day', fws.day,
                  'startTime', CAST(fws.start_time AS text),
                  'endTime', CAST(fws.end_time AS text),
                  'duration', CAST(fws.duration AS text)
                ))
                FROM fixed_work_schedule fws
                WHERE fws.work_schedule_id = w.work_schedule_id
              )\\:\\:text AS fixedSchedule,
            
              (
                SELECT json_agg(jsonb_build_object(
                  'day', fls.day,
                  'duration', CAST(fls.duration AS text)
                ))
                FROM flexible_work_schedule fls
                WHERE fls.work_schedule_id = w.work_schedule_id
              )\\:\\:text AS flexibleSchedule,
            
              (
                SELECT jsonb_build_object(
                  'duration', CAST(COALESCE(wws.duration, 0) AS text),
                  'startDay', wws.start_day,
                  'endDay', wws.end_day
                )
                FROM weekly_work_schedule wws
                WHERE wws.work_schedule_id = w.work_schedule_id
                LIMIT 1
              )\\:\\:text AS weeklySchedule
            
            FROM work_schedule w
            WHERE w.organization_id = :orgId
              AND w.is_active = true
            """, nativeQuery = true)
    List<WorkScheduleData> findAllWithChildrenByOrgId(@Param("orgId") String orgId);

    @Query("SELECT w FROM WorkScheduleEntity w WHERE w.scheduleName = :scheduleName AND w.organizationEntity.organizationId = :orgId")
    Optional<WorkScheduleEntity> findBySchedule(@Param("scheduleName") String scheduleName, @Param("orgId") String orgId);

    @Modifying
    @Transactional
    @Query("UPDATE WorkScheduleEntity w SET w.isDefault = false " +
            " WHERE w.organizationEntity.organizationId = :orgId AND w.isDefault = true")
    void resetDefaultLocation(@Param("orgId") String orgId);

    @Modifying
    @Transactional
    @Query("UPDATE WorkScheduleEntity w SET w.isDefault = false " +
            " WHERE w.organizationEntity.organizationId = :orgId " +
            " AND w.scheduleId <> :scheduleId " +
            " AND w.isDefault = true")
    void updateDefaultWorkSchedule(@Param("orgId") String orgId, @Param("scheduleId") String scheduleId);

    @Query("SELECT w FROM WorkScheduleEntity w WHERE w.scheduleName = :scheduleName " +
            "AND w.organizationEntity.organizationId = :orgId " +
            "AND w.scheduleId <> :scheduleId")
    Optional<WorkScheduleEntity> findByScheduleName(
            @Param("scheduleId") String scheduleId,
            @Param("scheduleName") String scheduleName,
            @Param("orgId") String orgId);

    @Modifying
    @Transactional
    @Query("UPDATE WorkScheduleEntity w SET w.isActive = false WHERE w.scheduleId = :scheduleId AND w.organizationEntity.organizationId = :orgId")
    void deactivateScheduleById(@Param("orgId") String orgId, @Param("scheduleId") String scheduleId);


    WorkScheduleEntity findByIsDefaultTrueAndOrganizationEntity_OrganizationId(String orgId);

    @Query("SELECT u.workSchedule FROM UserEntity u " +
            "WHERE u.userId = :userId AND u.workSchedule.isActive = true")
    WorkScheduleEntity findActiveWorkScheduleByUserId(@Param("userId") String userId);

    int countByOrganizationEntity_OrganizationId(String orgId);

    List<WorkScheduleEntity> findScheduleByOrganizationEntity_OrganizationId(String orgId);

    @Query(value = "SELECT work_schedule_id, work_schedule_name FROM work_schedule WHERE organization_id = :orgId", nativeQuery = true)
    List<Object[]> findSchedule(@Param("orgId") String orgId);

    @Query("SELECT ws FROM WorkScheduleEntity ws " +
            "LEFT JOIN FETCH ws.users u " +
            "LEFT JOIN FETCH ws.fixedWorkSchedules fws " +
            "LEFT JOIN FETCH ws.flexibleWorkSchedules flws " +
            "LEFT JOIN FETCH ws.weeklyWorkSchedule wws " +
            "WHERE u.userId IN :userIds AND ws.isActive = true")
    List<WorkScheduleEntity> findAllSchedulesWithUsers(@Param("userIds") List<String> userIds);

    @Query("""
    SELECT f
    FROM FixedWorkScheduleEntity f
    JOIN f.workScheduleEntity w
    JOIN UserEntity u ON u.workSchedule.id = w.id
    WHERE u.userId IN :userIds
""")
    List<FixedWorkScheduleEntity> findFixedSchedulesByUserIds(@Param("userIds") List<String> userIds);

    @Query("""
    SELECT f
    FROM FlexibleWorkScheduleEntity f
    JOIN f.workScheduleEntity w
    JOIN UserEntity u ON u.workSchedule.id = w.id
    WHERE u.userId IN :userIds
""")
    List<FlexibleWorkScheduleEntity> findFlexibleSchedulesByUserIds(@Param("userIds") List<String> userIds);
}
