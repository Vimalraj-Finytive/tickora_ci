package com.uniq.tms.tms_microservice.repository;

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


    @Query("SELECT g FROM GroupEntity g WHERE g.groupName = :groupName AND g.organizationEntity.id = :orgId")
    Optional<GroupEntity> findBygroupNameAndOrganizationId(@Param("groupName") String teamName, @Param("orgId") Long orgId);

    @Query(value = """
    WITH group_users_with_type AS (
        SELECT 
            g.group_id AS groupid,
            g.group_name AS groupname,
            g.location_id,
            jsonb_array_elements_text(g.group_members_id)::int AS user_id,
            'Member' AS type,
            g.managers_id
        FROM group_table g
        WHERE g.organization_id = :orgId

        UNION ALL

        SELECT 
            g.group_id AS groupid,
            g.group_name AS groupname,
            g.location_id,
            jsonb_array_elements_text(g.supervisors_id)::int AS user_id,
            'Supervisor' AS type,
            g.managers_id
        FROM group_table g
        WHERE g.organization_id = :orgId
    ),

    user_group_info AS (
        SELECT 
            u.user_id,
            ARRAY_AGG(DISTINCT g.groupname) AS groups,
            ARRAY_AGG(DISTINCT l.name) AS locations
        FROM group_users_with_type g
        JOIN users u ON u.user_id = g.user_id
        LEFT JOIN location l ON g.location_id = l.location_id
        GROUP BY u.user_id
    ),

    manager_names AS (
        SELECT 
            u.user_id, 
            u.user_name
        FROM users u
    ),

    group_members_id AS (
        SELECT 
            u.user_id AS userid,
            u.user_name AS username,
            u.email AS useremail,
            r.name AS userrole,
            u.date_of_joining
        FROM users u
        LEFT JOIN role r ON u.role_id = r.role_id
    ),

    group_data_with_members AS (
        SELECT 
            gu.groupid,
            gu.groupname,
            gu.location_id,
            gm.userid,
            gm.username,
            COALESCE(gm.userrole, 'staff') AS userrole,
            gm.useremail,
            gm.date_of_joining,
            gu.type AS usertype,
            ugi.groups,
            ugi.locations,
            m.user_name AS managername
        FROM group_users_with_type gu
        LEFT JOIN group_members_id gm ON gm.userid = gu.user_id
        LEFT JOIN user_group_info ugi ON ugi.user_id = gu.user_id
        LEFT JOIN manager_names m ON m.user_id = ANY (
            SELECT jsonb_array_elements_text(gu.managers_id)::int
        )
    )

    SELECT 
        gdm.groupid,
        gdm.groupname,
        JSONB_AGG(DISTINCT gdm.managername) FILTER (WHERE gdm.managername IS NOT NULL) AS managername,
        COALESCE(l.name, 'Unknown Location') AS location,
        JSONB_AGG(
            DISTINCT JSONB_BUILD_OBJECT(
                'id', gdm.userid,
                'name', gdm.username,
                'role', gdm.userrole,
                'email', gdm.useremail,
                'dateJoined', gdm.date_of_joining,
                'Type', gdm.usertype,
                'GroupName', gdm.groups,
                'location', gdm.locations
            )
        ) FILTER (WHERE gdm.userid IS NOT NULL) AS groupmember
    FROM group_data_with_members gdm
    LEFT JOIN location l ON gdm.location_id = l.location_id
    GROUP BY gdm.groupid, gdm.groupname, l.name
""", nativeQuery = true)
    List<Object[]> getGroupDataNative(@Param("orgId") Long orgId);


    @Modifying
    @Transactional
    @Query(value = """
    UPDATE group_table
    SET group_members_id = (
        SELECT jsonb_agg(elem)
        FROM jsonb_array_elements_text(group_members_id) AS elem
        WHERE elem::int <> :memberId
    )
    WHERE group_id = :groupId
""", nativeQuery = true)
    void deleteMemberById(@Param("groupId") Long groupId, @Param("memberId") Long memberId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = """
        DELETE FROM group_table
        WHERE group_id = :groupId
        """, nativeQuery = true)
    void deleteGroupById(@Param("groupId") Long groupId);
}
