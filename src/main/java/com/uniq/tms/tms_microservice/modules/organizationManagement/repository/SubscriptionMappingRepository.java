package com.uniq.tms.tms_microservice.modules.organizationManagement.repository;

import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.SubscriptionMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionMappingRepository extends JpaRepository<SubscriptionMappingEntity, String> {

    List<SubscriptionMappingEntity> findBySubscriptionId(String subscriptionId);

}
