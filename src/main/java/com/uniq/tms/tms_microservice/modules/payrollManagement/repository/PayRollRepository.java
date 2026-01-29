package com.uniq.tms.tms_microservice.modules.payrollManagement.repository;

import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.PayRollEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.uniq.tms.tms_microservice.modules.payrollManagement.projection.PayRollProjection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayRollRepository extends JpaRepository<PayRollEntity,String> {

    @Query("SELECT p.id AS id, p.payrollName AS payrollName FROM PayRollEntity p WHERE p.isActive=true")
    List<PayRollProjection> findAllIdAndName();

    List<PayRollEntity>findByIsActiveTrue();
    @Query("SELECT p FROM PayRollEntity p WHERE p.id = :id AND p.isActive = true")
    Optional<PayRollEntity> findActiveById(@Param("id") String id);

    @Query(" SELECT p FROM PayRollEntity p WHERE p.createdAt <= :date And p.isActive=true")
    List<PayRollEntity> findPayrollsCreatedBeforeOrOn(@Param("date") LocalDateTime date);

    @Query(" SELECT p.id FROM PayRollEntity p WHERE p.createdAt <= :date And p.isActive=true")
    List<String> findPayrollIdsCreatedBeforeOrOn(@Param("date") LocalDateTime date);

}
