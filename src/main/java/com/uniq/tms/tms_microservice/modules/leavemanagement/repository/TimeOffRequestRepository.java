package com.uniq.tms.tms_microservice.modules.leavemanagement.repository;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffRequestEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.Status;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.AccrualType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.Compensation;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffRequestUserModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.projection.TimeOffExportView;
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

    @Query("""
            SELECT COUNT(r) > 0
            FROM TimeOffRequestEntity r
            WHERE r.user.userId = :userId
            AND r.policy.policyId = :policyId
            AND r.requestDate = :requestDate
            AND r.status IN ('PENDING','APPROVED')
            """)
    boolean existsTimeoffRequest(String userId, String policyId, LocalDate requestDate);

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
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("minRoleLevel") int minRoleLevel);

    @Query("SELECT t FROM TimeOffRequestEntity t " +
            "WHERE t.startDate = :startDate AND t.status = Status.APPROVED")
    List<TimeOffRequestEntity> findByStartDateAndStatusApproved(@Param("startDate") LocalDate startDate);

    @Query(""" 
            SELECT COUNT(r) >0  FROM TimeOffRequestEntity r
            WHERE r.user.userId = :userId
            AND r.policy.policyId = :policyId
            AND r.status NOT IN  ('REJECTED','CANCELLED')
            AND (r.startDate <= :endDate AND r.endDate >= :startDate)
            """)
    boolean existsOverlappingRequest(String userId, String policyId, LocalDate startDate, LocalDate endDate);


    @Query("""
    SELECT r
    FROM TimeOffRequestEntity r
    WHERE MONTH(r.startDate) = :month
      AND YEAR(r.startDate) = :year
      AND r.policy.compensation = :compensation
      AND r.status = status
      AND r.user.active = true""")
    List<TimeOffRequestEntity> findAllUnpaidRequest(
            @Param("month") int month,
            @Param("year") int year,
            @Param("type") Compensation compensation,
            @Param("status") Status status);

    @Query("""
    SELECT r
    FROM TimeOffRequestEntity r
    WHERE MONTH(r.startDate) = :month
      AND YEAR(r.startDate) = :year
      AND r.policy.compensation = :compensation
      AND r.status = :status
      AND r.policy.accrualType = :accrualType
      AND r.user.active = true
    """)
    List<TimeOffRequestEntity> findAllAnnualRequests(
            @Param("month") int month,
            @Param("year") int year,
            @Param("compensation") Compensation compensation,
            @Param("status") Status status,
            @Param("accrual") AccrualType accrualType
    );

    @Query("""
    SELECT r
    FROM TimeOffRequestEntity r
    WHERE
        (
            (MONTH(r.startDate) = :month AND YEAR(r.startDate) = :year)
            OR
            (MONTH(r.endDate) = :month AND YEAR(r.endDate) = :year)
        )
      AND r.status = :status
      AND r.policy.accrualType = :accrualType
      AND r.user.active = true
    """)
    List<TimeOffRequestEntity> findFixedRequests(
            @Param("month") int month,
            @Param("year") int year,
            @Param("status") Status status,
            @Param("accrualType") AccrualType accrualType);

        @Query(value = """
        SELECT *
        FROM timeoff_export_view
        WHERE leave_start_date >= :fromDate
        AND leave_end_date <= :toDate
        AND policy_id IN (:policies)
        AND status IN (:status)
        """, nativeQuery = true)
        List<TimeOffExportView> fetchRequestDate(LocalDate fromDate,
                                          LocalDate toDate,
                                          List<String> policies,
                                          List<String> status);

    @Query(value = """
    SELECT *
    FROM timeoff_request_view
    WHERE creator_id = :userId
      AND leave_start_date >= :fromDate
      AND leave_end_date <= :toDate
      AND (array_length(:policies, 1) IS NULL OR policy_id = ANY(:policies))
      AND (array_length(:status, 1) IS NULL OR status = ANY(:status))
    """,
            nativeQuery = true)
    List<TimeOffExportView> fetchCreatorRequests(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String[] status,
            @Param("policies") String[] policies,
            @Param("userId") String userId
    );

    @Query(value = """
    SELECT *
    FROM timeoff_request_view
    WHERE viewer_id = :viewerId
      AND leave_start_date >= :fromDate
      AND leave_end_date <= :toDate
      AND (array_length(:policies, 1) IS NULL OR policy_id = ANY(:policies))
      AND (array_length(:status, 1) IS NULL OR status = ANY(:status))
    """,
            nativeQuery = true)
    List<TimeOffExportView> fetchReceiverRequests(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String[] status,
            @Param("policies") String[] policies,
            @Param("viewerId") String viewerId
    );

    @Query(value = """
    SELECT *
    FROM timeoff_request_view
    WHERE leave_start_date >= :fromDate
      AND leave_end_date <= :toDate
      AND (array_length(:policies, 1) IS NULL OR policy_id = ANY(:policies))
      AND (array_length(:status, 1) IS NULL OR status = ANY(:status))
    """,
            nativeQuery = true)
    List<TimeOffExportView> fetchAllRequests(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String[] status,
            @Param("policies") String[] policies
    );

    TimeOffRequestEntity findByTimeOffRequestId(Long requestId);
}
