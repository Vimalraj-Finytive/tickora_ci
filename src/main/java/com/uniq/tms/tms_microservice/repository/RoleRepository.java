package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    @Query("SELECT r FROM RoleEntity r WHERE r.organizationEntity.organizationId = :orgId AND " +
            "((:role = 'SuperAdmin' AND r.name IN ('Admin', 'Staff', 'Student', 'Manager')) OR " +
            "(:role = 'Admin' AND r.name IN ('Student', 'Staff', 'Manager')))")
    List<RoleEntity> findRolesByOrgIdAndRole(@Param("orgId") Long orgId, @Param("role") String role);
}
