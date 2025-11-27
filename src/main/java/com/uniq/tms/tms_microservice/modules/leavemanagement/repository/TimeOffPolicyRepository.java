package com.uniq.tms.tms_microservice.modules.leavemanagement.repository;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.record.UserPolicyProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimeOffPolicyRepository extends JpaRepository<TimeOffPolicyEntity, String> {

    @Query("SELECT MAX(t.policyId) FROM TimeOffPolicyEntity t WHERE t.policyId LIKE CONCAT(:prefix, '%')")
    String findMaxPolicyId(@Param("prefix") String prefix);

    TimeOffPolicyEntity findByPolicyId(String policyId);

    @Query("""
            SELECT p FROM TimeOffPolicyEntity p WHERE p.policyId IN :policyIds""")
    List<TimeOffPolicyEntity> findByPolicyIdIn(@Param("policyIds") List<String> policyIds);

    @Query("SELECT up.policy FROM UserPolicyEntity up WHERE up.user.userId = :userId AND up.policy.isActive = true")
    List<TimeOffPolicyEntity> findPolicyByUserId(@Param("userId") String userId);

    @Query("""
    SELECT new com.uniq.tms.tms_microservice.modules.leavemanagement.record.UserPolicyProjection(
       new com.uniq.tms.tms_microservice.modules.leavemanagement.record.UserPolicyKey(
            up.user.userId,
            up.policy.policyId
        ),
        up.validTo
    )
    FROM UserPolicyEntity up
    """)
    List<UserPolicyProjection> findUserPolicyValidTo();

    @Query(
            value = "SELECT * FROM timeoff_policies WHERE is_active IS TRUE",
            nativeQuery = true
    )
    List<TimeOffPolicyEntity> findByIsActiveTrue();

    @Query("""
    SELECT COUNT(p) > 0
    FROM TimeOffPolicyEntity p
    WHERE p.policyId = :policyId
      AND p.isActive = true
      AND p.validityStartDate <= :date
      AND (p.validityEndDate IS NULL OR :date <= p.validityEndDate)
    """)
    boolean existsValidPolicy(String policyId, LocalDate date);

}


