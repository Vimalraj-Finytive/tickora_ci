package com.uniq.tms.tms_microservice.modules.leavemanagement.repository;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.AccrualType;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Set;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalanceEntity, Long> {

    LeaveBalanceEntity findByPolicy_PolicyIdAndUser_UserId(String policyId, String userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM LeaveBalanceEntity lb " +
            "WHERE lb.policy.policyId = :policyId " +
            "AND lb.user.userId IN :userIds")
    void deleteByPolicyIdAndUserIds(
            @Param("policyId") String policyId,
            @Param("userIds") Set<String> userIds
    );

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

    @Query("""
       SELECT lb FROM LeaveBalanceEntity lb
       WHERE lb.policy.policyId = :policyId
         AND lb.user.userId = :userId
         AND lb.periodStartDate <= :start
         AND lb.periodEnd >= :end
       """)
    LeaveBalanceEntity findForPeriod(String policyId, String userId, LocalDate start, LocalDate end);

    @Query("""
           SELECT lb FROM LeaveBalanceEntity lb
           WHERE lb.policy.policyId = :policyId
             AND lb.user.userId = :userId
             AND :date BETWEEN lb.periodStartDate AND lb.periodEnd
           """)
    LeaveBalanceEntity findForMonth(String policyId, String userId, LocalDate date);

    @Query("""
           SELECT lb FROM LeaveBalanceEntity lb
           WHERE lb.policy.policyId = :policyId
             AND lb.user.userId = :userId
             AND EXTRACT(YEAR FROM lb.periodStartDate) = :year
           """)
    LeaveBalanceEntity findForYear(String policyId, String userId, int year);

    @Query("""
    SELECT lb
    FROM LeaveBalanceEntity lb
    WHERE EXTRACT(MONTH FROM lb.periodStartDate) = :month
    AND EXTRACT(YEAR FROM lb.periodStartDate) = :year
    AND lb.policy.accrualType = :accrualType
    AND lb.active = true
    """)
    List<LeaveBalanceEntity> findBalancesByMonthYearAndAccrualType(
            @Param("month") int month,
            @Param("year") int year,
            @Param("accrualType") AccrualType accrualType);

    @Query("""
    SELECT lb
    FROM LeaveBalanceEntity lb
    WHERE EXTRACT(YEAR FROM lb.periodStartDate) = :year
    AND lb.policy.accrualType = :accrualType
    AND lb.active = true
    """)
    List<LeaveBalanceEntity> findBalancesByYearAndAccrualType(
            @Param("year") int year,
            @Param("accrualType") AccrualType accrualType);

    @Query("""
    SELECT lb
    FROM LeaveBalanceEntity lb
            WHERE lb.policy.accrualType = :type
            AND MONTH(lb.periodStartDate) <= :month
            AND MONTH(lb.periodEnd) >= :month
            AND YEAR(lb.periodStartDate) <= :year
            AND YEAR(lb.periodEnd) >= :year
            AND lb.active = true
    """)
    List<LeaveBalanceEntity> findAllFixedAccrual(
            @Param("month") int month,
            @Param("year") int year,
            @Param("type") AccrualType type);

    @Query("""
SELECT lb FROM LeaveBalanceEntity lb
WHERE lb.user.userId = :userId
  AND lb.policy.accrualType = 'MONTHLY'
  AND FUNCTION('DATE_TRUNC', 'month', lb.periodStartDate) =
      FUNCTION('DATE_TRUNC', 'month', CAST(:date AS timestamp))
ORDER BY lb.periodStartDate DESC
""")
    List<LeaveBalanceEntity> findMonthlyBalances(
            @Param("userId") String userId,
            @Param("date") LocalDate date
    );




    @Query("""
SELECT lb FROM LeaveBalanceEntity lb
WHERE lb.user.userId = :userId
  AND lb.policy.accrualType = 'ANNUALLY'
  AND :date BETWEEN lb.periodStartDate AND lb.periodEnd
ORDER BY lb.periodStartDate DESC
""")
    List<LeaveBalanceEntity> findAnnualBalances(
            @Param("userId") String userId,
            @Param("date") LocalDate date
    );


    @Query("""
    SELECT lb FROM LeaveBalanceEntity lb
    WHERE lb.user.userId = :userId
      AND lb.policy.policyId = :policyId
      AND lb.active = true
""")
    LeaveBalanceEntity findByUserIdAndPolicyId(@Param("userId") String userId,
                                               @Param("policyId") String policyId);

    @Query("""
      SELECT lb FROM LeaveBalanceEntity lb
      WHERE lb.user.userId = :userId
      AND lb.policy.policyId = :policyId
      AND lb.active = true
     """)
    LeaveBalanceEntity findActiveBalanceByUserIdAndPolicy(
            @Param("userId") String userId,
            @Param("policyId") String policyId
    );

}
