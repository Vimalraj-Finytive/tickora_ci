package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.identityManagement.enums.IdGenerationTypeEnum;
import com.uniq.tms.tms_microservice.modules.identityManagement.service.IdGenerationService;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.CalendarAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.HolidayDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.CalendarEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.CalendarHolidayEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.CountryEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.PublicHolidayEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.CalendarDtoMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.HolidayDtoMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.CalendarId;
import com.uniq.tms.tms_microservice.modules.leavemanagement.projection.CalendarHolidayProjection;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.CalendarHolidayRepository;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.CalendarRepository;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.CountryRepository;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.PublicHolidayRepository;
import com.uniq.tms.tms_microservice.shared.util.HolidayJsonLoaderUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CalendarAdapterImpl implements CalendarAdapter {

    private static final Logger log = LoggerFactory.getLogger(CalendarAdapterImpl.class);

    private final CalendarRepository calendarRepository;
    private final PublicHolidayRepository publicHolidayRepository;
    private final HolidayDtoMapper holidayDtoMapper;
    private final CalendarHolidayRepository calendarHolidayRepository;
    private final CalendarDtoMapper calendarDtoMapper;
    private final HolidayJsonLoaderUtil holidayJsonLoaderUtil;
    private final IdGenerationService idGenerationService;
    private final CountryRepository countryRepository;

    public CalendarAdapterImpl(CalendarRepository calendarRepository, PublicHolidayRepository publicHolidayRepository, HolidayDtoMapper holidayDtoMapper, CalendarHolidayRepository calendarHolidayRepository, CalendarDtoMapper calendarDtoMapper, HolidayJsonLoaderUtil holidayJsonLoaderUtil, IdGenerationService idGenerationService, CountryRepository countryRepository) {
        this.calendarRepository = calendarRepository;
        this.publicHolidayRepository = publicHolidayRepository;
        this.holidayDtoMapper = holidayDtoMapper;
        this.calendarHolidayRepository = calendarHolidayRepository;
        this.calendarDtoMapper = calendarDtoMapper;
        this.holidayJsonLoaderUtil = holidayJsonLoaderUtil;
        this.idGenerationService = idGenerationService;
        this.countryRepository = countryRepository;
    }

    @Override
    public void unsetExistingDefault() {
        calendarRepository.unsetExistingDefault();
    }

    @Override
    @Transactional
    public CalendarEntity saveCalendar(CalendarEntity entity) {
        CalendarEntity savedCalendar = calendarRepository.save(entity);
        return savedCalendar;
    }

    @Override
    @Transactional
    public List<HolidayDto> getHolidaysFromPublicHolidays(String countryCode) {
        return publicHolidayRepository.findByCountry_Code(countryCode)
                .stream()
                .map(holidayDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void saveHolidays(List<HolidayDto> existingHolidays, String countryCode) {
        if (existingHolidays == null || existingHolidays.isEmpty()) {
            log.warn("No holidays to save.");
            return;
        }

        log.debug("Fetching country for code: {}", countryCode);
        CountryEntity country = null;
        if (countryCode != null) {
            country = countryRepository.findByCode(countryCode)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Country not found: " + countryCode));

            log.debug("Country found: ID={}, Code={}", country.getId(), country.getCode());
            existingHolidays.forEach(holidayDto -> holidayDto.setCountryCode(countryCode));
            log.info("Starting to process {} holidays for country: {}", existingHolidays.size(), countryCode);
        } else {
            log.warn("Country code is null. Skipping country assignment for holidays.");
        }

        List<PublicHolidayEntity> holidayEntities = existingHolidays.stream()
                .map(holidayDtoMapper::toPublicHolidayEntity)
                .toList();

        List<String> ids = idGenerationService.generateNextId(
                IdGenerationTypeEnum.PUBLIC_HOLIDAY,
                holidayEntities.size()
        );
        log.info("Generated {} unique IDs for public holidays", ids.size());

        for (int i = 0; i < holidayEntities.size(); i++) {
            PublicHolidayEntity entity = holidayEntities.get(i);
            entity.setId(ids.get(i));
            if(country!=null){
                entity.setCountry(country);
            }
        }
        log.info("All entities prepared. Saving {} holidays to database...", holidayEntities.size());
        try {
            publicHolidayRepository.saveAll(holidayEntities);
            log.info("{} holidays saved successfully to public_holidays.", holidayEntities.size());
        } catch (Exception e) {
            log.error("Error saving holidays: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void saveHolidaysToCalendarDetails(CalendarEntity calendarEntity, List<HolidayDto> holidayDto) {
        if (calendarEntity == null || calendarEntity.getId() == null) {
            throw new IllegalArgumentException("CalendarEntity or its ID cannot be null");
        }

        List<CalendarHolidayEntity> calendarHolidays = holidayDto.stream()
                .map(dto -> calendarDtoMapper.toCalendarHolidayEntity(dto, calendarEntity))
                .toList();

        log.info("Saving {} holidays for calendar ID: {}", calendarHolidays.size(), calendarEntity.getId());

        List<String> ids = idGenerationService.generateNextId(IdGenerationTypeEnum.CALENDAR_DETAILS, calendarHolidays.size());
        log.info("Calendar details Id : {}", ids);

        for (int i=0 ; i< calendarHolidays.size() ; i++){
            CalendarHolidayEntity entity = calendarHolidays.get(i);
            entity.setId(ids.get(i));
        }
        calendarHolidayRepository.saveAll(calendarHolidays);

        log.info("Holidays successfully saved for calendar ID: {}", calendarEntity.getId());
    }

    @Override
    @Transactional
    public List<HolidayDto> fetchHolidaysFromNager(String countryCode) {
        int currentYear = LocalDate.now().getYear();
        List<Integer> years = List.of(currentYear - 1, currentYear, currentYear + 1);
        return holidayJsonLoaderUtil.getHolidaysForYears(countryCode, years);
    }

    @Override
    public List<CalendarEntity> getAllCalendars(){
        return calendarRepository.findAll();
    }

    @Override
    public void deleteCalendarById(List<CalendarEntity> calendarIds) {
        if (calendarIds == null || calendarIds.isEmpty()) {
            return;
        }
        List<String> ids = calendarIds.stream()
                .map(CalendarEntity::getId)
                .collect(Collectors.toList());
         calendarRepository.deleteCalendarByIds(ids);
    }

    @Override
    public List<CalendarEntity> findAllCalendarByIds(List<CalendarEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        List<String> ids = entities.stream()
                .map(CalendarEntity::getId)
                .collect(Collectors.toList());
        return calendarRepository.findAllCalendarByIdIn(ids);
    }

    @Override
    public CalendarEntity getById(String id) {
        return calendarRepository.findByIdAndIsActiveTrue(id);
    }

    @Override
    @Transactional
    public CalendarEntity updateCalendar(CalendarEntity entity) {
        CalendarEntity existing = calendarRepository.findById(entity.getId())
                .orElseThrow(() -> new EntityNotFoundException("Calendar not found"));
        if (Boolean.TRUE.equals(entity.getIsDefault())) {
            calendarRepository.updateAllDefaultsToFalseExcept(entity.getId());
        }
        if (entity.getName() != null)
            existing.setName(entity.getName());
        if (entity.getIsDefault() != null)
            existing.setIsDefault(entity.getIsDefault());
        return calendarRepository.save(existing);
    }

    @Override
    public Optional<CalendarEntity> findByCalendarId(String calendarId) {
        return calendarRepository.findByCalendarId(calendarId);
    }

    @Override
    public CalendarHolidayEntity saveManualHolidays(CalendarHolidayEntity entity) {
        return calendarHolidayRepository.save(entity);
    }

    public CalendarHolidayEntity findById(String holidayId) {
        return calendarHolidayRepository.findById(holidayId)
                .orElseThrow(() -> new EntityNotFoundException("Holiday not found: " + holidayId));
    }

    @Override
    public CalendarHolidayEntity updateHoliday(CalendarHolidayEntity entity) {
        return Optional.of(entity)
                .map(calendarHolidayRepository::save)
                .orElseThrow(() -> new RuntimeException("Error updating holiday"));
    }

    @Override
    public List<CalendarHolidayEntity> findHolidayByCalendarId(String id, String year) {
        return calendarHolidayRepository.findByCalendar_IdAndYear(id, year);
    }

    @Override
    public Boolean existsById(String id) {
        return calendarRepository.existsById(id);
    }

    @Override
    public Boolean existsCalendarIdAndHolidayId(String calendarId, String holidayId) {
        return calendarHolidayRepository.existsByIdAndCalendar_Id(holidayId, calendarId);
    }

    @Override
    public void deleteByCalendarAndHoliday(String calendarId, String holidayId) {
        calendarHolidayRepository.deleteByCalendarAndHoliday(calendarId,holidayId);
    }

    @Override
    public CalendarEntity findByCalendarIdAndDefaultTrue(CalendarId ids) {
        return calendarRepository.findByIdAndIsDefaultTrue(ids);
    }

    @Override
    public CalendarEntity findDefaultCalendar() {
        return calendarRepository.findDefaultCalendar();
    }

    @Override
    public List<CalendarHolidayProjection> findAllHolidayDates() {
        return calendarHolidayRepository.findAllHolidayDates();
    }

}
