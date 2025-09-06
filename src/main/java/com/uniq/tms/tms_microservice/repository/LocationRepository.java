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

    @Query(value = "SELECT name, location_id FROM location WHERE organization_id = :orgId", nativeQuery = true)
    List<Object[]> findLocationNameIdMappings(@Param("orgId") String orgId);

    @Query("SELECT l FROM LocationEntity l WHERE l.organizationEntity.organizationId = :orgId")
    List<LocationEntity> findByOrgId(@Param("orgId") String orgId);

    boolean existsBylocationIdInAndOrganizationEntity_OrganizationId(List<Long> locationIds, String orgId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = """
        DELETE FROM location
        WHERE location_id IN :locationId
        AND organization_id = :orgId
        """, nativeQuery = true)
    void deleteAllLocationById(@Param("locationId") List<Long> locationId, @Param("orgId") String orgId);

    LocationEntity findByLocationIdAndOrganizationEntity_OrganizationId(Long locationId, String orgId);

    @Modifying
    @Transactional
    @Query("UPDATE LocationEntity l SET l.isDefault = false " +
            "WHERE l.organizationEntity.organizationId = :orgId " +
            "AND l.locationId <> :defaultLocationId " +
            "AND l.isDefault = true")
    void resetDefaultLocation(@Param("orgId") String orgId, @Param("defaultLocationId") Long defaultLocationId);

    @Modifying
    @Transactional
    @Query("UPDATE LocationEntity l SET l.isDefault = false " +
            "WHERE l.organizationEntity.organizationId = :orgId AND l.isDefault = true")
    void resetDefaultLocation(@Param("orgId") String orgId);

    @Query("SELECT l FROM LocationEntity l WHERE l.locationId IN :locationIds AND l.organizationEntity.organizationId = :orgId AND l.isDefault = true")
    Optional<LocationEntity> findDefaultLocationByOrgId(List<Long> locationIds, String orgId);

    @Query("SELECT l FROM LocationEntity l WHERE l.organizationEntity.organizationId = :orgId AND l.isDefault = true")
    LocationEntity findDefaultLocationById(@Param("orgId") String orgId);

    List<LocationEntity> findLocationByOrganizationEntity_OrganizationId(String orgId);
}
