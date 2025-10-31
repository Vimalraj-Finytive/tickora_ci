package com.uniq.tms.tms_microservice.modules.organizationManagement.repository;

import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, String> {

    Optional<OrganizationEntity> findByOrganizationId(String orgId);

    @Query("SELECT o.organizationId FROM OrganizationEntity o")
    List<String> findAllOrgIds();

    OrganizationEntity findByOrgName(String organization);

    @Query(value = "SELECT nextval('org_id_seq')", nativeQuery = true)
    Long findNextOrganizationId();

    @Query("SELECT o.orgType FROM OrganizationEntity o WHERE o.organizationId = :orgId")
    String findOrgTypeByOrganizationId(String orgId);

    boolean existsByOrganizationIdStartingWith(String s);

    @Query("SELECT o FROM OrganizationEntity o WHERE o.organizationId = :orgId")
    Optional<OrganizationEntity> findSummaryById(String orgId);

    @Query("SELECT o.orgName FROM OrganizationEntity o WHERE o.organizationId = :orgId")
    String findOrgNameByOrganizationId(String orgId);

    @Query(
            value = "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = :schemaName AND table_name = :tableName)",
            nativeQuery = true
    )

    boolean tableExists( String schemaName,  String tableName);

    @Query("SELECT COUNT(o) FROM OrganizationEntity o WHERE o.createdAt BETWEEN :start AND :end")
    long countOrganizationsBetweenDates(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

    @Query("SELECT o.orgType, COUNT(o) FROM OrganizationEntity o GROUP BY o.orgType")
    List<Object[]> countOrganizationsByType();

    @Query("SELECT o.orgType, COUNT(o) FROM OrganizationEntity o " +
            "WHERE o.createdAt BETWEEN :from AND :to " +
            "GROUP BY o.orgType")
    List<Object[]> countOrganizationsByTypeBetweenDates(@Param("from") LocalDateTime from,
                                                        @Param("to") LocalDateTime to);
}
