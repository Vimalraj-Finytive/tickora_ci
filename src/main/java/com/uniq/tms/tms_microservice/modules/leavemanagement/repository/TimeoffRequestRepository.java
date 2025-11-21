package com.uniq.tms.tms_microservice.modules.leavemanagement.repository;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeoffRequestEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TimeoffRequestRepository extends JpaRepository<TimeoffRequestEntity,Long> {
    List<TimeoffRequestEntity> findByUserId(String userId);
    List<TimeoffRequestEntity> findByStartDate(LocalDate startDate);
    boolean existsByUserIdAndPolicy_PolicyId(String userId, String policyId);

    @Query("SELECT t FROM TimeoffRequestEntity t " +
            "WHERE t.userId = :userId " +
            "AND t.requestDate = :requestDate")
    TimeoffRequestEntity findByUserIdAndRequestDate(
            @Param("userId") String userId,
            @Param("requestDate") LocalDate requestDate);
}
