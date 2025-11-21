package com.uniq.tms.tms_microservice.modules.leavemanagement.repository;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeoffPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

public interface TimeoffPolicyRepository extends JpaRepository<TimeoffPolicyEntity, String> {

    @Query("SELECT MAX(t.policyId) FROM TimeoffPolicyEntity t WHERE t.policyId LIKE CONCAT(:prefix, '%')")
    String findMaxPolicyId(@Param("prefix") String prefix);

    TimeoffPolicyEntity findByPolicyId(String policyId);

    @Query("""
        SELECT p 
        FROM TimeoffPolicyEntity p 
        WHERE p.policyId IN :policyIds
    """)
    List<TimeoffPolicyEntity> findByPolicyIdIn(@Param("policyIds") List<String> policyIds);

    @Query("SELECT up.policy FROM UserPolicyEntity up WHERE up.user.userId = :userId")
    List<TimeoffPolicyEntity> findPolicyByUserId(@Param("userId") String userId);
}


