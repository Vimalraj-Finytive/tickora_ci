package com.uniq.tms.tms_microservice.modules.userManagement.repository;

import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserSchemaMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserSchemaMapperRepository extends JpaRepository<UserSchemaMappingEntity , String> {
    UserSchemaMappingEntity findUserByEmail(String email);

    UserSchemaMappingEntity findUserByMobile(String mobile);

    @Query("SELECT u FROM UserSchemaMappingEntity u WHERE u.mobile= :mobile AND u.orgId = :orgId")
    Optional<UserSchemaMappingEntity> findUserByMobileAndOrgId(@Param("mobile") String mobile, @Param("orgId") String orgId);

    @Query(
            "SELECT DISTINCT LOWER(u.email) " +
                    "FROM UserSchemaMappingEntity u " +
                    "WHERE u.orgId = :orgId AND u.email IS NOT NULL"
    )
    Set<String> findAllMappedEmailsByOrgId(@Param("orgId") String orgId);

    @Query(
            "SELECT DISTINCT u.mobile " +
                    "FROM UserSchemaMappingEntity u " +
                    "WHERE u.orgId = :orgId AND u.mobile IS NOT NULL"
    )
    Set<String> findAllMappedMobilesByOrgId(@Param("orgId") String orgId);

}
