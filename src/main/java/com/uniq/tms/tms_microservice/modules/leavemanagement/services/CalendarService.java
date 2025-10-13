package com.uniq.tms.tms_microservice.modules.leavemanagement.services;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.HolidayDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.Calendar;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.CalendarId;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.Holiday;
import java.util.List;

public interface CalendarService {
    Calendar create(Calendar calendarMiddleware);
    List<Calendar> getAll();
    void delete(CalendarId ids);
    Calendar getById(String id);
    Calendar update(Calendar model);
    Holiday createHoliday(Holiday holidayMiddleware, String calendarId);
    Holiday updateHoliday(HolidayDto holidayDto, String id, String holidayId);
    List<Holiday> findHolidaysByCalendar(String id);
    void deleteHolidayById(String calendarId,String holidayId);
}
