package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity,String> {
}
