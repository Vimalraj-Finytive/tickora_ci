package com.uniq.tms.tms_microservice.modules.leavemanagement.repository;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.UserPolicyEntity;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface UserPolicyRepository extends JpaRepository<UserPolicyEntity, Long> {

    @Query("SELECT up.user.userId, up.policy.policyId FROM UserPolicyEntity up WHERE up.user.userId IN :userIds")
    List<Object[]> findUserPolicyMap(@Param("userIds") List<String> userIds);

    @Query("SELECT up FROM UserPolicyEntity up WHERE up.user.userId IN :userIds")
    List<UserPolicyEntity> findByUserIds(List<String> userIds);

    @Transactional
    @Modifying
    @Query("DELETE FROM UserPolicyEntity up WHERE up.policy.policyId = :policyId")
    void deleteByPolicyId(String policyId);

    @Query("SELECT up FROM UserPolicyEntity up WHERE up.policy.policyId = :policyId")
    List<UserPolicyEntity> findByPolicyId(String policyId);

    @Query("SELECT up.user.userId FROM UserPolicyEntity up WHERE up.policy.policyId = :policyId")
    List<String> findUserIdsByPolicyId(@Param("policyId") String policyId);



}
