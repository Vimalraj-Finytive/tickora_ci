package com.uniq.tms.tms_microservice.repository;

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

    List<WorkScheduleEntity> findAllByOrganizationEntity_OrganizationId(Long orgId);

    @Query("SELECT MAX(w.scheduleId) FROM WorkScheduleEntity w WHERE w.scheduleId LIKE CONCAT(:prefix, '%')")
    String findMaxIdByPrefix(@Param("prefix") String prefix);

    @Transactional
    @Query(value = """
        SELECT DISTINCT w.*,
                        fws.fixed_work_schedule_id as fws_id,
                        fls.flexible_work_schedule_id as fls_id,
                        wws.weekly_work_schedule_id as wws_id
        FROM work_schedule w
        LEFT JOIN fixed_work_schedule fws ON w.work_schedule_id = fws.work_schedule_id
        LEFT JOIN flexible_work_schedule fls ON w.work_schedule_id = fls.work_schedule_id
        LEFT JOIN weekly_work_schedule wws ON w.work_schedule_id = wws.work_schedule_id
        WHERE w.organization_id = :orgId AND w.is_active = true
        """, nativeQuery = true)
        List<WorkScheduleEntity> findAllWithChildrenByOrgId(@Param("orgId") Long orgId);

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
