package com.uniq.tms.tms_microservice.modules.leavemanagement.repository;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TimeOffPolicyRepository extends JpaRepository<TimeOffPolicyEntity, String> {

    @Query("SELECT MAX(t.policyId) FROM TimeoffPolicyEntity t WHERE t.policyId LIKE CONCAT(:prefix, '%')")
    String findMaxPolicyId(@Param("prefix") String prefix);

    TimeOffPolicyEntity findByPolicyId(String policyId);

    @Query("""
        SELECT p 
        FROM TimeoffPolicyEntity p 
        WHERE p.policyId IN :policyIds
    """)
    List<TimeOffPolicyEntity> findByPolicyIdIn(@Param("policyIds") List<String> policyIds);

    @Query("SELECT up.policy FROM UserPolicyEntity up WHERE up.user.userId = :userId")
    List<TimeOffPolicyEntity> findPolicyByUserId(@Param("userId") String userId);
}


