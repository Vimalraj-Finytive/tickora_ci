package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.dto.GroupDto;
import com.uniq.tms.tms_microservice.entity.GroupEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<GroupEntity, Long> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE group_table SET group_name = :groupName, location_id = :locationId WHERE group_id = :groupId", nativeQuery = true)
    void updateGroupNameAndLocation(@Param("groupId") Long groupId,
                                    @Param("groupName") String groupName,
                                    @Param("locationId") Long locationId);

    @Query("SELECT g FROM GroupEntity g WHERE g.groupName = :groupName AND g.organizationEntity.id = :orgId")
    Optional<GroupEntity> findBygroupNameAndOrganizationId(@Param("groupName") String teamName, @Param("orgId") Long orgId);
    @Query(value = """
    WITH group_base AS (
        SELECT 
            g.group_id AS groupid,
            g.group_name AS groupname,
            g.location_id,
            g.organization_id
        FROM group_table g
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
            u.location_id,
            u.active
        FROM users u
        LEFT JOIN role r ON u.role_id = r.role_id
    ),
    group_data AS (
        SELECT 
            gb.groupid,
            gb.groupname,
            gb.location_id,
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
    GROUP BY gd.groupid, gd.groupname, l.name
    """, nativeQuery = true)
    List<Object[]> getGroupDataNative(@Param("orgId") Long orgId);

    @Modifying
    @Transactional
    @Query(value = """
    DELETE FROM user_group
    WHERE group_id = :groupId AND user_id = :memberId
""", nativeQuery = true)
    void deleteMemberById(@Param("groupId") Long groupId, @Param("memberId") Long memberId);

    @Query("""
    SELECT COUNT(g) > 0
    FROM GroupEntity g
    WHERE g.groupName = :groupName
      AND g.organizationEntity.organizationId = :orgId
      AND g.groupId <> :groupId
""")
    boolean existsGroupNameInOrganization(
            @Param("groupName") String groupName,
            @Param("orgId") Long orgId,
            @Param("groupId") Long groupId
    );

    @Query(value = """
        SELECT 
            g.group_id AS groupId,
            g.group_name AS groupName
        FROM 
            group_table g
        JOIN 
            user_group ug ON g.group_id = ug.group_id
        WHERE 
            ug.user_id = :userId
            AND g.organization_id = :orgId
            AND ug.type = 'Supervisor'
    """, nativeQuery = true)
    List<GroupDto> findByUserIdAndOrganization_id(
            @Param("userId") Long userId,
            @Param("orgId") Long orgId
    );

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = """
        DELETE FROM group_table
        WHERE group_id = :groupId
        """, nativeQuery = true)
    void deleteGroupById(@Param("groupId") Long groupId);

    @Query(value = """
        SELECT 
            g.group_id AS groupId,
            g.group_name AS groupName
        FROM 
            group_table g
        WHERE 
            g.organization_id = :orgId
    """, nativeQuery = true)
    List<GroupDto> findByOrganizationId(Long orgId);
}
