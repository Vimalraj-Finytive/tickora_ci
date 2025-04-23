package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.dto.UserResponseDto;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByEmail(String email);

    UserEntity findUserByEmail(String email);;

    boolean existsByEmail(String email);

    @Query("SELECT new com.uniq.tms.tms_microservice.dto.UserResponseDto( " +
            "u.userId, u.userName, u.email, u.mobileNumber, " +
            "COALESCE(g.groupName, '-'), u.organizationId, r.name, u.dateOfJoining, l.name) " +
            "FROM UserEntity u " +
            "LEFT JOIN UserGroupEntity ug ON ug.user.userId = u.userId " +
            "LEFT JOIN GroupEntity g ON ug.group.groupId = g.groupId " +
            "JOIN RoleEntity r ON u.role = r " +
            "JOIN LocationEntity l ON u.locationId = l.locationId " +
            "WHERE u.organizationId = :orgId AND r.hierarchyLevel > :hierarchyLevel")
    List<UserResponseDto> findByOrganizationIdAndHierarchyLevel(@Param("orgId") Long orgId, @Param("hierarchyLevel") int hierarchyLevel);

    List<UserEntity> findAllByOrganizationIdAndRole_RoleId(Long organizationId, Long roleId);

    List<UserEntity> findAllByOrganizationIdAndRole_RoleIdIn(Long organizationId, List<Integer> higherRoleIds);

    List<UserEntity> findByUserIdInAndOrganizationId(List <Long> userIds, Long orgId);

    boolean existsByMobileNumber(String mobileNumber);
}
