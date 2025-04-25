package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
    @Query("SELECT l FROM LocationEntity l WHERE l.organizationEntity.organizationId = :orgId")
    List<LocationEntity> findAllLocationsByOrganization(@Param("orgId") Long orgId);
}
