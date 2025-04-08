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
            "u.userId, " +
            "u.userName, u.email, u.mobile_number, " +
            "g.groupName, r.name, l.name, u.dateOfJoining) " +
            "FROM UserEntity u " +
            "JOIN GroupEntity g ON u.groupId = g.groupId " +
            "JOIN RoleEntity r ON u.role = r " +
            "JOIN LocationEntity l ON u.locationId = l.locationId " +
            "WHERE u.organizationId = :orgId AND r.name IN (:role)")
    List<UserResponseDto> findByOrganizationId(@Param("orgId") Long orgId, @Param("role") List<String> accessibleRoles);

    List<UserEntity> findAllByOrganizationIdAndRole_Name(Long organizationId, String roleName);

    List<UserEntity> findAllByOrganizationIdAndRole_NameNot(Long orgId, String excludedRole);

}
