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
import com.uniq.tms.tms_microservice.modules.userManagement.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.UserRole;
import com.uniq.tms.tms_microservice.modules.userManagement.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CalendarServiceImpl implements CalendarService {

    private static final Logger log = LoggerFactory.getLogger(CalendarServiceImpl.class);

    private final CalendarEntityMapper calendarEntityMapper;
    private final CalendarAdapter calendarAdapter;
    private final IdGenerationService idGenerationService;
    private final HolidayEntityMapper holidayEntityMapper;
    private final UserAdapter userAdapter;
    private final UserService userService;

    public CalendarServiceImpl(CalendarEntityMapper calendarEntityMapper, CalendarAdapter calendarAdapter, IdGenerationService idGenerationService, HolidayEntityMapper holidayEntityMapper, UserAdapter userAdapter, UserService userService) {
        this.calendarEntityMapper = calendarEntityMapper;
        this.calendarAdapter = calendarAdapter;
        this.idGenerationService = idGenerationService;
        this.holidayEntityMapper = holidayEntityMapper;
        this.userAdapter = userAdapter;
        this.userService = userService;
    }

    @Override
    @Transactional
    public Calendar create(Calendar calendarMiddleware, String orgId) {
        log.info("Creating calendar: {}", calendarMiddleware);
        boolean isDefault = Boolean.TRUE.equals(calendarMiddleware.getIsDefault());
        calendarMiddleware.setIsDefault(isDefault);
        calendarMiddleware.setId(idGenerationService.generateNextId(IdGenerationTypeEnum.CALENDAR));

        if (isDefault) {
            calendarAdapter.unsetExistingDefault();
        }

        CalendarEntity entity = calendarEntityMapper.toEntity(calendarMiddleware);
        CalendarEntity savedEntity = calendarAdapter.saveCalendar(entity);
        boolean shouldAssignToSuperAdmin = calendarMiddleware.getDefault();
        if (shouldAssignToSuperAdmin) {
            int superAdminRoleLevel = UserRole.SUPERADMIN.getHierarchyLevel();
            List<UserEntity> superAdminUser = userAdapter.findUserByOrgIdAndRoleId(orgId, superAdminRoleLevel);
            List<UserEntity> userEntities = new ArrayList<>();
            if (superAdminUser != null) {
                for (UserEntity user : superAdminUser) {
                    user.setCalendar(savedEntity);
                    userEntities.add(user);
                }
                userAdapter.save(userEntities);
            }
        }

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
        CalendarEntity defaultCalendar = calendarAdapter.findDefaultCalendar();
        String defaultCalendarId = defaultCalendar.getId();
        List<String> calendarIds = calendarEntities.stream()
                .map(CalendarEntity::getId)
                .collect(Collectors.toList());
        List<UserEntity> users = userAdapter.findByCalendar_CalendarIdIn(calendarIds);
        users.forEach(user ->
                userService.updateUserCalendar(user, defaultCalendarId)
        );
        userAdapter.saveAllUsers(users);
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
    public Calendar update(Calendar model,String orgId){
        CalendarEntity entity = calendarEntityMapper.toEntity(model);
        CalendarEntity calendarEntity=calendarAdapter.updateCalendar(entity);
        Calendar calendar= calendarEntityMapper.toModel(calendarEntity);
        boolean shouldAssignToSuperAdmin = model.getDefault();
        if (shouldAssignToSuperAdmin) {
            int superAdminRoleLevel = UserRole.SUPERADMIN.getHierarchyLevel();
            List<UserEntity> superAdminUser = userAdapter.findUserByOrgIdAndRoleId(orgId, superAdminRoleLevel);
            List<UserEntity> userEntities = new ArrayList<>();
            if (superAdminUser != null) {
                for (UserEntity user : superAdminUser) {
                    user.setCalendar(calendarEntity);
                    userEntities.add(user);
                }
                userAdapter.save(userEntities);
            }
        }
        return calendar;
    }

    @Override
    public Holiday createHoliday(Holiday holidayMiddleware, String calendarId) {
        if (calendarAdapter.existsByCalendarIdAndDate(calendarId, holidayMiddleware.getDate())) {
            throw new IllegalArgumentException("Holiday already exists for the selected date in this calendar");
        }
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
        boolean duplicateExists = calendarAdapter.existsByDate(calendarId, holidayDto.getDate(), holidayId);
        if (duplicateExists) {
            throw new IllegalArgumentException("Holiday already exists for the selected date in this calendar");
        }
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
