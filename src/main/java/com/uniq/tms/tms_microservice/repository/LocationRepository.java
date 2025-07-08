package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, Long> {

    // Custom query to fetch location name and ID mapping
    @Query(value = "SELECT name, location_id FROM location", nativeQuery = true)
    List<Object[]> findLocationNameIdMappings();

    @Query("SELECT l FROM LocationEntity l WHERE l.name = :name AND l.organizationEntity.organizationId = :orgId")
    Optional<LocationEntity> findByNameAndOrganizationId(String name, Long orgId);

    @Query("SELECT l FROM LocationEntity l WHERE l.organizationEntity.organizationId = :orgId")
    List<LocationEntity> findByOrgId(@Param("orgId") Long orgId);
}
