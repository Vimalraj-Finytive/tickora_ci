package com.uniq.tms.tms_microservice.modules.leavemanagement.repository;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalanceEntity, Long> {

    LeaveBalanceEntity findByPolicy_PolicyIdAndUserId(String policyId,String userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM LeaveBalanceEntity lb WHERE lb.policy.policyId = :policyId")
    void deleteByPolicyId(String policyId);

    Optional<LeaveBalanceEntity> findTopByUserIdOrderByLeaveBalanceIdDesc(String userId);

    @Query("""
        SELECT lb FROM LeaveBalanceEntity lb
        WHERE lb.policy.policyId = :policyId
    """)
    List<LeaveBalanceEntity> findByPolicyId(@Param("policyId") String policyId);
}
