package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.WorkScheduleTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkScheduleTypeRepository extends JpaRepository<WorkScheduleTypeEntity, String> {

    @Query("SELECT MAX(w.typeId) FROM WorkScheduleTypeEntity w WHERE w.typeId LIKE CONCAT(:prefix, '%')")
    String findMaxIdByPrefix(@Param("prefix") String prefix);
}
