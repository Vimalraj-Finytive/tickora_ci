package com.uniq.tms.tms_microservice.modules.payrollManagement.repository;

import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.PayRollSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PayRollSettingRepository extends JpaRepository<PayRollSettingEntity,Long> {

    Optional<PayRollSettingEntity> findFirstBy();
}
