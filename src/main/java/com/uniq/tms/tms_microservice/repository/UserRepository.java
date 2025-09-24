package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.entity.UserGroupEntity;
import com.uniq.tms.tms_microservice.model.UserResponse;
import com.uniq.tms.tms_microservice.dto.UserNameSuggestionDto;
import com.uniq.tms.tms_microservice.projection.TimesheetProjection;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
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

    @Query("SELECT new com.uniq.tms.tms_microservice.model.UserResponse(" +
            "u.userId, u.userName, u.email, u.mobileNumber, w.scheduleName, " +
            "COALESCE(g.groupName, '-'), r.name, l.name, u.dateOfJoining, " +
            "sd.userName, sd.mobile, sd.email, sd.relation) " +
            "FROM UserEntity u " +
            "LEFT JOIN UserGroupEntity ug ON ug.user.userId = u.userId " +
            "LEFT JOIN GroupEntity g ON ug.group.groupId = g.groupId " +
            "LEFT JOIN u.workSchedule w " +
            "JOIN RoleEntity r ON u.role = r " +
            "LEFT JOIN UserLocationEntity ul ON ul.user.userId= u.userId " +
            "LEFT JOIN LocationEntity l ON ul.location.locationId = l.locationId " +
            "LEFT JOIN SecondaryDetailsEntity sd ON sd.user.userId = u.userId " +
            "WHERE u.organizationId = :orgId AND u.active = true AND r.hierarchyLevel > :hierarchyLevel")
    List<UserResponse> findAllUsers(@Param("orgId") String orgId, @Param("hierarchyLevel") int hierarchyLevel);

    @Query("SELECT new com.uniq.tms.tms_microservice.dto.UserNameSuggestionDto(u.userId, u.userName) " +
            "FROM UserEntity u WHERE LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<UserNameSuggestionDto> searchUserNamesContaining(@Param("keyword") String keyword);

    @Query("SELECT u FROM UserEntity u WHERE u.organizationId = :orgId AND u.active = true AND u.role.roleId = :roleId")
    List<UserEntity> findUsersByOrgIdAndRoleId(@Param("orgId") String orgId, @Param("roleId") Long roleId);

    @Query("SELECT u FROM UserEntity u WHERE u.organizationId = :orgId AND u.active = true AND u.role.hierarchyLevel IN :higherRoleIds")
    List<UserEntity> findByOrgIdAndRoleId(String orgId, List<Integer> higherRoleIds);

    @Query("SELECT u FROM UserEntity u WHERE u.userId IN :userIds AND u.organizationId = :orgId AND u.active = true")
    List<UserEntity> findByUserIdAndOrgIdAndActiveTrue(@Param("userIds") List<String> userIds, @Param("orgId") String orgId);

    Optional<UserEntity> findByUserId(String userId);

    Optional<UserEntity> findOptionalByMobileNumber(String mobileNumber);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.active = false, u.isRegisterUser = false WHERE u.userId = :userId AND u.organizationId = :orgId")
    void deactivateUserById(String userId, String orgId);

    @Query("SELECT new com.uniq.tms.tms_microservice.dto.UserNameSuggestionDto(u.userId, u.userName) " +
            "FROM UserEntity u " +
            "WHERE u.organizationId = :orgId AND u.active = true" + " AND u.role.hierarchyLevel > :hierarchyLevel")
    List<UserNameSuggestionDto> findAllActiveUsersByOrganization(@Param("orgId") String orgId, @Param("hierarchyLevel") int hierarchyLevel);

    @Query("SELECT new com.uniq.tms.tms_microservice.dto.UserNameSuggestionDto(u.userId, u.userName) " +
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

    List<UserEntity> findUserByOrganizationIdAndRole_RoleId(String orgId, int roleId);

    @Query("SELECT u.userId FROM UserEntity u WHERE u.userId LIKE CONCAT(:orgPrefix, '%') ORDER BY u.userId DESC LIMIT 1")
    String findLastUserIdByOrgPrefix(@Param("orgPrefix") String orgPrefix);

    boolean existsByUserId(String userId);

    @Query("SELECT new com.uniq.tms.tms_microservice.model.UserResponse(" +
            "u.userId, u.userName, u.email, u.mobileNumber, w.scheduleName, " +
            "COALESCE(g.groupName, '-'), r.name, l.name, u.dateOfJoining, " +
            "sd.userName, sd.mobile, sd.email, sd.relation) " +
            "FROM UserEntity u " +
            "LEFT JOIN UserGroupEntity ug ON ug.user.userId = u.userId " +
            "LEFT JOIN GroupEntity g ON ug.group.groupId = g.groupId " +
            "LEFT JOIN u.workSchedule w " +
            "JOIN RoleEntity r ON u.role = r " +
            "JOIN UserLocationEntity ul ON ul.user.userId= u.userId " +
            "JOIN LocationEntity l ON ul.location.locationId = l.locationId " +
            "LEFT JOIN SecondaryDetailsEntity sd ON sd.user.userId = u.userId " +
            "WHERE u.organizationId = :orgId AND u.active = false AND r.hierarchyLevel > :hierarchyLevel")
    List<UserResponse> findAllInActiveUsers(@Param("orgId") String orgId, @Param("hierarchyLevel") int hierarchyLevel);

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
    Page<TimesheetProjection> findUsersByUserIds(@Param("userIds") List<String> arrayOfUserIds, Pageable pageable);
}
