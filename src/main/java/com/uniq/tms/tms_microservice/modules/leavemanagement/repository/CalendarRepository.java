package com.uniq.tms.tms_microservice.modules.leavemanagement.repository;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.CalendarEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CalendarRepository extends JpaRepository<CalendarEntity, String> {
    @Modifying
    @Transactional
    @Query("UPDATE CalendarEntity c SET c.isDefault = false WHERE c.isDefault = true")
    void unsetExistingDefault();

    @Query("SELECT MAX(c.id) FROM CalendarEntity c WHERE c.id LIKE CONCAT(:prefix, '%')")
    String findMaxIdByPrefix(String prefix);

    @Query("SELECT c FROM CalendarEntity c WHERE c.id IN :ids AND c.isActive = true")
    List<CalendarEntity> findAllCalendarByIdIn(@Param("ids") List<String> entity);

    @Modifying
    @Query("UPDATE CalendarEntity c SET isActive = false WHERE c.id IN :ids AND c.isDefault = false")
    void deleteCalendarByIds(@Param("ids") List<String> calendarIds);

    CalendarEntity findByIdAndIsActiveTrue(String id);

    @Modifying
    @Query("UPDATE CalendarEntity c SET c.isDefault = false WHERE c.id <> :excludeId AND c.isDefault = true")
    void updateAllDefaultsToFalseExcept(@Param("excludeId") String excludeId);
}
