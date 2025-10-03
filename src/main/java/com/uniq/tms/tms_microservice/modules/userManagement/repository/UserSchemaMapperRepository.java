package com.uniq.tms.tms_microservice.modules.userManagement.repository;

import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserSchemaMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserSchemaMapperRepository extends JpaRepository<UserSchemaMappingEntity , String> {
    UserSchemaMappingEntity findUserByEmail(String email);

    UserSchemaMappingEntity findUserByMobile(String mobile);

    @Query("SELECT u FROM UserSchemaMappingEntity u WHERE u.mobile= :mobile AND u.orgId = :orgId")
    Optional<UserSchemaMappingEntity> findUserByMobileAndOrgId(@Param("mobile") String mobile, @Param("orgId") String orgId);
}
