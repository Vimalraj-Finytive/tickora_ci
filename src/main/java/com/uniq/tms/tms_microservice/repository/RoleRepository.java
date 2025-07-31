package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.RoleEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    @Query("SELECT r FROM RoleEntity r WHERE r.hierarchyLevel > :hierarchyLevel ")
    List<RoleEntity> findRolesByOrgIdAndRoleLevel(@Param("hierarchyLevel") int hierarchyLevel);

    @Query("SELECT r FROM RoleEntity r JOIN FETCH r.privilegeEntities")
    List<RoleEntity> findAllWithPrivileges();

    // Custom query to fetch role name and ID mapping
    @Query(value = "SELECT name, role_id FROM role", nativeQuery = true)
    List<Object[]> findRoleNameIdMappings();

    @Query("SELECT r FROM RoleEntity r JOIN FETCH r.privilegeEntities WHERE r.id = :id")
    RoleEntity findByIdWithPrivileges(Long id);

    @Query("SELECT u FROM UserEntity u " +
            "WHERE u.role.roleId IN :roleIds AND u.organizationId = :orgId")
    List<UserEntity> findByIdIn(@Param("roleIds") List<Long> roleIds,
                                   @Param("orgId") String orgId);

    @Query("SELECT r FROM RoleEntity r JOIN FETCH r.privilegeEntities WHERE LOWER(r.name) = LOWER(:name)")
    Optional<RoleEntity> findByNameWithPrivileges(@Param("name") String name);
}
