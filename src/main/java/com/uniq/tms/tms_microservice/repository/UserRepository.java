package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.model.UserResponse;
import com.uniq.tms.tms_microservice.dto.UserNameSuggestionDto;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query(value = "SELECT * FROM users u WHERE u.organization_id = :organizationId AND u.user_id = :userId", nativeQuery = true)
    UserEntity findUserByOrganizationIdAndUserId(@Param("organizationId") Long organizationId, @Param("userId") Long userId);

    UserEntity findByEmail(String email);

    UserEntity findUserByEmail(String email);;

    UserEntity findByMobileNumber(String mobile);

    boolean existsByEmail(String email);

    @Query("SELECT new com.uniq.tms.tms_microservice.model.UserResponse(" +
            "u.userId, u.userName, u.email, u.mobileNumber, " +
            "COALESCE(g.groupName, '-'), r.name, l.name, u.dateOfJoining) " +
            "FROM UserEntity u " +
            "LEFT JOIN UserGroupEntity ug ON ug.user.userId = u.userId " +
            "LEFT JOIN GroupEntity g ON ug.group.groupId = g.groupId " +
            "JOIN RoleEntity r ON u.role = r " +
            "JOIN LocationEntity l ON u.locationId = l.locationId " +
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

    boolean existsByMobileNumber(String mobileNumber);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.active = false WHERE u.userId = :userId")
    void deactivateUserById(Long userId);

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

//    @Query("SELECT u.userId FROM UserEntity u WHERE u.userId LIKE CONCAT(:prefix, '%') ORDER BY u.userId DESC")
//    List<String> findLatestUserId(@Param("prefix") String prefix, Pageable pageable);

}
