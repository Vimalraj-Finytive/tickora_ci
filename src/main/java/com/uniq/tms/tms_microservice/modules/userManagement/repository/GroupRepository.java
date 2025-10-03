package com.uniq.tms.tms_microservice.modules.userManagement.repository;

import com.uniq.tms.tms_microservice.modules.userManagement.dto.GroupDto;
import com.uniq.tms.tms_microservice.modules.userManagement.projections.GroupsData;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.GroupEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {

    @Query("SELECT g FROM GroupEntity g WHERE g.groupName = :groupName AND g.organizationEntity.id = :orgId")
    Optional<GroupEntity> findByGroupNameAndOrganizationId(@Param("groupName") String teamName, @Param("orgId") String orgId);
    @Query(value = """
    WITH group_base AS (
        SELECT
            g.group_id AS groupid,
            g.group_name AS groupname,
            g.location_id,
            g.work_schedule_id,
            g.organization_id
        FROM org_groups g
        WHERE g.organization_id = :orgId
    ),
    user_group_details AS (
        SELECT
            ug.group_id,
            ug.user_id,
            ug.type
        FROM user_group ug
    ),
    user_info AS (
        SELECT
            u.user_id,
            u.user_name,
            u.email,
            u.date_of_joining,
            r.name AS role_name,
            u.active
        FROM users u
        LEFT JOIN role r ON u.role_id = r.role_id
    ),
    group_data AS (
        SELECT
            gb.groupid,
            gb.groupname,
            gb.location_id,
            gb.work_schedule_id,
            ugd.user_id,
            ui.user_name,
            ui.email,
            ui.date_of_joining,
            ui.role_name,
            ui.active,
            ugd.type AS user_type
        FROM group_base gb
        LEFT JOIN user_group_details ugd ON gb.groupid = ugd.group_id
        LEFT JOIN user_info ui ON ui.user_id = ugd.user_id
    )
    SELECT
        gd.groupid,
        gd.groupname,
        COALESCE(l.name, 'Unknown Location') AS location,
        ws.work_schedule_name AS work_schedule,
        JSONB_AGG(
            JSONB_BUILD_OBJECT(
                'userId', gd.user_id,
                'userName', gd.user_name,
                'email', gd.email,
                'dateOfJoining', gd.date_of_joining,
                'role', gd.role_name,
                'type', gd.user_type,
                'active', gd.active
            )
        ) FILTER (WHERE gd.user_id IS NOT NULL) AS members_details
    FROM group_data gd
    LEFT JOIN location l ON gd.location_id = l.location_id
    LEFT JOIN work_schedule ws ON gd.work_schedule_id = ws.work_schedule_id
    GROUP BY gd.groupid, gd.groupname, l.name, ws.work_schedule_name
    """, nativeQuery = true)
    List<GroupsData> getGroupData(@Param("orgId") String orgId);

    @Modifying
    @Transactional
    @Query(value = """
    DELETE FROM user_group
    WHERE group_id = :groupId AND user_id = :memberId
""", nativeQuery = true)
    void deleteMemberById(@Param("groupId") Long groupId, @Param("memberId") String memberId);

    @Query("""
    SELECT COUNT(g) > 0
    FROM GroupEntity g
    WHERE g.groupName = :groupName
      AND g.organizationEntity.organizationId = :orgId
      AND g.groupId <> :groupId
""")
    boolean existsGroupNameInOrganization(
            @Param("groupName") String groupName,
            @Param("orgId") String orgId,
            @Param("groupId") Long groupId
    );

    @Query(value = """
        SELECT
            g.group_id AS groupId,
            g.group_name AS groupName
        FROM
            org_groups g
        JOIN
            user_group ug ON g.group_id = ug.group_id
        WHERE
            ug.user_id = :userId
            AND g.organization_id = :orgId
            AND ug.type = 'Supervisor'
    """, nativeQuery = true)
    List<GroupDto> findByUserIdAndOrganizationId(
            @Param("userId") String userId,
            @Param("orgId") String orgId
    );

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = """
        DELETE FROM org_groups
        WHERE group_id = :groupId
        AND organization_id = :orgId
        """, nativeQuery = true)
    void deleteGroupById(@Param("groupId") Long groupId, @Param("orgId") String orgId);

    @Query(value = """
        SELECT 
            g.group_id AS groupId,
            g.group_name AS groupName
        FROM 
            org_groups g
        WHERE 
            g.organization_id = :orgId
    """, nativeQuery = true)
    List<GroupDto> findByOrganizationId(String orgId);

    Optional<GroupEntity> findByGroupId(Long groupId);

    @Query(value = "SELECT group_name, group_id FROM org_groups WHERE organization_id = :orgId", nativeQuery = true)
    List<Object[]> findGroupNameIdMappings(@Param("orgId") String orgId);

    List<GroupEntity> findAllByOrganizationEntity_OrganizationId(String orgId);

    boolean existsByGroupIdAndOrganizationEntity_OrganizationId(Long groupId, String orgId);

    @Transactional
    List<GroupEntity> findByLocationEntity_LocationIdIn(List<Long> defaultLocationId);

    @Modifying
    @Transactional
    @Query("UPDATE GroupEntity g SET g.workSchedule.scheduleId = :newId WHERE g.workSchedule.scheduleId = :oldId")
    void updateGroupWorkSchedule(@Param("oldId") String oldId, @Param("newId") String newId);

    @Query("SELECT g.groupName FROM GroupEntity g WHERE g.id = :groupId")
    String findGroupNameByGroupId(@Param("groupId") Long groupId);
}
