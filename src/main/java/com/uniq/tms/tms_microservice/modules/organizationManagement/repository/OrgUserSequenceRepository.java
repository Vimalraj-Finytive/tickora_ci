package com.uniq.tms.tms_microservice.modules.organizationManagement.repository;

import com.uniq.tms.tms_microservice.modules.identityManagement.entity.OrgUserSequenceEntity;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrgUserSequenceRepository extends JpaRepository<OrgUserSequenceEntity, String> {

    @Modifying
    @Transactional
    @Query("UPDATE OrgUserSequenceEntity s SET s.lastUserId = s.lastUserId + 1 WHERE s.orgId = :orgId")
    int incrementSequence(@Param("orgId") String orgId);

    @Query("SELECT s.lastUserId FROM OrgUserSequenceEntity s WHERE s.orgId = :orgId")
    Integer getLastUserId(@Param("orgId") String orgId);

    @Modifying
    @Transactional
    @Query("UPDATE OrgUserSequenceEntity s SET s.lastSecondaryUserId = s.lastSecondaryUserId + 1 WHERE s.orgId = :orgId")
    int incrementSecondaryUserSequence(@Param("orgId") String orgId);

    @Query("SELECT s.lastSecondaryUserId FROM OrgUserSequenceEntity s WHERE s.orgId = :orgId")
    Integer getLastSecondaryUserId(@Param("orgId") String orgId);

    @Modifying
    @Transactional
    @Query("UPDATE OrgUserSequenceEntity s SET s.lastSubscriptionId = s.lastSubscriptionId + 1 WHERE s.orgId = :orgId")
    int incrementSubscriptionSequence(@Param("orgId") String orgId);

    @Query("SELECT s.lastSubscriptionId FROM OrgUserSequenceEntity s WHERE s.orgId = :orgId")
    Integer getLastSubscription(@Param("orgId") String orgId);

    @Modifying
    @Query("UPDATE OrgUserSequenceEntity s SET s.lastSubscriptionId = :lastId WHERE s.orgId = :orgId")
    void updateLastSubscriptionId(@Param("orgId") String orgId, @Param("lastId") int lastId);

    @Modifying
    @Query("UPDATE OrgUserSequenceEntity s SET s.lastSecondaryUserId = :lastId WHERE s.orgId = :orgId")
    void updateLastSecondaryId(@Param("orgId") String orgId, @Param("lastId") int lastId);

    @Modifying
    @Transactional
    @Query("UPDATE OrgUserSequenceEntity s SET s.lastPaymentId = COALESCE(s.lastPaymentId, 0) + 1 WHERE s.orgId = :orgId")
    int incrementPaymentSequence(@Param("orgId") String orgId);

    @Query("SELECT s.lastPaymentId FROM OrgUserSequenceEntity s WHERE s.orgId = :orgId")
    Integer getLastPaymentId(@Param("orgId") String orgId);

    @Modifying
    @Transactional
    @Query("UPDATE OrgUserSequenceEntity s SET s.lastPaymentId = :value WHERE s.orgId = :orgId")
    void updateLastPaymentId(@Param("orgId") String orgId, @Param("value") int value);

}
