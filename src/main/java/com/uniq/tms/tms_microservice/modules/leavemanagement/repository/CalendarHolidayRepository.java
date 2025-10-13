package com.uniq.tms.tms_microservice.modules.leavemanagement.repository;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.CalendarHolidayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface CalendarHolidayRepository extends JpaRepository<CalendarHolidayEntity , String> {

    @Query("SELECT MAX(ch.id) FROM CalendarHolidayEntity ch WHERE ch.id LIKE CONCAT(:prefix, '%')")
    String findMaxIdByPrefix(String prefix);

    List<CalendarHolidayEntity> findByCalendar_Id(String id);

    boolean existsByIdAndCalendar_Id(  String holidayId ,String calendarId);

    @Transactional
    @Modifying
    @Query("DELETE FROM CalendarHolidayEntity h WHERE h.id = :holidayId AND h.calendar.id = :calendarId")
    void deleteByCalendarAndHoliday(@Param("calendarId") String calendarId, @Param("holidayId") String holidayId);
}
