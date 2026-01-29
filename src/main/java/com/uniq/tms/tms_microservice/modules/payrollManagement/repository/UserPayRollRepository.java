package com.uniq.tms.tms_microservice.modules.payrollManagement.repository;

import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.UserPayRollEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.projection.UserPayRollAmount;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserPayRollRepository extends JpaRepository<UserPayRollEntity, Long> {

    @Query("SELECT up FROM UserPayRollEntity up WHERE up.user.userId IN :userIds")
    List<UserPayRollEntity> findExistingUserPayrolls(@Param("userIds") List<String> userIds);

    @Query("""
    SELECT up
    FROM UserPayRollEntity up
    WHERE up.user.active = true
      AND up.user.userId IN :userIds
    """)
    List<UserPayRollEntity> findAllByActiveUsers(
            @Param("userIds") List<String> userIds
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM UserPayRollEntity up WHERE up.payroll.id = :payrollId")
    void deleteByPayrollId(String payrollId);

    @Query(value = """
    SELECT *
    FROM payroll_amount_view
    WHERE LOWER(month) = LOWER(:month)
    """, nativeQuery = true)
    List<UserPayRollAmount> findAllByMonth(@Param("month") String month);

    @Query("""
        SELECT upr.user
        FROM UserPayRollEntity upr
        WHERE upr.payroll.id = :payrollId
        AND upr.user.dateOfJoining <= :date
        AND FUNCTION('DATE', upr.createdAt) <= :date
        AND upr.user.active = true
    """)
    List<UserEntity> findUsersByPayrollId(@Param("payrollId") String payrollId, @Param("date") LocalDate date);

    @Query("""
        SELECT upr.user.userId
        FROM UserPayRollEntity upr
        WHERE upr.user.dateOfJoining <= :date
        AND upr.payroll.id IN (:payrollIds)
    """)
    List<String> findAllUsersByMonth(@Param("date") LocalDate date, @Param("payrollIds") List<String> payrollIds);

    @Query("""
        SELECT upr.user
        FROM UserPayRollEntity upr
        WHERE upr.user.dateOfJoining <= :date
        AND upr.user.active = true
        AND upr.payroll.id IN (:payrollIds)
    """)
    List<UserEntity> findAllUsersPayroll(@Param("date") LocalDate date, @Param("payrollIds") List<String> payrollIds);
}
