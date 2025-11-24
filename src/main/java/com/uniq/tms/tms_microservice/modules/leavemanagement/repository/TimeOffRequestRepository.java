package com.uniq.tms.tms_microservice.modules.leavemanagement.repository;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffRequestEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffRequestUserModel;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimeOffRequestRepository extends JpaRepository<TimeOffRequestEntity,Long> {

    List<TimeOffRequestEntity> findByUserId(String userId);
    List<TimeOffRequestEntity> findByStartDate(LocalDate startDate);
    boolean existsByUserIdAndPolicy_PolicyId(String userId, String policyId);

    @Query("SELECT t FROM TimeOffRequestEntity t " +
            "WHERE t.userId = :userId " +
            "AND t.requestDate = :requestDate")
    TimeOffRequestEntity findByUserIdAndRequestDate(
            @Param("userId") String userId,
            @Param("requestDate") LocalDate requestDate);

    @Query("""
SELECT new com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffRequestUserModel(
    r,
    u.userName
)
FROM TimeOffRequestEntity r
JOIN UserEntity u ON u.userId = r.userId
WHERE r.startDate <= :toDate
  AND r.endDate   >= :fromDate
""")
    List<TimeOffRequestUserModel> filterWithUser(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

}
