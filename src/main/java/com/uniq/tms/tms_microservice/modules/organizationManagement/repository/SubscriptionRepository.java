package com.uniq.tms.tms_microservice.modules.organizationManagement.repository;

import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.SubscriptionEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.*;
import java.util.*;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, String> {
    @Query(value = "SELECT subscribed_users FROM subscription " +
            "WHERE organization_id = :orgId AND status IN ('Active','Expired') " +
            "ORDER BY CASE WHEN status = 'Active' THEN 1 ELSE 2 END, end_date DESC " +
            "LIMIT 1", nativeQuery = true)
    Optional<Long> getSubscribedUsersByOrgId(@Param("orgId") String orgId);

    @Query(value = "SELECT * FROM subscription " +
            "WHERE organization_id = :orgId AND LOWER(status) IN ('active','expired') " +
            "ORDER BY CASE WHEN LOWER(status) = 'active' THEN 1 ELSE 2 END, end_date DESC " +
            "LIMIT 1", nativeQuery = true)
    Optional<SubscriptionEntity> findActiveSubscription(@Param("orgId") String orgId);


    List<SubscriptionEntity> findAllSubscriptionsByOrgId(@Param("orgId") String orgId);

    @Query("SELECT s.subscribedUsers FROM SubscriptionEntity s WHERE s.orgId = :orgId AND s.status = 'Active'")
    Long findSubscriptionIdByOrgId(@Param("orgId") String orgId);

    @Query("SELECT s FROM SubscriptionEntity s WHERE s.orgId = :orgId AND s.status = 'Active'")
    Optional<SubscriptionEntity> findActiveSubscriptionByOrgId(@Param("orgId") String orgId);

    Optional<SubscriptionEntity> findById(String subscriptionId);

//    List<SubscriptionEntity> findByOrgIdAndStartDateBetween(String orgId, LocalDateTime start, LocalDateTime end);

//    List<SubscriptionEntity> findByStartDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM SubscriptionEntity s WHERE s.startDate BETWEEN :start AND :end")
    List<SubscriptionEntity> findAllByStartDateBetween(@Param("start") LocalDateTime start,
                                                       @Param("end") LocalDateTime end);

    List<SubscriptionEntity> findByStartDateBetween(LocalDateTime start, LocalDateTime end);


}
