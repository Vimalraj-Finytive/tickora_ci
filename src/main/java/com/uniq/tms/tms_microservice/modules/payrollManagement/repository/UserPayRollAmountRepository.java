package com.uniq.tms.tms_microservice.modules.payrollManagement.repository;

import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.UserPayRollAmountEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

@Repository
public interface UserPayRollAmountRepository extends JpaRepository<UserPayRollAmountEntity,Integer> {

    Optional<UserPayRollAmountEntity> findByUser_UserId(String userId);


    @Query(value = """
    SELECT *
    FROM user_payroll_amount upa
    WHERE upa.payroll_id = :payrollId
      AND LOWER(upa.month) = LOWER(:month)
    """,
            nativeQuery = true)
    List<UserPayRollAmountEntity> findAllByPayrollIdAndMonth(
            @Param("payrollId") String payrollId,
            @Param("month") String month
    );

    @Query(value = """
    SELECT *
    FROM user_payroll_amount upa
    WHERE LOWER(upa.month) = LOWER(:month)
    """,
            nativeQuery = true)
    List<UserPayRollAmountEntity> findAllByMonth(
            @Param("month") String month
    );

    Optional<UserPayRollAmountEntity> findByUser_UserIdAndMonth(String userId, String month);

}
