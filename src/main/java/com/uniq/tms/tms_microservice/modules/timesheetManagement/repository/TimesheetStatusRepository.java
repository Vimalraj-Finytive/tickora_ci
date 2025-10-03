package com.uniq.tms.tms_microservice.modules.timesheetManagement.repository;

import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.TimesheetStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TimesheetStatusRepository extends JpaRepository<TimesheetStatusEntity, String> {
    Optional<TimesheetStatusEntity> findByStatusName(String label);
}
