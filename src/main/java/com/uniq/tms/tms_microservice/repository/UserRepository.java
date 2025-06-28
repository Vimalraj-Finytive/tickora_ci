package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.entity.UserGroupEntity;
import com.uniq.tms.tms_microservice.model.UserResponse;
import com.uniq.tms.tms_microservice.dto.UserNameSuggestionDto;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

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
            @Param("organizationId") Long organizationId,
            @Param("userId") Long userId
    );

    UserEntity findByEmail(String email);

    UserEntity findByMobileNumber(String mobile);

    @Query("SELECT new com.uniq.tms.tms_microservice.model.UserResponse(" +
            "u.userId, u.userName, u.email, u.mobileNumber, " +
            "COALESCE(g.groupName, '-'), r.name, l.name, u.dateOfJoining) " +
            "FROM UserEntity u " +
            "LEFT JOIN UserGroupEntity ug ON ug.user.userId = u.userId " +
            "LEFT JOIN GroupEntity g ON ug.group.groupId = g.groupId " +
            "JOIN RoleEntity r ON u.role = r " +
            "JOIN UserLocationEntity ul ON ul.user.userId= u.userId " +
            "JOIN LocationEntity l ON ul.location.locationId = l.locationId " +
            "WHERE u.organizationId = :orgId AND u.active = true AND r.hierarchyLevel > :hierarchyLevel")
    List<UserResponse> findAllUsers(@Param("orgId") Long orgId, @Param("hierarchyLevel") int hierarchyLevel);

    @Query("SELECT new com.uniq.tms.tms_microservice.dto.UserNameSuggestionDto(u.userId, u.userName) " +
            "FROM UserEntity u WHERE LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<UserNameSuggestionDto> searchUserNamesContaining(@Param("keyword") String keyword);

    @Query("SELECT u FROM UserEntity u WHERE u.organizationId = :orgId AND u.active = true AND u.role.roleId = :roleId")
    List<UserEntity> findUsersByOrgIdAndRoleId(@Param("orgId") Long orgId, @Param("roleId") Long roleId);

    @Query("SELECT u FROM UserEntity u WHERE u.organizationId = :orgId AND u.active = true AND u.role.hierarchyLevel IN :higherRoleIds")
    List<UserEntity> findByOrgIdAndRoleId(Long orgId, List<Integer> higherRoleIds);

    @Query("SELECT u FROM UserEntity u WHERE u.userId IN :userIds AND u.organizationId = :orgId AND u.active = true")
    List<UserEntity> findByUserIdAndOrgIdAndActiveTrue(@Param("userIds") List<Long> userIds, @Param("orgId") Long orgId);

    Optional<UserEntity> findByUserId(Long userId);

    Optional<UserEntity> findOptionalByMobileNumber(String mobileNumber);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.active = false WHERE u.userId = :userId AND u.organizationId = :orgId")
    void deactivateUserById(Long userId, Long orgId);

    @Query("SELECT new com.uniq.tms.tms_microservice.dto.UserNameSuggestionDto(u.userId, u.userName) " +
            "FROM UserEntity u " +
            "WHERE u.organizationId = :orgId AND u.active = true" + " AND u.role.hierarchyLevel > :hierarchyLevel")
    List<UserNameSuggestionDto> findAllActiveUsersByOrganization(@Param("orgId") Long orgId, @Param("hierarchyLevel") int hierarchyLevel);

    @Query("SELECT new com.uniq.tms.tms_microservice.dto.UserNameSuggestionDto(u.userId, u.userName) " +
            "FROM UserGroupEntity ug " +
            "JOIN ug.user u " +
            "WHERE ug.group.groupId IN :groupIds AND u.organizationId = :orgId AND u.active = true")
    List<UserNameSuggestionDto> findAllGroupUsersByOrganizationId(@Param("groupIds") List<Long> groupIds,
                                                                  @Param("orgId") Long orgId);

    @Query(value = "SELECT mobile_number FROM users", nativeQuery = true)
    List<String> findAllMobileNumbers();

    @Query(value = "SELECT email FROM users", nativeQuery = true)
    List<String> findAllEmails();

    UserEntity findByOrganizationIdAndUserId(Long orgId, Long userId);

    @Query("SELECT u FROM UserEntity u WHERE u.organizationId = :orgId AND u.active = true AND u.userId <> :userId AND u.role.hierarchyLevel > :hierarchyLevel")
    List<UserEntity> findAllUsersList(@Param("orgId") Long orgId, @Param("userId") Long userIdFromToken, @Param("hierarchyLevel") int hierarchyLevel);

    @Query("SELECT u FROM UserEntity u WHERE u.organizationId = :orgId AND u.active = true AND u.role.name IN :roles")
    List<UserEntity> findUserByRoles(Set<String> roles, Long orgId);

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
                                                 @Param("orgId") Long orgId);
}
