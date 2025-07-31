package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.FixedWorkScheduleEntity;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FixedWorkScheduleRepository extends JpaRepository<FixedWorkScheduleEntity, String> {

    @Query("SELECT MAX(w.fixedWorkScheduleId) FROM FixedWorkScheduleEntity w WHERE w.fixedWorkScheduleId LIKE CONCAT(:prefix, '%')")
    String findMaxIdByPrefix(@Param("prefix") String prefix);

    @Modifying
    @Transactional
    @Query("DELETE FROM FixedWorkScheduleEntity f WHERE f.workScheduleEntity.scheduleId = :scheduleId")
    void deleteByScheduleId(@Param("scheduleId") String scheduleId);

    List<FixedWorkScheduleEntity> findByworkScheduleEntity_scheduleId(String scheduleId);
}
