package com.uniq.tms.tms_microservice.modules.payrollManagement.repository;

import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.PayRollHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayRollHistoryRepository extends JpaRepository<PayRollHistoryEntity,Integer> {
}
