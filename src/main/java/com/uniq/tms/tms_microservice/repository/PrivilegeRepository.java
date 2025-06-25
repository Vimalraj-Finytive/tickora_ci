package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.PrivilegeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrivilegeRepository extends JpaRepository<PrivilegeEntity, Long> {
}
