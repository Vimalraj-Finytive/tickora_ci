package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long> {

    Optional<OrganizationEntity> findByOrganizationId(Long orgId);

    @Query("SELECT o.organizationId FROM OrganizationEntity o")
    List<Long> findAllOrgIds();
}
