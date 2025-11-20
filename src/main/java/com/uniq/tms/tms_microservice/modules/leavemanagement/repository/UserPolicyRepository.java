package com.uniq.tms.tms_microservice.modules.leavemanagement.repository;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeoffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.UserPolicyEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserPolicyRepository extends JpaRepository<UserPolicyEntity,Long> {
    @Query("SELECT up.user.userId FROM UserPolicyEntity up WHERE up.policy.policyId = :policyId")
    List<String> findUserIdsByPolicyId(@Param("policyId") String policyId);

    @Query("SELECT up.policy FROM UserPolicyEntity up WHERE up.user.userId = :userId")
    List<TimeoffPolicyEntity> findPolicyByUserId(@Param("userId") String userId);
}
