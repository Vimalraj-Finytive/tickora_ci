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
            "(r.hierarchyLevel > :hierarchyLevel)")
    List<RoleEntity> findRolesByOrgIdAndRoleLevel(@Param("orgId") Long orgId, @Param("hierarchyLevel") int hierarchyLevel);
}
