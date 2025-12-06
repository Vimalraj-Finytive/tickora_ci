package com.uniq.tms.tms_microservice.modules.leavemanagement.repository;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.UserPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.AccrualType;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface UserPolicyRepository extends JpaRepository<UserPolicyEntity, Long> {

    @Query("SELECT up.user.userId, up.policy.policyId FROM UserPolicyEntity up WHERE up.user.userId IN :userIds")
    List<Object[]> findUserPolicyMap(@Param("userIds") List<String> userIds);

    @Query("SELECT up FROM UserPolicyEntity up WHERE up.user.userId IN :userIds")
    List<UserPolicyEntity> findByUser_UserIds(List<String> userIds);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserPolicyEntity up " +
            "WHERE up.policy.policyId = :policyId " +
            "AND up.user.userId IN :userIds")
    void deleteByPolicyIdAndUserIds(
            @Param("policyId") String policyId,
            @Param("userIds") Set<String> userIds
    );

    @Query("SELECT up FROM UserPolicyEntity up WHERE up.policy.policyId = :policyId")
    List<UserPolicyEntity> findByPolicyId(String policyId);

    @Query("SELECT up.user.userId FROM UserPolicyEntity up WHERE up.policy.policyId = :policyId")
    List<String> findUserIdsByPolicyId(@Param("policyId") String policyId);

    @Query("""
    SELECT COUNT(up) > 0
    FROM UserPolicyEntity up
    WHERE up.policy.policyId = :policyId
      AND up.user.userId = :userId
      AND up.validFrom <= :startDate
      AND (up.validTo IS NULL OR up.validTo >= :endDate)
    """)
    boolean isUserPolicyActive(String policyId, String userId,  LocalDate startDate, LocalDate endDate);

    @Query("SELECT up FROM UserPolicyEntity up " +
            "WHERE up.user.userId = :userId AND up.policy.policyId = :policyId " +
            "ORDER BY up.validTo DESC NULLS LAST, up.id DESC")
    List<UserPolicyEntity> findAllByUserAndPolicy(
            @Param("userId") String userId,
            @Param("policyId") String policyId
    );

    @Query("""
    SELECT up FROM UserPolicyEntity up
    WHERE up.validTo IN (
        SELECT MAX(up2.validTo)
        FROM UserPolicyEntity up2
        WHERE up2.policy.policyId IN :policyIds
          AND up2.user.userId IN :userIds
        GROUP BY up2.policy.policyId, up2.user.userId
    )
""")
    List<UserPolicyEntity> findAllByPolicyIdsAndUserIds(
            @Param("policyIds") List<String> policyIds,
            @Param("userIds") Set<String> userIds
    );

    @Query("""
    SELECT DISTINCT up.user.userId
    FROM UserPolicyEntity up
    """)
    List<String> findAllUserIdsInUserPolicies();


    List<UserPolicyEntity> findByUser_UserIdAndPolicy_AccrualType(
            String userId,
            AccrualType accrualType
    );



    List<UserPolicyEntity> findByUser_UserId(String userId);


}
