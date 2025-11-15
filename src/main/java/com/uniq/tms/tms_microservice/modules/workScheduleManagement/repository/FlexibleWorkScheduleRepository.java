package com.uniq.tms.tms_microservice.modules.workScheduleManagement.repository;

import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.FlexibleWorkScheduleEntity;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.enums.DayOfWeekEnum;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FlexibleWorkScheduleRepository extends JpaRepository<FlexibleWorkScheduleEntity, String> {

    @Query("SELECT MAX(w.flexibleWorkScheduleId) FROM FlexibleWorkScheduleEntity w WHERE w.flexibleWorkScheduleId LIKE CONCAT(:prefix, '%')")
    String findMaxIdByPrefix(@Param("prefix") String prefix);

    @Modifying
    @Transactional
    @Query("DELETE FROM FlexibleWorkScheduleEntity f WHERE f.workScheduleEntity.scheduleId = :scheduleId")
    void deleteByScheduleId(@Param("scheduleId") String scheduleId);

    List<FlexibleWorkScheduleEntity> findByworkScheduleEntity_scheduleId(String scheduleId);

    @Query("SELECT f FROM FlexibleWorkScheduleEntity f " +
            "WHERE f.workScheduleEntity.scheduleId = :workScheduleId " +
            "AND f.day = :day")
    FlexibleWorkScheduleEntity findByWorkScheduleIdAndDay(
            @Param("workScheduleId") String workScheduleId,
            @Param("day") DayOfWeekEnum day
    );
}
