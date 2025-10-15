package com.uniq.tms.tms_microservice.modules.leavemanagement.services.impl;

import com.uniq.tms.tms_microservice.modules.identityManagement.enums.IdGenerationTypeEnum;
import com.uniq.tms.tms_microservice.modules.identityManagement.service.IdGenerationService;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.CalendarAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.HolidayDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.CalendarEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.CalendarHolidayEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.ImportType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.CalendarEntityMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.HolidayEntityMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.Calendar;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.CalendarId;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.Holiday;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.CalendarService;
import com.uniq.tms.tms_microservice.modules.organizationManagement.repository.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CalendarServiceImpl implements CalendarService {

    private static final Logger log = LoggerFactory.getLogger(CalendarServiceImpl.class);

    private final CalendarEntityMapper calendarEntityMapper;
    private final CalendarAdapter calendarAdapter;
    private final IdGenerationService idGenerationService;
    private final HolidayEntityMapper holidayEntityMapper;
    private final OrganizationRepository organizationRepository;

    public CalendarServiceImpl(CalendarEntityMapper calendarEntityMapper, CalendarAdapter calendarAdapter, IdGenerationService idGenerationService, HolidayEntityMapper holidayEntityMapper, OrganizationRepository organizationRepository) {
        this.calendarEntityMapper = calendarEntityMapper;
        this.calendarAdapter = calendarAdapter;
        this.idGenerationService = idGenerationService;
        this.holidayEntityMapper = holidayEntityMapper;
        this.organizationRepository = organizationRepository;
    }

    @Override
    @Transactional
    public Calendar create(Calendar calendarMiddleware) {
        log.info("Creating calendar: {}", calendarMiddleware);
        boolean isDefault = Boolean.TRUE.equals(calendarMiddleware.getIsDefault());
        calendarMiddleware.setIsDefault(isDefault);
        calendarMiddleware.setId(idGenerationService.generateNextId(IdGenerationTypeEnum.CALENDAR));

        if (isDefault) {
            calendarAdapter.unsetExistingDefault();
        }

        CalendarEntity entity = calendarEntityMapper.toEntity(calendarMiddleware);
        CalendarEntity savedEntity = calendarAdapter.saveCalendar(entity);

        log.info("Calendar saved successfully with ID: {}", savedEntity.getId());

        if (calendarMiddleware.getImportType()== ImportType.AUTO) {
            String countryCode = calendarMiddleware.getCountryCode();
            List<HolidayDto> existingHolidays = calendarAdapter.getHolidaysFromPublicHolidays(countryCode);
            if (existingHolidays.isEmpty()) {
                log.info("No existing holidays found in public_holidays for country: {}. Fetching from Nager API...", countryCode);
                existingHolidays = calendarAdapter.fetchHolidaysFromNager(countryCode);

                if (!existingHolidays.isEmpty()) {
                    log.info("Fetched {} holidays from Nager API. Saving to public_holidays...", existingHolidays.size());
                    calendarAdapter.saveHolidays(existingHolidays, existingHolidays.getFirst().getCountryCode());
                } else {
                    log.warn("No holidays found from Nager API for country: {}", countryCode);
                }
            }
            log.info("Saving {} holidays to calendar_details for calendar ID: {}", existingHolidays.size(), savedEntity.getId());
            calendarAdapter.saveHolidaysToCalendarDetails(savedEntity, existingHolidays);
        }
        log.info("Calendar creation process completed successfully for ID: {}", savedEntity.getId());
        return calendarEntityMapper.toModel(savedEntity);
    }

    @Override
    public List<Calendar> getAll(){
        return calendarAdapter.getAllCalendars().stream()
                .filter(Objects::nonNull)
                .filter(e -> Boolean.TRUE.equals(e.getActive()))
                .map(calendarEntityMapper::toModel)
                .toList();
    }

    @Override
    @Transactional
    public void delete(CalendarId ids){
        List<CalendarEntity> entity = calendarEntityMapper.toEntity(ids);
        List<CalendarEntity> calendarEntities = calendarAdapter.findAllCalendarByIds(entity);
        if (calendarEntities == null || calendarEntities.isEmpty()) {
            log.warn("No calendars found for IDs: {}", ids);
            return;
        }

        boolean hasDefault = calendarEntities.stream()
                .anyMatch(CalendarEntity::getIsDefault);

        if (hasDefault) {
            throw new IllegalArgumentException("Default calendars cannot be deleted");
        }

        log.info("Archiving and deleting calendars: {}", ids);
        calendarAdapter.deleteCalendarById(calendarEntities);
    }

    @Override
    public Calendar getById(String id){
        CalendarEntity entity = calendarAdapter.getById(id);
        if(entity == null){
            return null;
        }
        return calendarEntityMapper.toModel(entity);
    }

    @Override
    public Calendar update(Calendar model){
        CalendarEntity entity = calendarEntityMapper.toEntity(model);
        return calendarEntityMapper.toModel(calendarAdapter.updateCalendar(entity));
    }

    @Override
    public Holiday createHoliday(Holiday holidayMiddleware, String calendarId) {
        CalendarHolidayEntity entity = holidayEntityMapper.toEntity(holidayMiddleware);
        entity.setId(idGenerationService.generateNextId(IdGenerationTypeEnum.CALENDAR_DETAILS));
        Optional<CalendarEntity> optionalCalendar = calendarAdapter.findByCalendarId(calendarId);
        if (optionalCalendar.isEmpty()) {
            throw new IllegalArgumentException("Calendar not found for ID: " + calendarId);
        }
        CalendarEntity calendar = optionalCalendar.get();
        entity.setCalendar(calendar);
        entity.setYear(String.valueOf(entity.getDate().getYear()));
        CalendarHolidayEntity savedEntity = calendarAdapter.saveManualHolidays(entity);
        return holidayEntityMapper.toModel(savedEntity);
    }

    @Override
    public Holiday updateHoliday(HolidayDto holidayDto, String calendarId, String holidayId) {
        CalendarHolidayEntity existingEntity = Optional.ofNullable(calendarAdapter.findById(holidayId))
                .orElseThrow(() -> new EntityNotFoundException("Holiday not found: " + holidayId));
        Optional.of(existingEntity)
                .filter(holiday -> holiday.getCalendar().getId().equals(calendarId))
                .orElseThrow(() -> new IllegalArgumentException("Holiday does not belong to the specified calendar."));
        CalendarHolidayEntity updatedEntity = Optional.of(existingEntity)
                .map(entity -> {
                    entity.setName(holidayDto.getName());
                    entity.setDate(holidayDto.getDate());
                    entity.setYear(String.valueOf(holidayDto.getDate().getYear()));
                    return entity;
                })
                .map(calendarAdapter::updateHoliday)
                .orElseThrow(() -> new RuntimeException("Error updating holiday"));
        return holidayEntityMapper.toModel(updatedEntity);
    }

    @Override
    public List<Holiday> findHolidaysByCalendar(String id, String year) {
        if(!calendarAdapter.existsById(id)){
            throw new IllegalArgumentException("calendar Id not found");
        }
        return calendarAdapter.findHolidayByCalendarId(id, year).stream()
                .map(holidayEntityMapper::toModel)
                .toList();
    }

    @Override
    public void deleteHolidayById(String calendarId, String holidayId) {
        if(calendarAdapter.existsCalendarIdAndHolidayId(calendarId,holidayId)){
            calendarAdapter.deleteByCalendarAndHoliday(calendarId,holidayId);
        }
        else{
            throw new IllegalArgumentException("Holiday or CalendarId not found");
        }
    }
}
