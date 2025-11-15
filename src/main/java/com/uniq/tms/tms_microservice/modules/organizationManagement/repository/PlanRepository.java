package com.uniq.tms.tms_microservice.modules.organizationManagement.repository;

import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.PlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<PlanEntity, String> {

    @Query("SELECT p.planId FROM PlanEntity p WHERE p.isDefault = true")
    String findByIsDefault();

    @Query("SELECT p FROM PlanEntity p WHERE p.isDefault = false ORDER BY p.createdAt ASC")
    List<PlanEntity> findAllPlans();

    Optional<PlanEntity> findByPlanId(String planId);

    @Query("SELECT e.planId FROM PlanEntity e")
    List<String> getAllPlanIds();

}
