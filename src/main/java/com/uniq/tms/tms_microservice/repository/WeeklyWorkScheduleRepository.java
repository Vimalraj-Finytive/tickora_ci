package com.uniq.tms.tms_microservice.repository;

import com.uniq.tms.tms_microservice.entity.WeeklyWorkScheduleEntity;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WeeklyWorkScheduleRepository extends JpaRepository<WeeklyWorkScheduleEntity, String> {

    @Query("SELECT MAX(w.weeklyWorkScheduleId) FROM WeeklyWorkScheduleEntity w WHERE w.weeklyWorkScheduleId LIKE CONCAT(:prefix, '%')")
    String findMaxIdByPrefix(@Param("prefix") String prefix);

    @Modifying
    @Transactional
    @Query("DELETE FROM WeeklyWorkScheduleEntity f WHERE f.workScheduleEntity.scheduleId = :scheduleId")
    void deleteByScheduleId(@Param("scheduleId") String scheduleId);

}
