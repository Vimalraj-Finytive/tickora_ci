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
public interface TimeOffRequestRepository extends JpaRepository<TimeOffRequestEntity, Long> {

    List<TimeOffRequestEntity> findByUser_UserId(String userId);

    List<TimeOffRequestEntity> findByStartDate(LocalDate startDate);

    boolean existsByUser_UserIdAndPolicy_PolicyIdAndRequestDate(String userId, String policyId, LocalDate requestDate);

    @Query("SELECT t FROM TimeOffRequestEntity t " +
            "WHERE t.policy.policyId = :policyId " +
            "AND t.user.userId = :userId " +
            "AND t.requestDate = :requestDate")
    TimeOffRequestEntity findByUser_UserIdAndRequestDate(
            @Param("policyId") String policyId,
            @Param("userId") String userId,
            @Param("requestDate") LocalDate requestDate);

    @Query("""
            SELECT new com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffRequestUserModel(
                r,
                u.userId,
                u.userName,
                m.type
            )
            FROM UsersRequestMappingEntity m
            JOIN TimeOffRequestEntity r ON r.timeOffRequestId = m.timeOffRequestId
            JOIN r.user u
            WHERE m.viewerId = :userId
              AND r.startDate >= :fromDate
              AND r.endDate <= :toDate
            """)
    List<TimeOffRequestUserModel> filterWithUser(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("userId") String userId
    );

    @Query("""
            SELECT new com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffRequestUserModel(
                r,
                u.userId,
                u.userName,
                m.type
            )
            FROM UsersRequestMappingEntity m
            JOIN TimeOffRequestEntity r ON r.timeOffRequestId = m.timeOffRequestId
            JOIN UserEntity u ON u.userId = m.viewerId
            WHERE m.requesterId = :userId
              AND r.startDate >= :fromDate
              AND r.endDate <= :toDate
            """)
    List<TimeOffRequestUserModel> filterCreatedByUser(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("userId") String userId
    );

    @Query("""
            SELECT new com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffRequestUserModel(
                r,
                u.userName
            )
            FROM TimeOffRequestEntity r
            JOIN r.user u
            JOIN u.role role
            WHERE r.startDate <= :toDate
              AND r.endDate >= :fromDate
              AND role.hierarchyLevel > :minRoleLevel
            """)
    List<TimeOffRequestUserModel> filterWithUserAndRole(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("minRoleLevel") int minRoleLevel);

    @Query("SELECT t FROM TimeOffRequestEntity t " +
            "WHERE t.startDate = :startDate AND t.status = Status.APPROVED")
    List<TimeOffRequestEntity> findByStartDateAndStatusApproved(@Param("startDate") LocalDate startDate);
}
