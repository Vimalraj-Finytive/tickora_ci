package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.OrganizationTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationTypeRepository extends JpaRepository<OrganizationTypeEntity, String> {
    OrganizationTypeEntity findByOrgType(String orgType);
}
