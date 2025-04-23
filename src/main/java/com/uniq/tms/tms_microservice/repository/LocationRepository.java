package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
    List<LocationEntity> findByOrganizationEntity_OrganizationId(Long orgId);
}
