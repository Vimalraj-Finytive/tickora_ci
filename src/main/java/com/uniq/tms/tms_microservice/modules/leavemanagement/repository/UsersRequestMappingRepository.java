package com.uniq.tms.tms_microservice.modules.leavemanagement.repository;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.UsersRequestMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRequestMappingRepository extends JpaRepository<UsersRequestMappingEntity,Long> {

}
