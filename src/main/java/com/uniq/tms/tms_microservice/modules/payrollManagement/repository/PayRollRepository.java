package com.uniq.tms.tms_microservice.modules.payrollManagement.repository;

import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.PayRollEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.UserPayRollAmountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.uniq.tms.tms_microservice.modules.payrollManagement.projection.PayRollProjection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayRollRepository extends JpaRepository<PayRollEntity,String> {

    @Query("SELECT p.id AS id, p.payrollName AS payrollName FROM PayRollEntity p")
    List<PayRollProjection> findAllIdAndName();


}
