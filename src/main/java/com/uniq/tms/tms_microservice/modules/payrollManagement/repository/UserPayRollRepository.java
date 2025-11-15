package com.uniq.tms.tms_microservice.modules.payrollManagement.repository;

import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.UserPayRollEntity;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserPayRollRepository extends JpaRepository<UserPayRollEntity, Long> {

    @Query("SELECT up FROM UserPayRollEntity up WHERE up.user.userId IN :userIds")
    List<UserPayRollEntity> findExistingUserPayrolls(@Param("userIds") List<String> userIds);

    @Query("SELECT up FROM UserPayRollEntity up WHERE up.user.active = true")
    List<UserPayRollEntity> findAllByActiveUsers();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM UserPayRollEntity up WHERE up.payroll.id = :payrollId")
    void deleteByPayrollId(String payrollId);
}
