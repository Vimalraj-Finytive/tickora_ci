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
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalanceEntity, Long> {

    LeaveBalanceEntity findByPolicy_PolicyIdAndUser_UserId(String policyId,String userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM LeaveBalanceEntity lb WHERE lb.policy.policyId = :policyId")
    void deleteByPolicyId(String policyId);

   Optional<LeaveBalanceEntity> findTopByUser_UserIdOrderByLeaveBalanceIdDesc(String userId);

    @Query("""
        SELECT lb FROM LeaveBalanceEntity lb
        WHERE lb.policy.policyId = :policyId
    """)
    List<LeaveBalanceEntity> findByPolicyId(@Param("policyId") String policyId);

    @Query("SELECT lb FROM LeaveBalanceEntity lb JOIN FETCH lb.policy WHERE lb.user.userId = :userId")
    List<LeaveBalanceEntity> findLeaveBalanceByUserId(@Param("userId") String userId);




    @Query("SELECT lb FROM LeaveBalanceEntity lb " +
            "JOIN FETCH lb.policy p " +
            "JOIN FETCH lb.user u " +
            "WHERE u.userId IN :userIds")
    List<LeaveBalanceEntity> findLeaveBalanceByUserIds(@Param("userIds") List<String> userIds);
}
