package com.uniq.tms.tms_microservice.modules.timesheetManagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.CalendarHolidayRepository;
import com.uniq.tms.tms_microservice.modules.locationManagement.entity.LocationEntity;
import com.uniq.tms.tms_microservice.modules.locationManagement.repository.LocationRepository;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.*;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.projection.TimesheetUserProjection;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.repository.TimesheetStatusRepository;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.LogFrom;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.LogType;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.TimesheetStatusEnum;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.TimesheetWorkStatusEnum;
import com.uniq.tms.tms_microservice.modules.userManagement.projections.UserCalendarProjection;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.model.ScheduleTypeInfo;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.enums.DayOfWeekEnum;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.model.ScheduleTypeInfo;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.repository.FixedWorkScheduleRepository;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.repository.FlexibleWorkScheduleRepository;
import com.uniq.tms.tms_microservice.shared.helper.TimesheetHelper;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.mapper.TimesheetDtoMapper;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.TimesheetHistoryEntity;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.TimesheetStatusEntity;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.projection.TimesheetHistoryProjection;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.projection.TimesheetProjection;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.repository.TimesheetHistoryRepository;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.repository.TimesheetRepository;
import com.uniq.tms.tms_microservice.modules.userManagement.projections.UserDashboard;
import com.uniq.tms.tms_microservice.modules.userManagement.projections.UserGroupProjection;
import com.uniq.tms.tms_microservice.modules.userManagement.repository.UserRepository;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.FixedWorkScheduleEntity;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.FlexibleWorkScheduleEntity;
import com.uniq.tms.tms_microservice.shared.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TimesheetAdapterImpl implements TimesheetAdapter {

    @Value("${timesheet.extra.worked.minutes}")
    private int extraWorkedMinutes;

    private final TimesheetRepository timesheetRepository;
    private final TimesheetHistoryRepository timesheetHistoryRepository;
    private final TimesheetStatusRepository timesheetStatusRepository;
    private final LocationRepository locationRepository;
    private final TimesheetDtoMapper timesheetDtoMapper;
    private final UserRepository userRepository;
    private final TimesheetHelper timesheetHelper;
    private final FixedWorkScheduleRepository fixedWorkScheduleRepository;
    private final FlexibleWorkScheduleRepository flexibleWorkScheduleRepository;
    private final CalendarHolidayRepository calendarHolidayRepository;

    public TimesheetAdapterImpl(TimesheetRepository timesheetRepository, TimesheetHistoryRepository timesheetHistoryRepository,
                                TimesheetStatusRepository timesheetStatusRepository, LocationRepository locationRepository,
                                TimesheetDtoMapper timesheetDtoMapper, UserRepository userRepository, TimesheetHelper timesheetHelper, FixedWorkScheduleRepository fixedWorkScheduleRepository, FlexibleWorkScheduleRepository flexibleWorkScheduleRepository, CalendarHolidayRepository calendarHolidayRepository) {
        this.timesheetRepository = timesheetRepository;
        this.timesheetHistoryRepository = timesheetHistoryRepository;
        this.timesheetStatusRepository = timesheetStatusRepository;
        this.locationRepository = locationRepository;
        this.timesheetDtoMapper = timesheetDtoMapper;
        this.userRepository = userRepository;
        this.timesheetHelper = timesheetHelper;
        this.fixedWorkScheduleRepository = fixedWorkScheduleRepository;
        this.flexibleWorkScheduleRepository = flexibleWorkScheduleRepository;
        this.calendarHolidayRepository = calendarHolidayRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(TimesheetAdapterImpl.class);

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto filterTimesheetsForAllUsers(
            LocalDate startDate,
            LocalDate endDate,
            List<String> userIds,
            String orgId,
            Integer pageIndex,
            Integer pageSize
    ) {

        Pageable pageable = (pageIndex != null && pageSize != null) ?
                PageRequest.of(pageIndex, pageSize, Sort.by("userId").ascending())
                : Pageable.unpaged();
        log.info("Get paginated users from DB");

        // Step 1: Fetch paginated users
        String[] arrayOfUserIds = userIds.toArray(new String[0]);
        Page<TimesheetProjection> pagedUser = userRepository.findUsersByUserIds(arrayOfUserIds, pageable);
        List<String> pagedUserIds = pagedUser.stream()
                .map(TimesheetProjection::getUserId)
                .toList();

        String[] userIdArrays = pagedUserIds.toArray(new String[0]);

        log.info("Fetched user Groups");
        Map<String, String> userGroups = fetchUserGroupsMap(userIdArrays);
        List<UserCalendarProjection> userCalendarList = userRepository.findCalendarIdsByUserIds(userIdArrays);
        Map<String, String> userToCalendarMap = userCalendarList.stream()
                .filter(Objects::nonNull)
                .filter(u -> u.getUserId() != null)
                .filter(u -> u.getCalendarId() != null)
                .collect(Collectors.toMap(UserCalendarProjection::getUserId, UserCalendarProjection::getCalendarId));
        Set<String> calendarIds = new HashSet<>(userToCalendarMap.values());
        List<Object[]> results = calendarHolidayRepository.findHolidayDatesByCalendarIds(calendarIds);
        Map<String, List<LocalDate>> calendarHolidayMap = results.stream()
                .collect(Collectors.groupingBy(
                        row -> (String) row[0],
                        Collectors.mapping(row -> (LocalDate) row[1], Collectors.toList())
                ));
        log.info("Fetch WorkSchedules for all users in date range");
        TimesheetHelper.WorkScheduleResult result = timesheetHelper.fetchWorkSchedulesAndDays(userIdArrays);
        Map<String, Map<DayOfWeek, FixedWorkScheduleEntity>> fixedMap = result.getFixedMap();
        Map<String, Map<DayOfWeek, FlexibleWorkScheduleEntity>> flexMap = result.getFlexMap();
        Map<String, Set<DayOfWeek>> userWorkingDaysMap = result.getUserWorkingDaysMap();

        log.info("Get all users timesheet");
        Map<String, List<TimesheetDto>> userTimesheetMap = fetchAndMapTimesheets(startDate, endDate, userIdArrays,
                userGroups, fixedMap, flexMap, userWorkingDaysMap, userToCalendarMap, calendarHolidayMap);
        List<UserTimesheetResponseDto> finalResponse = new ArrayList<>();

        for (TimesheetProjection user : pagedUser) {
            List<TimesheetDto> userTimesheets = getUserTimesheetsWithDefaults(
                    user,
                    userTimesheetMap.getOrDefault(user.getUserId(), new ArrayList<>()),
                    startDate,
                    endDate,
                    userWorkingDaysMap.getOrDefault(user.getUserId(), Collections.emptySet()),
                    userGroups,
                    fixedMap,
                    flexMap,
                    userToCalendarMap,
                    calendarHolidayMap
            );

            TimesheetSummaryDto summary = calculateSummaryWithHistory(userTimesheets);

            finalResponse.add(new UserTimesheetResponseDto(summary, userTimesheets));
        }

        finalResponse = finalResponse.stream()
                .filter(dto -> dto.getSummary() != null && dto.getTimesheets() != null && !dto.getTimesheets().isEmpty())
                .toList();

        int safePageIndex = pageIndex != null ? pageIndex : 0;
        int safePageSize = pageSize != null ? pageSize : (int) pagedUser.getTotalElements();
        PaginationDto pagination = buildPagination(pagedUser.getTotalElements(), safePageIndex, safePageSize);
        return createSuccessResponse(finalResponse, pagination);
    }


    /**
     * Fetches and maps timesheets for all users
     */
    private Map<String, List<TimesheetDto>> fetchAndMapTimesheets(
            LocalDate startDate,
            LocalDate endDate,
            String[] arrayOfUserIds,
            Map<String, String> userGroups,
            Map<String, Map<DayOfWeek, FixedWorkScheduleEntity>> fixedMap,
            Map<String, Map<DayOfWeek, FlexibleWorkScheduleEntity>> flexMap,
            Map<String, Set<DayOfWeek>> userWorkingDaysMap,
            Map<String, String> userToCalendarMap,
            Map<String, List<LocalDate>> calendarHolidayMap
    ) {
        List<TimesheetProjection> mainProjections =
                timesheetRepository.fetchMainTimesheets(startDate, endDate, arrayOfUserIds);

        if (mainProjections.isEmpty()) {
            return new HashMap<>();
        }

        Set<Long> timesheetIdSet = mainProjections.stream()
                .map(TimesheetProjection::getId)
                .collect(Collectors.toSet());

        Map<Long, List<TimesheetHistoryDto>> historyMap = fetchTimesheetHistoryMap(timesheetIdSet);
        historyMap.values().forEach(list ->
                list.sort(Comparator.comparing(TimesheetHistoryDto::getTimesheetHistoryId)));
        log.info("Return main projections");
        return mainProjections.stream()
                .map(p -> {
                    TimesheetDto dto = mapToTimesheetDto(p, userGroups, historyMap);
                    LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
                    Set<DayOfWeek> wokingDays = userWorkingDaysMap.getOrDefault(dto.getUserId(),
                            Collections.emptySet());
                    boolean isWorkingDay = wokingDays.contains(dto.getDate().getDayOfWeek());
                    String calendarId = userToCalendarMap.get(dto.getUserId());
                    boolean isHoliday = calendarHolidayMap.getOrDefault(calendarId, List.of()).contains(dto.getDate());
                    log.info("after condition holiday : {}",isHoliday);
                    ScheduleTypeInfo scheduledHours = TimesheetHelper.getScheduledHoursForUser(dto.getUserId(), dto.getDate(), fixedMap, flexMap);
                    setTimesheetStatusAndDayType(dto, isWorkingDay, dto.getDate(), today, scheduledHours, isHoliday);
                    return dto;
                })
                .collect(Collectors.groupingBy(TimesheetDto::getUserId));
    }

    /**
     * Fetches timesheet history and maps it by timesheet ID
     */
    private Map<Long, List<TimesheetHistoryDto>> fetchTimesheetHistoryMap(Set<Long> timesheetIdSet) {
        log.info("Return timesheet history");
        return timesheetRepository
                .fetchTimesheetHistory(timesheetIdSet.toArray(new Long[0]))
                .stream()
                .collect(Collectors.groupingBy(
                        TimesheetHistoryProjection::getTimesheetId,
                        Collectors.mapping(h -> new TimesheetHistoryDto(
                                h.getTimesheetHistoryId(),
                                h.getLocationName(),
                                h.getLogTime(),
                                h.getLogType(),
                                h.getLogFrom()
                        ), Collectors.toList())
                ));
    }

    /**
     * Fetches user groups and maps them by user ID
     */
    private Map<String, String> fetchUserGroupsMap(String[] arrayOfUserIds) {
        log.info("Fetch user groups");
        return timesheetRepository.fetchUserGroups(arrayOfUserIds)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        UserGroupProjection::getUserId,
                        ug -> ug.getGroupNames() != null ? ug.getGroupNames() : "null"
                ));
    }

    /**
     * Maps projection to DTO with groups and history
     */
    private TimesheetDto mapToTimesheetDto(
            TimesheetProjection projection,
            Map<String, String> userGroups,
            Map<Long, List<TimesheetHistoryDto>> historyMap
    ) {
        log.info("Mpa to Dtos");
        TimesheetDto dto = timesheetDtoMapper.toTimeDto(projection);
        dto.setRole(projection.getRoleName() != null ? projection.getRoleName() : "null");
        dto.setGroupName(userGroups.getOrDefault(projection.getUserId(), "null"));
        dto.setHistory(historyMap.getOrDefault(projection.getId(), Collections.emptyList()));
        return dto;
    }

    /**
     * Gets user timesheets with default records if none exist
     */
    private List<TimesheetDto> getUserTimesheetsWithDefaults(
            TimesheetProjection user,
            List<TimesheetDto> existingTimesheets,
            LocalDate startDate,
            LocalDate endDate,
            Set<DayOfWeek> workingDays,
            Map<String, String> userGroups,
            Map<String, Map<DayOfWeek, FixedWorkScheduleEntity>> fixedMap,
            Map<String, Map<DayOfWeek, FlexibleWorkScheduleEntity>> flexMap,
            Map<String, String> userToCalendarMap,
            Map<String, List<LocalDate>> calendarHolidayMap
    ) {
        Map<LocalDate, TimesheetDto> existingTimesheet = existingTimesheets.stream()
                .collect(Collectors.toMap(TimesheetDto::getDate, Function.identity(), (a, b) -> a));
        LocalDate effectiveStart = startDate.isBefore(user.getDate())
                ? user.getDate()
                : startDate;
        if (effectiveStart.isAfter(endDate)) {
            log.info("User joined after requested range ({} > {}), returning empty timesheet list", effectiveStart, endDate);
            return Collections.emptyList();
        }
        List<TimesheetDto> completeList = effectiveStart.datesUntil(endDate.plusDays(1))
                .map(date -> {
                    TimesheetDto existing = existingTimesheet.get(date);
                    if (existing != null) return existing;

                    return createDefaultTimesheetForDate(
                            user,
                            date,
                            LocalDate.now(ZoneId.of("Asia/Kolkata")),
                            workingDays,
                            userGroups,
                            fixedMap,
                            flexMap,
                            userToCalendarMap,
                            calendarHolidayMap
                    );
                })
                .toList();

        log.info("Return default timesheet");
        return completeList;
    }

    /**
     * Creates a single default timesheet record for a specific date
     */
    private TimesheetDto createDefaultTimesheetForDate(
            TimesheetProjection user,
            LocalDate date,
            LocalDate today,
            Set<DayOfWeek> workingDays,
            Map<String, String> userGroups,
            Map<String, Map<DayOfWeek, FixedWorkScheduleEntity>> fixedMap,
            Map<String, Map<DayOfWeek, FlexibleWorkScheduleEntity>> flexMap,
            Map<String, String> userToCalendarMap,
            Map<String, List<LocalDate>> calendarHolidayMap
    ) {
        log.info("User projection is null");
        TimesheetDto timesheet = new TimesheetDto();
        String calendarId = userToCalendarMap.get(user.getUserId());
        boolean isHoliday = calendarHolidayMap.getOrDefault(calendarId, List.of()).contains(date);
        boolean isWorkingDay = workingDays.contains(date.getDayOfWeek());
        timesheet.setId(null);
        timesheet.setDate(date);
        timesheet.setUserId(user.getUserId());
        timesheet.setUserName(user.getUserName());
        timesheet.setMobileNumber(user.getMobileNumber());
        timesheet.setRole(user.getRoleName() != null ? user.getRoleName() : "null");
        timesheet.setWorkScheduleName(user.getWorkScheduleName() != null ? user.getWorkScheduleName() : "null");
        timesheet.setGroupName(userGroups.getOrDefault(user.getUserId(), "null"));
        timesheet.setFirstClockInTime("00:00");
        timesheet.setLastClockOutTime("00:00");
        timesheet.setTrackedHoursDuration("00h 00m");
        timesheet.setRegularHoursDuration("00h 00m");
        timesheet.setStartTimeDuration("00h 00m");
        timesheet.setEndTimeDuration("00h 00m");
        timesheet.setTotalOverTime("00h 00m");
        timesheet.setHistory(Collections.emptyList());

        ScheduleTypeInfo scheduledHours = TimesheetHelper.getScheduledHoursForUser(user.getUserId(), date, fixedMap, flexMap);
        setTimesheetStatusAndDayType(timesheet, isWorkingDay, date, today, scheduledHours, isHoliday);
        return timesheet;
    }

    /**
     * Sets the appropriate status and day type for a timesheet
     */
    private void setTimesheetStatusAndDayType(
            TimesheetDto timesheet,
            boolean isWorkingDay,
            LocalDate date,
            LocalDate today,
            ScheduleTypeInfo scheduledHours,
            boolean isHoliday
    ) {
        log.info("Boolean WS : {}", isWorkingDay);
        timesheet.setDayType(isWorkingDay ? "Working Day" : "Holiday");
        log.info("working day ? :{}", timesheet.getDayType());

        boolean hasClockIn = timesheet.getFirstClockIn() != null;

        boolean hasClockOut = timesheet.getLastClockOut() != null;

        boolean hasSystemGeneratedClockOut = timesheet.getHistory().stream()
                .anyMatch(h -> LogType.CLOCK_OUT.equals(h.getLogType())
                        && LogFrom.SYSTEM_GENERATED.equals(h.getLogFrom()));

        log.info("clockin : {}, clockout : {}, userId: {}",
                timesheet.getFirstClockIn(),
                timesheet.getLastClockOut(),
                timesheet.getUserId());
        log.info("hasClockIn : {}, hasClockOut:{}, sysGenClockOut:{}",
                hasClockIn, hasClockOut, hasSystemGeneratedClockOut);


        // 1. Holiday
        if (isHoliday) {
            timesheet.setUserDayType(TimesheetStatusEnum.PUBLIC_HOLIDAY.getLabel());
            if (hasClockIn) {
                if (hasSystemGeneratedClockOut || !hasClockOut) {
                    timesheet.setWorkStatus(TimesheetWorkStatusEnum.FAILED_CLOCK_OUT.getLabel());
                    timesheet.setStatus(TimesheetStatusEnum.PRESENT.getLabel());
                } else {
                    timesheet.setWorkStatus(TimesheetWorkStatusEnum.EXTRA_WORKED_DAY.getLabel());
                    timesheet.setStatus(TimesheetStatusEnum.PRESENT.getLabel());
                }
            } else {
                timesheet.setWorkStatus(TimesheetStatusEnum.NOT_MARKED.getLabel());
                timesheet.setStatus(TimesheetStatusEnum.PUBLIC_HOLIDAY.getLabel());
            }
            return;
        }

        if (!isWorkingDay) {
            timesheet.setUserDayType(TimesheetStatusEnum.REST_DAY.getLabel());
            if (hasClockIn) {
                if (hasSystemGeneratedClockOut || !hasClockOut) {
                    timesheet.setWorkStatus(TimesheetWorkStatusEnum.FAILED_CLOCK_OUT.getLabel());
                    timesheet.setStatus(TimesheetStatusEnum.PRESENT.getLabel());
                } else {
                    timesheet.setWorkStatus(TimesheetWorkStatusEnum.EXTRA_WORKED_DAY.getLabel());
                    timesheet.setStatus(TimesheetStatusEnum.PRESENT.getLabel());
                }
            } else {
                timesheet.setWorkStatus(TimesheetStatusEnum.NOT_MARKED.getLabel());
                timesheet.setStatus(TimesheetStatusEnum.REST_DAY.getLabel());
            }
            return;
        }
        // 2. Paid Leave / Permission / Half Day
        if (timesheet.getStatus() != null && !Objects.equals(timesheet.getStatus(), TimesheetStatusEnum.PRESENT.getLabel())) {
            log.info("Status : {}", timesheet.getStatus());
            if (TimesheetStatusEnum.PAID_LEAVE.getLabel().equals(timesheet.getStatus())
                    || TimesheetStatusEnum.PERMISSION.getLabel().equals(timesheet.getStatus())
                    || TimesheetStatusEnum.HALF_DAY.getLabel().equals(timesheet.getStatus())) {
                timesheet.setUserDayType(TimesheetWorkStatusEnum.TIME_OFF.getLabel());
                timesheet.setWorkStatus(TimesheetStatusEnum.NOT_MARKED.getLabel());
                timesheet.setStatus(timesheet.getStatus());
            }
            return;
        }
        // 3. Working Day - No clock in
        if (!hasClockIn) {
            timesheet.setUserDayType("Time Off");
            timesheet.setWorkStatus("Time Off");
            timesheet.setStatus(date.isBefore(today)
                    ? TimesheetStatusEnum.ABSENT.getLabel()
                    : TimesheetStatusEnum.NOT_MARKED.getLabel());
            return;
        }

        // 4. Working Day - ClockIn + ClockOut
        if (hasClockIn && hasClockOut) {
            log.info("Have both in and out");
            timesheet.setUserDayType("Working Day");

            if (hasSystemGeneratedClockOut) {
                timesheet.setWorkStatus(TimesheetWorkStatusEnum.FAILED_CLOCK_OUT.getLabel());
                timesheet.setStatus(TimesheetStatusEnum.PRESENT.getLabel());
            } else {
                setWorkStatusForCompletedTimesheet(timesheet, scheduledHours);
            }
            return;
        }

        // 5. Working Day - ClockIn only
        if (hasClockIn && !hasClockOut) {
            timesheet.setUserDayType("Working Day");
            if (date.isEqual(today)) {
                timesheet.setWorkStatus(TimesheetStatusEnum.PRESENT.getLabel());
            } else if (hasSystemGeneratedClockOut) {
                timesheet.setWorkStatus(TimesheetWorkStatusEnum.FAILED_CLOCK_OUT.getLabel());
            } else {
                timesheet.setWorkStatus(TimesheetStatusEnum.PRESENT.getLabel());
            }
            timesheet.setStatus(TimesheetStatusEnum.PRESENT.getLabel());
            return;
        }

        // Fallback
        timesheet.setUserDayType(TimesheetWorkStatusEnum.TIME_OFF.getLabel());
        timesheet.setWorkStatus(TimesheetStatusEnum.NOT_MARKED.getLabel());
        timesheet.setStatus(TimesheetStatusEnum.NOT_MARKED.getLabel());
    }


    /**
     * Sets work status for timesheets with both clock-in and clock-out
     */
    private void setWorkStatusForCompletedTimesheet(TimesheetDto timesheet, ScheduleTypeInfo scheduleInfo) {
        if (scheduleInfo == null) {
            log.warn("No schedule found for userId={}", timesheet.getUserId());
            //timesheet.setWorkStatus(TimesheetWorkStatusEnum.NO_SCHEDULE.getLabel());
            timesheet.setStatus(TimesheetStatusEnum.ABSENT.getLabel());
            return;
        }

        if (!scheduleInfo.isFixed()) {
            log.info("Processing Flexible schedule for userId={}", timesheet.getUserId());

            boolean hasSystemGeneratedClockOut = timesheet.getHistory().stream()
                    .anyMatch(h -> LogType.CLOCK_OUT.equals(h.getLogType())
                            && LogFrom.SYSTEM_GENERATED.equals(h.getLogFrom()));

            if (hasSystemGeneratedClockOut) {
                log.info("System generated clock-out found");
                timesheet.setWorkStatus(TimesheetWorkStatusEnum.FAILED_CLOCK_OUT.getLabel());
                timesheet.setStatus(TimesheetStatusEnum.PRESENT.getLabel());
                return;
            }

            Duration scheduledHours = scheduleInfo.getDuration() != null ? scheduleInfo.getDuration() : Duration.ZERO;
            log.info("Scheduled Hours (Flexible): {}", scheduledHours);

            Duration worked = timesheet.getTrackedHours() != null ? timesheet.getTrackedHours() : Duration.ZERO;
            if (worked.compareTo(scheduledHours) > 0) {
                timesheet.setWorkStatus(TimesheetWorkStatusEnum.OVERTIME.getLabel());
            } else if (worked.compareTo(scheduledHours) >= 0) {
                timesheet.setWorkStatus(TimesheetWorkStatusEnum.SUFFICIENT_HOURS.getLabel());
            } else {
                timesheet.setWorkStatus(TimesheetWorkStatusEnum.LESS_WORKED_HOURS.getLabel());
            }
            timesheet.setStatus(TimesheetStatusEnum.PRESENT.getLabel());
        }
        else {
            log.info("Processing Fixed schedule for userId={}", timesheet.getUserId());
            handleFixedSchedule(timesheet, scheduleInfo);
        }
    }

    private void handleFixedSchedule(TimesheetDto timesheet, ScheduleTypeInfo scheduleInfo) {

        LocalTime fixedStartTime = scheduleInfo.getStartTime();
        LocalTime fixedEndTime = scheduleInfo.getEndTime();
        Duration scheduledHours = scheduleInfo.getDuration();
        log.info("Processing Fixed schedule - Start: {}, End: {}", fixedStartTime, fixedEndTime);
        LocalTime actualStart = timesheet.getFirstClockIn();
        LocalTime actualEnd = timesheet.getLastClockOut();

        if (actualStart != null && actualEnd != null && fixedStartTime != null && fixedEndTime != null) {
            boolean isLateClockIn = actualStart.isAfter(fixedStartTime);
            boolean isEarlyClockOut = actualEnd.isBefore(fixedEndTime);
            Duration worked = timesheet.getTrackedHours() != null ? timesheet.getTrackedHours() : Duration.ZERO;
            boolean hasOvertimeHours = worked.compareTo(scheduledHours) > 0;
//            boolean isOvertime = actualEnd.isAfter(fixedEndTime.plusMinutes(30));

            if (!isLateClockIn  && hasOvertimeHours) {
                timesheet.setWorkStatus(TimesheetWorkStatusEnum.OVERTIME.getLabel());
            } else if (isLateClockIn && isEarlyClockOut) {
                timesheet.setWorkStatus(TimesheetWorkStatusEnum.IRREGULAR_WORK_TIME.getLabel());
            } else if (isLateClockIn) {
                timesheet.setWorkStatus(TimesheetWorkStatusEnum.LATE_CLOCK_IN.getLabel());
            } else if (isEarlyClockOut) {
                timesheet.setWorkStatus(TimesheetWorkStatusEnum.EARLY_CLOCK_OUT.getLabel());
            } else {
                if (worked.compareTo(scheduledHours) >= 0) {
                    timesheet.setWorkStatus(TimesheetWorkStatusEnum.SUFFICIENT_HOURS.getLabel());
                } else {
                    timesheet.setWorkStatus(TimesheetWorkStatusEnum.LESS_WORKED_HOURS.getLabel());
                }
            }
        } else {
            log.warn("Missing clock times for fixed schedule evaluation");
            Duration worked = timesheet.getTrackedHours() != null ? timesheet.getTrackedHours() : Duration.ZERO;
            if (worked.compareTo(scheduledHours) >= 0) {
                timesheet.setWorkStatus(TimesheetWorkStatusEnum.SUFFICIENT_HOURS.getLabel());
            } else {
                timesheet.setWorkStatus(TimesheetWorkStatusEnum.LESS_WORKED_HOURS.getLabel());
            }
        }

        timesheet.setStatus(TimesheetStatusEnum.PRESENT.getLabel());
    }

    /**
     * Creates a successful response with data and pagination
     */
    private PaginationResponseDto createSuccessResponse(
            List<UserTimesheetResponseDto> finalResponse,
            PaginationDto pagination
    ) {
        PaginationResponseDto response = new PaginationResponseDto();
        response.setStatusCode(200);
        response.setMessage("Success");
        response.setUserTimesheetResponseDtos(finalResponse);
        response.setPaginationDto(pagination);
        return response;
    }

    /**
     * Calculates summary for a user's timesheets including history and day type.
     */
    private TimesheetSummaryDto calculateSummaryWithHistory(
            List<TimesheetDto> timesheets
    ) {
        if (timesheets == null || timesheets.isEmpty()) return null;

        int present = 0, absent = 0, holiday = 0, notMarked = 0, paidLeave = 0, halfDay = 0, permission = 0;

        for (TimesheetDto t : timesheets) {

            switch (t.getStatus()) {
                case "Present" -> present++;
                case "Paid Leave" -> paidLeave++;
                case "Half Day" -> halfDay++;
                case "Permission" -> permission++;
                case "Absent" -> absent++;
                case "Not Marked" -> notMarked++;
                case "Holiday" -> holiday++;
                default -> present++;
            }

            // Format times and durations
            t.setFirstClockInTime(formatTime(t.getFirstClockIn()));
            t.setLastClockOutTime(formatTime(t.getLastClockOut()));
            t.setTrackedHoursDuration(formatDuration(t.getTrackedHours()));
            t.setRegularHoursDuration(formatDuration(t.getRegularHours()));
        }

        TimesheetDto first = timesheets.getFirst();

        TimesheetSummaryDto summary = new TimesheetSummaryDto();
        summary.setUserId(first.getUserId());
        summary.setUserName(first.getUserName());
        summary.setMobileNumber(first.getMobileNumber());
        summary.setRole(first.getRole());
        summary.setGroupName(first.getGroupName());
        summary.setPresentCount(present);
        summary.setAbsentCount(absent);
        summary.setHolidayCount(holiday);
        summary.setNotMarkedCount(notMarked);
        summary.setPaidLeaveCount(paidLeave);
        summary.setHalfDayCount(halfDay);
        summary.setPermissionCount(permission);
        summary.setTotalCount(timesheets.size());

        return summary;
    }

    /**
     * Builds pagination DTO
     */
    private PaginationDto buildPagination(long totalElements, int pageIndex, int pageSize) {
        int totalPages = pageSize > 0 ? (int) Math.ceil((double) totalElements / pageSize) : 1;
        boolean isLast = pageSize == 0 || pageIndex >= totalPages - 1;

        PaginationDto dto = new PaginationDto();
        dto.setPageIndex(pageIndex);
        dto.setPageSize(pageSize);
        dto.setTotalPages(totalPages);
        dto.setTotalElements(totalElements);
        dto.setLast(isLast);
        return dto;
    }

    private String formatDuration(Duration duration) {
        if (duration == null || duration.isZero()) {
            return "00h 00m";
        }
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        return String.format("%02dh %02dm", hours, minutes);
    }

    private String formatTime(LocalTime localTime) {
        if (localTime == null) {
            return "00:00";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        return localTime.format(formatter);
    }

    @Override
    public Optional<TimesheetEntity> findByUserIdAndDate(String userId, LocalDate date) {
        return timesheetRepository.findByUser_UserIdAndDate(userId, date);
    }

    @Override
    public TimesheetEntity saveTimesheet(TimesheetEntity timesheet) {
        return timesheetRepository.save(timesheet);
    }

    @Override
    public TimesheetHistoryEntity saveTimesheetHistory(TimesheetHistoryEntity history) {
        TimesheetEntity timesheet = timesheetRepository.findByUser_UserIdAndDate(history.getTimesheet().getUser().getUserId(), history.getTimesheet().getDate())
                .orElseThrow(() -> new IllegalArgumentException("Timesheet not found for user: " + history.getTimesheet().getUser().getUserId()));

        history.setTimesheet(timesheet);
        return timesheetHistoryRepository.save(history);
    }

    @Override
    public void calculateTrackedAndBreakHours(List<TimesheetHistoryEntity> savedLogs) {
        if (savedLogs == null || savedLogs.isEmpty()) return;
        Map<Long, List<TimesheetHistoryEntity>> groupedLogs = savedLogs.stream()
                .filter(log -> log.getTimesheet() != null)
                .collect(Collectors.groupingBy(log -> log.getTimesheet().getId()));
        Set<Long> timesheetIds = groupedLogs.keySet();
        Map<Long, TimesheetEntity> timesheetMap = timesheetRepository.findAllById(timesheetIds)
                .stream()
                .collect(Collectors.toMap(TimesheetEntity::getId, t -> t));
        for (Map.Entry<Long, List<TimesheetHistoryEntity>> entry : groupedLogs.entrySet()) {
            TimesheetEntity timesheet = timesheetMap.get(entry.getKey());
            if (timesheet == null) continue;
            List<TimesheetHistoryEntity> historyLogs = entry.getValue();
            historyLogs.sort(Comparator.comparing(TimesheetHistoryEntity::getLoggedTimestamp));
            long trackedSeconds = DateTimeUtil.toSeconds(timesheet.getTrackedHours());
            long breakSeconds = DateTimeUtil.toSeconds(timesheet.getTotalBreakHours());
            LocalTime lastClockIn = timesheet.getFirstClockIn();
            LocalTime lastClockOut = timesheet.getLastClockOut();
            for (TimesheetHistoryEntity log : historyLogs) {
                LocalTime logTime = log.getLogTime();
                if (logTime == null) continue;
                switch (log.getLogType()) {
                    case CLOCK_IN -> {
                        if (lastClockOut != null) {
                            long breakTime = Duration.between(lastClockOut, logTime).getSeconds();
                            if (breakTime > 0) breakSeconds += breakTime;
                        }
                        lastClockIn = logTime;
                    }
                    case CLOCK_OUT -> {
                        if (lastClockIn != null) {
                            long trackedTime = Duration.between(lastClockIn, logTime).getSeconds();
                            if (trackedTime > 0) trackedSeconds += trackedTime;
                        }
                        lastClockOut = logTime;
                    }
                    default -> {}
                }
            }
            timesheet.setTrackedHours(DateTimeUtil.toLocalTime(trackedSeconds));
            timesheet.setRegularHours(DateTimeUtil.toLocalTime(trackedSeconds));
            timesheet.setTotalBreakHours(DateTimeUtil.toLocalTime(breakSeconds));
            timesheet.setLastClockOut(lastClockOut);
        }
        timesheetRepository.saveAll(timesheetMap.values());
    }

    @Override
    public TimesheetEntity findUserIdAndDate(String userId, LocalDate date) {
        Optional<TimesheetEntity> timesheetOptional = timesheetRepository.findByUser_UserIdAndDate(userId, date);
        return timesheetOptional.orElse(null);

    }

    @Override
    public List<TimesheetEntity> findActiveTimesheetsByDate(LocalDate today) {
        return timesheetRepository.findActiveTimesheetsByDate(today);
    }

    @Override
    public void saveAll(List<TimesheetEntity> openClockIns) {
        timesheetRepository.saveAll(openClockIns);
    }

    @Override
    public List<TimesheetEntity> getLatestLogsByTimesheetIds(List<String> memberIds, String orgId, LocalDate date) {
        return timesheetHistoryRepository.findLatestLogByTimesheet(memberIds, date);
    }

    @Override
    public TimesheetEntity save(TimesheetEntity timesheet) {
        return timesheetRepository.save(timesheet);
    }

    @Override
    public List<UserAttendanceDto> findAttendanceForUserInRange(List<String> userId, LocalDate fromDate, LocalDate toDate) {
        return timesheetRepository.findAttendanceForUsersInRange(userId, fromDate, toDate);
    }

    @Override
    public List<UserTimesheetDto> fetchUserTimesheetsWithHistory(LocalDate startDate, LocalDate endDate, List<String> userIds, String orgId) {
        String[] userIdArray = userIds.toArray(new String[0]);
        List<TimesheetUserProjection> pagedUser = userRepository.findUsersByUserIds(userIdArray);
        log.info("Found {} users", pagedUser.size());
        pagedUser.forEach(u -> log.info("User: {}, Name: {}", u.getUserId(), u.getUserName()));
        if(startDate == null) {
            log.info("Startdate before:{}", startDate);
            startDate = pagedUser.getFirst().getDate();
            log.info("Startdate:{}", startDate);
        }
        List<TimesheetUserProjection> resultList = timesheetRepository.fetchMainTimesheetsUsers(startDate, endDate, userIdArray);
        Set<Long> timesheetIdSet = resultList.stream().map(TimesheetUserProjection::getId).collect(Collectors.toSet());
        Map<Long, List<TimesheetHistoryDto>> historyMap = fetchTimesheetHistoryMap(timesheetIdSet);
        historyMap.values().forEach(list ->
                list.sort(Comparator.comparing(TimesheetHistoryDto::getTimesheetHistoryId)));
        log.info("Fetching Ws");
        TimesheetHelper.WorkScheduleResult scheduleResult = timesheetHelper.fetchWorkSchedulesAndDays(userIdArray);
        log.info("Mapping Schedules");
        Map<String, Map<DayOfWeek, FixedWorkScheduleEntity>> fixedMap = scheduleResult.getFixedMap();
        Map<String, Map<DayOfWeek, FlexibleWorkScheduleEntity>> flexMap = scheduleResult.getFlexMap();
        Map<String, Set<DayOfWeek>> userWorkingDaysMap = scheduleResult.getUserWorkingDaysMap();
        //fetch user calendarIds
        List<UserCalendarProjection> userCalendarList = userRepository.findCalendarIdsByUserIds(userIdArray);
        Map<String, String> userToCalendarMap = userCalendarList.stream()
                .collect(Collectors.toMap(UserCalendarProjection::getUserId, UserCalendarProjection::getCalendarId));
        //fetch holidays
        Set<String> calendarIds = new HashSet<>(userToCalendarMap.values());
        List<Object[]> results = calendarHolidayRepository.findHolidayDatesByCalendarIds(calendarIds);
        Map<String, List<LocalDate>> calendarHolidayMap = results.stream()
                .collect(Collectors.groupingBy(
                        row -> (String) row[0],
                        Collectors.mapping(row -> (LocalDate) row[1], Collectors.toList())
                ));
        // Map projections to DTOs grouped by userId
        log.info("Mapping mobile projections to Dtos");
        Map<String, List<TimesheetDto>> userTimesheetMap = resultList.stream()
                .map(p -> {
                    TimesheetDto dto = mapToTimesheetDto(p, historyMap);
                    LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
                    Set<DayOfWeek> workingDays = userWorkingDaysMap.getOrDefault(dto.getUserId(), Collections.emptySet());
                    boolean isWorkingDay = workingDays.contains(dto.getDate().getDayOfWeek());
                    String calendarId = userToCalendarMap.get(dto.getUserId());
                    boolean isHoliday = calendarHolidayMap.getOrDefault(calendarId, List.of()).contains(dto.getDate());
                    ScheduleTypeInfo scheduledHours = TimesheetHelper.getScheduledHoursForUser(dto.getUserId(), dto.getDate(), fixedMap, flexMap);
                    setTimesheetStatusAndDayType(dto, isWorkingDay, dto.getDate(), today, scheduledHours, isHoliday);
                    return dto;
                })
                .collect(Collectors.groupingBy(TimesheetDto::getUserId));
        // Build complete list including defaults for missing dates
        List<UserTimesheetDto> finalResponse = new ArrayList<>();
        for (TimesheetUserProjection user : pagedUser) {
            String userId = user.getUserId();
            List<TimesheetDto> userTimesheets = getUsersTimesheetsWithDefaults(
                    user,
                    userTimesheetMap.getOrDefault(userId, new ArrayList<>()),
                    startDate,
                    endDate,
                    userWorkingDaysMap.getOrDefault(userId, Collections.emptySet()),
                    fixedMap,
                    flexMap,
                    userToCalendarMap,
                    calendarHolidayMap
            );
            for (TimesheetDto t : userTimesheets) {
                finalResponse.add(new UserTimesheetDto(user.getUserName(), t));
            }
        }
        return finalResponse;
    }

        /**
         * Maps projection to DTO with history
         */
        private TimesheetDto mapToTimesheetDto(
                TimesheetUserProjection projection,
                Map<Long, List<TimesheetHistoryDto>> historyMap
        ) {
            log.info("Maps to Dtos");
            TimesheetDto dto = timesheetDtoMapper.toDto(projection);
            log.info("Mapped ClockIn={}, ClockOut={} → {} / {}",
                    projection.getFirstClockIn(),
                    projection.getLastClockOut(),
                    dto.getFirstClockInTime(),
                    dto.getLastClockOutTime());
            dto.setHistory(historyMap.getOrDefault(projection.getId(), Collections.emptyList()));
            return dto;
        }

    /**
     * Gets user timesheets with default records if none exist for flutter
     */
    private List<TimesheetDto> getUsersTimesheetsWithDefaults(
            TimesheetUserProjection user,
            List<TimesheetDto> existingTimesheets,
            LocalDate startDate,
            LocalDate endDate,
            Set<DayOfWeek> workingDays,
            Map<String, Map<DayOfWeek, FixedWorkScheduleEntity>> fixedMap,
            Map<String, Map<DayOfWeek, FlexibleWorkScheduleEntity>> flexMap,
            Map<String, String> userToCalendarMap,
            Map<String, List<LocalDate>> calendarHolidayMap
    ) {
        Map<LocalDate, TimesheetDto> existingTimesheet = existingTimesheets.stream()
                .collect(Collectors.toMap(TimesheetDto::getDate, Function.identity(),(a,b) -> a));
        log.info("User: {}, Start: {}, End: {}, Existing count: {}",
                user.getUserId(), startDate, endDate, existingTimesheets.size());
        LocalDate effectiveStart = startDate.isBefore(user.getDate())
                ? user.getDate()
                : startDate;
        log.info("Effective start: {}, User join date: {}", effectiveStart, user.getDate());
        if (effectiveStart.isAfter(endDate)) {
            log.info("User joined after requested range ({} > {}), returning empty timesheet list", effectiveStart, endDate);
            return Collections.emptyList();
        }
        List<TimesheetDto> completeList = effectiveStart.datesUntil(endDate.plusDays(1))
                .map(date -> {
                    TimesheetDto existing = existingTimesheet.get(date);
                    if (existing != null) return existing;
                    log.info("Existing timesheet for {} on {} -> {}", user.getUserId(), date, existing);
                    return createDefaultTimesheetForUserDate(
                            user,
                            date,
                            LocalDate.now(ZoneId.of("Asia/Kolkata")),
                            workingDays,
                            fixedMap,
                            flexMap,
                            userToCalendarMap,
                            calendarHolidayMap
                    );
                })
                .toList();
        log.info("Complete list size: {}", completeList.size());

        log.info("Return default timesheet");
        return completeList;
    }

    /**
     * Creates a single default timesheet record for a specific date
     */
    private TimesheetDto createDefaultTimesheetForUserDate(
            TimesheetUserProjection user,
            LocalDate date,
            LocalDate today,
            Set<DayOfWeek> workingDays,
            Map<String, Map<DayOfWeek, FixedWorkScheduleEntity>> fixedMap,
            Map<String, Map<DayOfWeek, FlexibleWorkScheduleEntity>> flexMap,
            Map<String, String> userToCalendarMap,
            Map<String, List<LocalDate>> calendarHolidayMap
    ) {
        log.info("User projection is null");

        TimesheetDto timesheet = new TimesheetDto();
        boolean isWorkingDay = workingDays.contains(date.getDayOfWeek());
        String calendarId = userToCalendarMap.get(user.getUserId());
        boolean isHoliday = calendarHolidayMap.getOrDefault(calendarId, List.of()).contains(date);
        timesheet.setDate(date);
        timesheet.setUserName(user.getUserName());
        timesheet.setFirstClockInTime("00:00");
        timesheet.setLastClockOutTime("00:00");
        timesheet.setTrackedHoursDuration("00h 00m");
        timesheet.setRegularHoursDuration("00h 00m");
        timesheet.setHistory(Collections.emptyList());

        ScheduleTypeInfo scheduledHours = TimesheetHelper.getScheduledHoursForUser(user.getUserId(), date, fixedMap, flexMap);
        setTimesheetStatusAndDayType(timesheet, isWorkingDay, date, today, scheduledHours,isHoliday);
        return timesheet;
    }

    @Override
    public void updateTimesheetHistory(Long id, LogType logType, LocalTime firstClockIn,LogFrom logFrom) {
        timesheetHistoryRepository.updateTimesheetHistory(id, logType, firstClockIn, logFrom);
    }

    @Override
    public void saveAllTimesheetHistories(List<TimesheetHistoryEntity> historyEntries) {
        timesheetHistoryRepository.saveAll(historyEntries);
    }

    @Override
    public List<UserDashboard> getDashboard(String orgId, List<String> userIds, LocalDate fromDate, LocalDate toDate) {
        return timesheetRepository.getDashboard(userIds,fromDate, toDate,orgId);
    }

    @Override
    public List<TimesheetStatusEntity> getStatus(){
        return timesheetStatusRepository.findAll();
    }

    @Override
    public Optional<TimesheetStatusEntity> findById(String status) {
        return timesheetStatusRepository.findById(status);
    }

    @Override
    public Optional<TimesheetStatusEntity> findByStatusName(String label) {
        return timesheetStatusRepository.findByStatusName(label);
    }

    @Override
    public List<TimesheetEntity> findUserByStatusId(List<String> statusId, LocalDate startDate, LocalDate endDate) {
        return timesheetRepository.findUserByStatusIdIn(statusId, startDate, endDate);
    }

    @Override
    public LocationEntity getDefaultLocation(String orgId) {
        return locationRepository.findDefaultLocationById(orgId);
    }

    @Override
    public List<String> findUserByStatusIdNotIn(LocalDate startDate, LocalDate endDate) {
        return timesheetRepository.findUserByStatusIdNotIn(startDate,endDate);
    }

    @Override
    public long countByUserIdsAndDateAndStatus(List<String> userIds, LocalDate date, String status) {
        String statusId;
        if ("PRESENT".equalsIgnoreCase(status)) {
            statusId = TimesheetStatusEnum.PRESENT.getId();
        } else if ("ABSENT".equalsIgnoreCase(status)) {
            statusId = TimesheetStatusEnum.ABSENT.getId();
        } else {
            throw new IllegalArgumentException("Unsupported status: " + status);
        }

        return timesheetRepository.countByUserIdsAndDateAndStatusId(userIds, date, statusId);
    }

    @Override
    public long countActiveUsers(String organizationId, LocalDate fromDate, LocalDate toDate) {
        return timesheetRepository.countUsersWithTimesheetsBetweenDates(organizationId, fromDate, toDate);
    }

    @Override
    public FixedWorkScheduleEntity findByWorkScheduleIdAndDay(String workScheduleId, DayOfWeekEnum day) {
        return fixedWorkScheduleRepository.findByWorkScheduleIdAndDay(workScheduleId,day);
    }

    @Override
    public FlexibleWorkScheduleEntity findByWorkScheduleIdAndDays(String workScheduleId, DayOfWeekEnum day) {
        return flexibleWorkScheduleRepository.findByWorkScheduleIdAndDay(workScheduleId,day);
    }

    @Override
    public List<TimesheetEntity> getTimesheetByUserIds(String userId, int year, int month) {
        return timesheetRepository.findByUserAndMonth(userId, year, month);
    }
}
