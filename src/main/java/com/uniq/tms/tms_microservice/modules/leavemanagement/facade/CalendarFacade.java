package com.uniq.tms.tms_microservice.modules.leavemanagement.facade;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.CalendarDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.CalendarIdDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.CalendarResponseDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.HolidayDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.CalendarDtoMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.HolidayDtoMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.CalendarId;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.Holiday;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.CalendarService;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import org.springframework.stereotype.Component;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.Calendar;
import java.util.List;
import java.util.Objects;

@Component
public class CalendarFacade {

    private final CalendarService calendarService;
    private final CalendarDtoMapper calendarDtoMapper;
    private final HolidayDtoMapper holidayDtoMapper;

    public CalendarFacade(CalendarService calendarService, CalendarDtoMapper calendarDtoMapper, HolidayDtoMapper holidayDtoMapper) {
        this.calendarService = calendarService;
        this.calendarDtoMapper = calendarDtoMapper;
        this.holidayDtoMapper = holidayDtoMapper;
    }

    public ApiResponse<CalendarDto> create(CalendarDto calendarDto) {
        Calendar calendarMiddleware = calendarDtoMapper.toMiddleware(calendarDto);
        Calendar saveCalendar = calendarService.create(calendarMiddleware);
        calendarDtoMapper.toDto(saveCalendar);
        return new ApiResponse<>(200,"Calendar Created Successfully",null);
    }

    public ApiResponse<List<CalendarResponseDto>> getAll() {
        List<CalendarResponseDto> calendar = calendarService.getAll().stream()
                .filter(Objects::nonNull)
                .map(calendarDtoMapper::toResponseDto)
                .toList();
        return new ApiResponse<>(200,"Calendar Fetched Successfully",calendar);
    }

    public void delete(CalendarIdDto ids) {
        CalendarId model = calendarDtoMapper.toModel(ids);
        calendarService.delete(model);
    }

    public ApiResponse<CalendarDto> getById(String id) {

        Calendar model =  calendarService.getById(id);
        if(model == null){
            return new ApiResponse<>(404,"Calendar Not Found",null);
        }
        CalendarDto dto = calendarDtoMapper.toDto(model);
        return new ApiResponse<>(200,"Calendar fetched successfully",dto);
    }

    public CalendarDto update(CalendarDto dto) {
        Calendar model = calendarDtoMapper.toMiddleware(dto);
        Calendar calendarModel = calendarService.update(model);
        return calendarDtoMapper.toDto(calendarModel);
    }

    public ApiResponse<HolidayDto> createHoliday(HolidayDto holidayDto, String calendarId) {
        Holiday holidayMiddleware = holidayDtoMapper.toMiddleware(holidayDto);
        Holiday savedHoliday = calendarService.createHoliday(holidayMiddleware, calendarId);
        HolidayDto dto = holidayDtoMapper.toDto(savedHoliday);
        return new ApiResponse<>(201,"Holiday Created Successfully",dto);
    }

    public ApiResponse<HolidayDto> updateHoliday(HolidayDto holidayDto, String id, String holidayId) {
        HolidayDto dto = holidayDtoMapper.toDto(calendarService.updateHoliday(holidayDto,id,holidayId));
        return new ApiResponse<>(200,"Holiday Updated Successfully", null);
    }

    public ApiResponse<List<HolidayDto>> findHolidayByCalendarId(String id, String year) {
        List<HolidayDto> holiday = calendarService.findHolidaysByCalendar(id, year).stream()
                .map(holidayDtoMapper::toDto).toList();
        return new ApiResponse<>(200,"Holidays Fetched Successfully",holiday);
    }

    public void deleteHolidayById(String calendarId, String holidayId) {
        calendarService.deleteHolidayById(calendarId,holidayId);
    }
}
