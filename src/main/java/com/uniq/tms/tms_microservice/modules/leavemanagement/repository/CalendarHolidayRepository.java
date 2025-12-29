package com.uniq.tms.tms_microservice.modules.leavemanagement.repository;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.CalendarHolidayEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.projection.CalendarHolidayProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface CalendarHolidayRepository extends JpaRepository<CalendarHolidayEntity , String> {

    @Query("SELECT MAX(ch.id) FROM CalendarHolidayEntity ch WHERE ch.id LIKE CONCAT(:prefix, '%')")
    String findMaxIdByPrefix(String prefix);

    List<CalendarHolidayEntity> findByCalendar_IdAndYear(String id, String year);

    boolean existsByIdAndCalendar_Id(  String holidayId ,String calendarId);

    @Transactional
    @Modifying
    @Query("DELETE FROM CalendarHolidayEntity h WHERE h.id = :holidayId AND h.calendar.id = :calendarId")
    void deleteByCalendarAndHoliday(@Param("calendarId") String calendarId, @Param("holidayId") String holidayId);

    @Query("SELECT h.calendar.id, h.date FROM CalendarHolidayEntity h WHERE h.calendar.id IN :calendarIds")
    List<Object[]> findHolidayDatesByCalendarIds(@Param("calendarIds") Set<String> calendarIds);

    @Query("""
    SELECT h.calendar.id AS calendarId,
           h.date AS date
    FROM CalendarHolidayEntity h
    """)
    List<CalendarHolidayProjection> findAllHolidayDates();

    boolean existsByCalendar_IdAndDate(String calendarId, LocalDate date);
    boolean existsByCalendar_IdAndDateAndIdNot(String calendarId, LocalDate date,String holidayId);

    @Query("""
SELECT h FROM CalendarHolidayEntity h
WHERE h.calendar.id = :calendarId
  AND h.year IN (:currentYear, :nextYear)
""")
    List<CalendarHolidayEntity> findByCalendarAndTwoYears(
            @Param("calendarId") String calendarId,
            @Param("currentYear") String currentYear,
            @Param("nextYear") String nextYear
    );

}
