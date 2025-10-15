package com.uniq.tms.tms_microservice.modules.leavemanagement.controller;

import com.uniq.tms.tms_microservice.modules.leavemanagement.constant.LeaveConstant;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.CalendarDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.CalendarIdDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.CalendarResponseDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.HolidayDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.facade.CalendarFacade;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(LeaveConstant.CALENDAR_URL)
public class CalendarController {

    private final CalendarFacade calendarFacade;

    public CalendarController(CalendarFacade calendarFacade) {
        this.calendarFacade = calendarFacade;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CalendarDto>> create(@RequestHeader("Authorization") String token,
                                                           @Validated @RequestBody CalendarDto calendarDto) {
        ApiResponse<CalendarDto> savedCalendar = calendarFacade.create(calendarDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCalendar);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CalendarResponseDto>>> getAll(@RequestHeader("Authorization") String token){
        ApiResponse<List<CalendarResponseDto>> calendarDto = calendarFacade.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(calendarDto);
    }

    @DeleteMapping
    public ResponseEntity<Void> Delete(@RequestHeader("Authorization") String token,
                                       @RequestBody CalendarIdDto ids){
        calendarFacade.delete(ids);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/id")
    public ResponseEntity<ApiResponse<CalendarDto>> getById(@RequestHeader("Authorization") String token,
                                                            @RequestParam String id) {
        ApiResponse<CalendarDto> dto = calendarFacade.getById(id);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<CalendarDto>> updateCalendar(
            @RequestBody CalendarDto dto) {
        CalendarDto updated = calendarFacade.update(dto);
        return ResponseEntity.ok(new ApiResponse<>(200, "Calendar updated successfully", updated));
    }

    @PostMapping("/{id}/holidays")
    public ResponseEntity<ApiResponse<HolidayDto>> createHoliday(
            @Validated @RequestBody HolidayDto holidayDto,
            @PathVariable String id) {
        ApiResponse<HolidayDto> savedHoliday = calendarFacade.createHoliday(holidayDto, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedHoliday);
    }

    @PutMapping("/{id}/holidays/{holidayId}")
    public ResponseEntity<ApiResponse<HolidayDto>> updateHoliday(
            @Validated @RequestBody HolidayDto holidayDto,
            @PathVariable String id,
            @PathVariable String holidayId
    ) {
        ApiResponse<HolidayDto> updatedHoliday = calendarFacade.updateHoliday(holidayDto, id, holidayId);
        return ResponseEntity.status(HttpStatus.OK).body(updatedHoliday);
    }

    @GetMapping("/{id}/holidays")
    public ResponseEntity<ApiResponse<List<HolidayDto>>> getHolidayBYId(@PathVariable String id, @RequestParam String year) {
        ApiResponse<List<HolidayDto>> holidayDto = calendarFacade.findHolidayByCalendarId(id, year);
        return ResponseEntity.status(HttpStatus.OK).body(holidayDto);
    }

    @DeleteMapping("/{calendarId}/holidays/{holidayId}")
    public ResponseEntity<Void> deleteHolidayById(@PathVariable String calendarId, @PathVariable String holidayId){
        calendarFacade.deleteHolidayById(calendarId,holidayId);
        return ResponseEntity.noContent().build();
    }
}
