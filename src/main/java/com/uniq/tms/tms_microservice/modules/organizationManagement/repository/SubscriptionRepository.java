package com.uniq.tms.tms_microservice.modules.organizationManagement.repository;

import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.SubscriptionEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity,String> {
    @Query("SELECT s.subscribedUsers FROM SubscriptionEntity s WHERE s.orgId = :orgId AND s.status = 'Active'")
    Integer getSubscribedUsersByOrgId(@Param("orgId") String orgId);

}
