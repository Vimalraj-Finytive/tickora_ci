package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.HolidayDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.CalendarEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.CalendarHolidayEntity;
import java.util.List;

public interface CalendarAdapter {
    void unsetExistingDefault();
    CalendarEntity saveCalendar(CalendarEntity entity);
    List<HolidayDto> getHolidaysFromPublicHolidays(String countryCode);
    List<HolidayDto> fetchHolidaysFromNager(String countryCode);
    void saveHolidays(List<HolidayDto> existingHolidays, String countryCode);
    void saveHolidaysToCalendarDetails(CalendarEntity savedEntity, List<HolidayDto> existingHolidays);
    List<CalendarEntity> getAllCalendars();
    void deleteCalendarById(List<CalendarEntity> calendarIds);
    List<CalendarEntity> findAllCalendarByIds(List<CalendarEntity> entity);
    CalendarEntity getById(String id);
    CalendarEntity updateCalendar(CalendarEntity entity);
    CalendarEntity findByCalendarId(String calendarId);
    CalendarHolidayEntity saveManualHolidays(CalendarHolidayEntity entity);
    CalendarHolidayEntity findById(String holidayId);
    CalendarHolidayEntity updateHoliday(CalendarHolidayEntity existingEntity);
    List<CalendarHolidayEntity> findHolidayByCalendarId(String id);
    Boolean existsById(String id);
    Boolean existsCalendarIdAndHolidayId(String calendarId,String holidayId);
    void deleteByCalendarAndHoliday(String calendarId , String holidayId);
}
