package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.LocationEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    boolean existsBylocationIdInAndOrganizationEntity_OrganizationId(List<Long> locationIds, Long orgId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = """
        DELETE FROM location
        WHERE location_id IN :locationId
        AND organization_id = :orgId
        """, nativeQuery = true)
    void deleteAllLocationById(@Param("locationId") List<Long> locationId, @Param("orgId") Long orgId);

    LocationEntity findByLocationIdAndOrganizationEntity_OrganizationId(Long locationId, Long orgId);

    @Modifying
    @Transactional
    @Query("UPDATE LocationEntity l SET l.isDefault = false " +
            "WHERE l.organizationEntity.organizationId = :orgId " +
            "AND l.locationId <> :defaultLocationId " +
            "AND l.isDefault = true")
    void resetDefaultLocation(@Param("orgId") Long orgId, @Param("defaultLocationId") Long defaultLocationId);

    @Modifying
    @Transactional
    @Query("UPDATE LocationEntity l SET l.isDefault = false " +
            "WHERE l.organizationEntity.organizationId = :orgId AND l.isDefault = true")
    void resetDefaultLocation(@Param("orgId") Long orgId);

    @Query("SELECT l FROM LocationEntity l WHERE l.locationId IN :locationIds AND l.organizationEntity.organizationId = :orgId AND l.isDefault = true")
    Optional<LocationEntity> findDefaultLocationByOrgId(List<Long> locationIds, Long orgId);

    @Query("SELECT l FROM LocationEntity l WHERE l.organizationEntity.organizationId = :orgId AND l.isDefault = true")
    LocationEntity findDefaultLocationById(@Param("orgId") Long orgId);
}
