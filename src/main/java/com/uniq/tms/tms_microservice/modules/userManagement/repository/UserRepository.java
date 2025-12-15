package com.uniq.tms.tms_microservice.modules.userManagement.repository;

import com.uniq.tms.tms_microservice.modules.timesheetManagement.projection.TimesheetUserProjection;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.UserNameEmailDto;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserGroupEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.UserNameSuggestionDto;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.projection.TimesheetProjection;
import com.uniq.tms.tms_microservice.modules.userManagement.projections.UserCalendarProjection;
import com.uniq.tms.tms_microservice.modules.userManagement.projections.UserProjection;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.WorkScheduleEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    @Query("""
                SELECT ug
                FROM UserGroupEntity ug
                JOIN FETCH ug.user u
                LEFT JOIN FETCH ug.group g
                WHERE u.organizationId = :organizationId
                  AND u.userId = :userId
                  AND u.active = true
            """)
    List<UserGroupEntity> findUserByOrganizationIdAndUserId(
            @Param("organizationId") String organizationId,
            @Param("userId") String userId
    );

    UserEntity findByEmail(String email);

    UserEntity findByMobileNumber(String mobile);


    @Query(value = """
                SELECT *
                FROM user_full_details_view
                WHERE organization_id = :orgId
                  AND active = true
                  AND hierarchy_level > :hierarchyLevel
            """, nativeQuery = true)
    List<UserProjection> findAllUsers(
            @Param("orgId") String orgId,
            @Param("hierarchyLevel") int hierarchyLevel
    );

    @Query("SELECT new com.uniq.tms.tms_microservice.modules.userManagement.dto.UserNameSuggestionDto(u.userId, u.userName) " +
            "FROM UserEntity u WHERE LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<UserNameSuggestionDto> searchUserNamesContaining(@Param("keyword") String keyword);

    @Query("SELECT u FROM UserEntity u WHERE u.organizationId = :orgId AND u.active = true AND u.role.roleId = :roleId")
    List<UserEntity> findUsersByOrgIdAndRoleId(@Param("orgId") String orgId, @Param("roleId") Long roleId);

    @Query("SELECT u FROM UserEntity u WHERE u.organizationId = :orgId AND u.active = true AND u.role.hierarchyLevel IN :higherRoleIds")
    List<UserEntity> findByOrgIdAndRoleId(String orgId, List<Integer> higherRoleIds);

    @Query("SELECT u FROM UserEntity u WHERE u.userId IN :userIds AND u.organizationId = :orgId AND u.active = true")
    List<UserEntity> findByUserIdAndOrgIdAndActiveTrue(@Param("userIds") List<String> userIds, @Param("orgId") String orgId);

    Optional<UserEntity> findByUserIdAndActiveTrue(String userId);

    Optional<UserEntity> findOptionalByMobileNumber(String mobileNumber);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.active = false, u.isRegisterUser = false WHERE u.userId = :userId AND u.organizationId = :orgId")
    void deactivateUserById(String userId, String orgId);

    @Query("SELECT new com.uniq.tms.tms_microservice.modules.userManagement.dto.UserNameSuggestionDto(u.userId, u.userName) " +
            "FROM UserEntity u " +
            "WHERE u.organizationId = :orgId AND u.active = true" + " AND u.role.hierarchyLevel > :hierarchyLevel")
    List<UserNameSuggestionDto> findAllActiveUsersByOrganization(@Param("orgId") String orgId, @Param("hierarchyLevel") int hierarchyLevel);

    @Query("SELECT new com.uniq.tms.tms_microservice.modules.userManagement.dto.UserNameSuggestionDto(u.userId, u.userName) " +
            "FROM UserGroupEntity ug " +
            "JOIN ug.user u " +
            "WHERE ug.group.groupId IN :groupIds AND u.organizationId = :orgId AND u.active = true")
    List<UserNameSuggestionDto> findAllGroupUsersByOrganizationId(@Param("groupIds") List<Long> groupIds,
                                                                  @Param("orgId") String orgId);

    @Query(value = "SELECT mobile_number FROM users WHERE organization_id = :orgId", nativeQuery = true)
    List<String> findAllMobileNumbers(@Param("orgId") String orgId);

    @Query(value = "SELECT email FROM users WHERE organization_id = :orgId", nativeQuery = true)
    List<String> findAllEmails(@Param("orgId") String orgId);

    UserEntity findByOrganizationIdAndUserId(String orgId, String userId);

    @Query("SELECT u FROM UserEntity u WHERE u.organizationId = :orgId AND u.active = true AND u.userId <> :userId AND u.role.hierarchyLevel > :hierarchyLevel")
    List<UserEntity> findAllUsersList(@Param("orgId") String orgId, @Param("userId") String userIdFromToken, @Param("hierarchyLevel") int hierarchyLevel);

    @Query("SELECT u FROM UserEntity u WHERE u.organizationId = :orgId AND u.active = true AND u.role.name IN :roles")
    List<UserEntity> findUserByRoles(Set<String> roles, String orgId);

    @Query("""
            SELECT u FROM UserEntity u
            JOIN UserGroupEntity ug ON u.userId = ug.user.userId
            WHERE u.role.name IN (:roles)
              AND ug.group.groupId IN (:groupIds)
              AND ug.type = 'Member'
              AND u.organizationId = :orgId
              AND u.active = true
            """)
    List<UserEntity> findUsersByRolesAndGroupIds(@Param("roles") Set<String> roles,
                                                 @Param("groupIds") List<Long> groupIds,
                                                 @Param("orgId") String orgId);

    List<UserEntity> findAllActiveUsersByOrganizationId(String orgId);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.workSchedule.scheduleId = :newId WHERE u.workSchedule.scheduleId = :oldId")
    void updateUserWorkSchedule(@Param("oldId") String oldId, @Param("newId") String newId);

    @Query("SELECT u.userId FROM UserEntity u WHERE u.userId LIKE CONCAT(:orgPrefix, '%') ORDER BY u.userId DESC LIMIT 1")
    String findLastUserIdByOrgPrefix(@Param("orgPrefix") String orgPrefix);

    boolean existsByUserId(String userId);

    @Query(value = """
                SELECT *
                FROM user_full_details_view
                WHERE organization_id = :orgId
                  AND active = false
                  AND hierarchy_level > :hierarchyLevel
            """, nativeQuery = true)
    List<UserProjection> findAllInActiveUsers(@Param("orgId") String orgId, @Param("hierarchyLevel") int hierarchyLevel);

    @Query("""
                SELECT u FROM UserEntity u
                WHERE LOWER(u.userId) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(u.mobileNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<UserEntity> searchUsers(@Param("keyword") String keyword);

    @Query("""
            SELECT u.userId AS userId,
                   u.userName AS userName,
                   u.mobileNumber AS mobileNumber,
                   u.dateOfJoining AS date,
                   r.name AS roleName,
                   ws.scheduleName AS workScheduleName,
                   STRING_AGG(g.groupName, ',') AS groupNames
            FROM UserEntity u
            LEFT JOIN u.role r
            LEFT JOIN u.workSchedule ws
            LEFT JOIN UserGroupEntity ug ON ug.user.userId = u.userId
            LEFT JOIN GroupEntity g ON g.id = ug.group.groupId
            WHERE u.userId IN :userIds
            GROUP BY u.userId, u.userName, u.mobileNumber, u.dateOfJoining, r.name, ws.scheduleName
            """)
    List<TimesheetProjection> findUsersByIds(@Param("userIds") List<String> userIds);

    @Query(value = """
            SELECT u.user_id AS userId,
                   u.user_name AS userName,
                   u.mobile_number AS mobileNumber,
                   u.date_of_joining AS date,
                   r.name AS roleName,
                   ws.work_schedule_name AS workScheduleName,
                   STRING_AGG(g.group_name, ',') AS groupNames
            FROM users u
            LEFT JOIN role r ON u.role_id = r.role_id
            LEFT JOIN work_schedule ws ON u.work_schedule_id = ws.work_schedule_id
            LEFT JOIN user_group ug ON ug.user_id = u.user_id
            LEFT JOIN org_groups g ON g.group_id = ug.group_id
            WHERE u.user_id = ANY(:userIds)
              AND u.active = true
            GROUP BY u.user_id, u.user_name, u.mobile_number, u.date_of_joining, r.name, ws.work_schedule_name
            """, nativeQuery = true)
    Page<TimesheetProjection> findUsersByUserIds(
            @Param("userIds") String[] userIds,
            Pageable pageable
    );

    @Query(value = """
            SELECT u.user_id AS userId,
                   u.user_name AS userName,
            FROM users u
            WHERE u.user_id = ANY(:userIds)
              AND u.active = true
            GROUP BY u.user_id, u.user_name
            """, nativeQuery = true)
    List<TimesheetUserProjection> findUsersByUserIds(
            @Param("userIds") String[] userIds);

    List<UserEntity> findUserByOrganizationIdAndRole_RoleId(String orgId, int roleId);

    long countByOrganizationId(String orgId);

    long countByOrganizationIdAndActiveTrue(String orgId);

    long countByOrganizationIdAndActiveFalse(String orgId);

    List<UserEntity> findByUserIdIn(List<String> userId);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.active = false, u.isRegisterUser = false WHERE u.userId IN :userIds AND u.organizationId = :orgId")
    void deactivateUsersByIds(@Param("userIds") List<String> userIds, @Param("orgId") String orgId);

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.organizationId = :orgId")
    Long countUsersByOrganizationId(@io.lettuce.core.dynamic.annotation.Param("orgId") String orgId);

    @Query("SELECT new com.uniq.tms.tms_microservice.modules.userManagement.dto.UserNameEmailDto(u.userName, u.email) " +
            "FROM UserEntity u WHERE u.role.roleId IN (1, 2)")
    List<UserNameEmailDto> findAdminAndSuperAdminNamesAndEmails();

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.workSchedule = :workSchedule WHERE u.userId IN :userIds AND u.organizationId = :orgId")
    int bulkUpdateWorkSchedule(@Param("workSchedule") WorkScheduleEntity workSchedule,
                               @Param("userIds") List<String> userIds,
                               @Param("orgId") String orgId);

    @Query("SELECT COUNT(u) FROM UserEntity u " +
            "WHERE u.organizationId = :orgId " +
            "AND u.createdAt BETWEEN :start AND :end")
    long countUsersByOrgAndCreatedAtBetween(@Param("orgId") String orgId,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT 
                u.user_id AS userId,
                u.date_of_joining AS date,
                u.user_name AS userName
            FROM users u
            WHERE u.user_id = ANY(:userIds)
              AND u.active = true
            """, nativeQuery = true)
    List<TimesheetUserProjection> findUserByUserIds(@Param("userIds") String[] userIds);

    @Query("SELECT u FROM UserEntity u WHERE u.userId IN :userIds AND u.active = true")
    List<UserEntity> findByUserIdAndActiveTrue(@Param("userIds") List<String> userIds);


    @Query("SELECT u.userName FROM UserEntity u WHERE u.userId = :userId")
    String findUsernameByUserId(@Param("userId") String userId);

    @Query("""
                SELECT u.userId AS userId, u.calendar.id AS calendarId
                FROM UserEntity u
                WHERE u.userId IN :userIds
            """)
    List<UserCalendarProjection> findCalendarIdsByUserIds(@Param("userIds") String[] userIds);

    @Query("""
                SELECT u
                FROM UserEntity u
                WHERE u.userId IN :userIds
            """)
    List<UserEntity> findUsersWithCalendars(@Param("userIds") String[] userIds);

    @Query("SELECT u FROM UserEntity u WHERE u.active = true AND u.userId <> :excludeId")
    List<UserEntity> findByActiveTrue(@Param("excludeId") String excludeId);

    @Query("SELECT u FROM UserEntity u WHERE u.role.roleId = 1 AND u.organizationId = :orgId")
    Optional<UserEntity> findSuperAdminByOrgId(@Param("orgId") String orgId);

    List<UserEntity> findByRequestApproverIdAndActiveTrue(String approverId);

    List<UserEntity> findAllByUserIdInAndActiveTrue(List<String> userIds);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.requestApproverId = :approverId WHERE u.id IN (:requestedUserIds)")
    void updateApproverForUsers(@Param("approverId") String approverId,
                                @Param("requestedUserIds") List<String> requestedUserIds);

    Optional<UserEntity> findByUserId(String userId);

    @Query("SELECT u.requestApproverId FROM UserEntity u WHERE u.userId = :userId")
    String findApproverIdByUserId(@Param("userId") String userId);

    @Query("SELECT u.calendar.id FROM UserEntity u WHERE u.userId = :userId")
    String getCalendarIdByUserId(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.requestApproverId = :newApproverId WHERE u.id IN :userIds")
    void updateUserApprover(@Param("newApproverId") String newApproverId,
                               @Param("userIds") List<String> userIds);

    @Query("SELECT u.userId FROM UserEntity u WHERE u.active = true")
    List<String> findAllActiveUsers();

    @Query(value = """
                SELECT *
                FROM user_full_details_view
                WHERE active = true
            """, nativeQuery = true)
    List<UserProjection> findAllUsers();

    @Query(value = """
                SELECT *
                FROM user_full_details_view
                WHERE user_id = :userId
                  AND active = true
            """, nativeQuery = true)
    List<UserProjection> findUserByUserId(@Param("userId") String userId);
}
