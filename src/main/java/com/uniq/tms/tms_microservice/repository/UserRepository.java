package com.uniq.tms.tms_microservice.repository;


import com.uniq.tms.tms_microservice.dto.UserResponseDto;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByEmail(String email);

    UserEntity findUserByEmail(String email);;

    boolean existsByEmail(String email);

    @Query("SELECT u.userId, u.userName, u.email, u.mobileNumber, " +
            "COALESCE(g.groupName, '-'), u.organizationId, r.name, u.dateOfJoining, l.name " +
            "FROM UserEntity u " +
            "LEFT JOIN UserGroupEntity ug ON ug.user.userId = u.userId " +
            "LEFT JOIN GroupEntity g ON ug.group.groupId = g.groupId " +
            "JOIN RoleEntity r ON u.role = r " +
            "JOIN LocationEntity l ON u.locationId = l.locationId " +
            "WHERE u.organizationId = :orgId AND r.name IN (:role) AND u.active = true")
    List<Object[]> findRawUsersWithGroups(@Param("orgId") Long orgId, @Param("role") List<String> accessibleRoles);



//    @Query("SELECT new com.uniq.tms.tms_microservice.dto.UserResponseDto( " +
//            "u.userId, u.userName, u.email, u.mobileNumber, " +
//            "COALESCE(g.groupName, '-'), u.organizationId, r.name, u.dateOfJoining, l.name) " +
//            "FROM UserEntity u " +
//            "LEFT JOIN UserGroupEntity ug ON ug.user.userId = u.userId " +
//            "LEFT JOIN GroupEntity g ON ug.group.groupId = g.groupId " +
//            "JOIN RoleEntity r ON u.role = r " +
//            "JOIN LocationEntity l ON u.locationId = l.locationId " +
//            "WHERE u.organizationId = :orgId AND r.name IN (:role)")
//    List<UserResponseDto> findByOrganizationId(@Param("orgId") Long orgId, @Param("role") List<String> accessibleRoles);

    @Query("SELECT u FROM UserEntity u WHERE u.organizationId = :orgId AND u.role.name = :role AND u.active = true")
    List<UserEntity> findByOrganizationIdAndRole_NameAndActiveTrue(@Param("orgId") Long orgId,@Param("role") String roleName);

    @Query("SELECT u FROM UserEntity u WHERE u.organizationId = :orgId AND u.role.name <> :excludedRole AND u.active = true")
    List<UserEntity> findByOrganizationIdAndActiveTrueAndRole_NameNot(@Param("orgId") Long orgId,@Param("excludedRole") String excludedRole);


    List<UserEntity> findByUserIdInAndOrganizationId(List <Long> userIds, Long orgId);

    Optional<UserEntity> findByUserIdAndActiveTrue(Long userId);
    boolean existsByMobileNumber(String mobileNumber);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.active = false WHERE u.userId = :userId")
    void deactivateUserById(Long userId);


}
