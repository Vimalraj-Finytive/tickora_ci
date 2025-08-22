package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.UserSchemaMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSchemaMapperRepository extends JpaRepository<UserSchemaMappingEntity , String> {
    UserSchemaMappingEntity findUserByEmail(String email);

    UserSchemaMappingEntity findUserByMobile(String mobile);
}
