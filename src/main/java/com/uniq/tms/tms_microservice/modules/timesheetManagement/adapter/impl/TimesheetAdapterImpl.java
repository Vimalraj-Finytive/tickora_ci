package com.uniq.tms.tms_microservice.modules.timesheetManagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.locationManagement.entity.LocationEntity;
import com.uniq.tms.tms_microservice.modules.locationManagement.repository.LocationRepository;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.*;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.repository.TimesheetStatusRepository;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.LogFrom;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.LogType;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.TimesheetStatusEnum;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.TimesheetWorkStatusEnum;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.model.ScheduleTypeInfo;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
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

    private final TimesheetRepository timesheetRepository;
    private final TimesheetHistoryRepository timesheetHistoryRepository;
    private final TimesheetStatusRepository timesheetStatusRepository;
    private final LocationRepository locationRepository;
    private final TimesheetDtoMapper timesheetDtoMapper;
    private final UserRepository userRepository;
    private final TimesheetHelper timesheetHelper;

    public TimesheetAdapterImpl(TimesheetRepository timesheetRepository, TimesheetHistoryRepository timesheetHistoryRepository,
                                TimesheetStatusRepository timesheetStatusRepository, LocationRepository locationRepository,
                                TimesheetDtoMapper timesheetDtoMapper, UserRepository userRepository, TimesheetHelper timesheetHelper) {
        this.timesheetRepository = timesheetRepository;
        this.timesheetHistoryRepository = timesheetHistoryRepository;
        this.timesheetStatusRepository = timesheetStatusRepository;
        this.locationRepository = locationRepository;
        this.timesheetDtoMapper = timesheetDtoMapper;
        this.userRepository = userRepository;
        this.timesheetHelper = timesheetHelper;
    }

    @Value("${timesheet.extra.worked.minutes}")
    private int extraWorkedMinutes;

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

        log.info("Fetch WorkSchedules for all users in date range");
        TimesheetHelper.WorkScheduleResult result = timesheetHelper.fetchWorkSchedulesAndDays(userIdArrays);
        Map<String, Map<DayOfWeek, FixedWorkScheduleEntity>> fixedMap = result.getFixedMap();
        Map<String, Map<DayOfWeek, FlexibleWorkScheduleEntity>> flexMap = result.getFlexMap();
        Map<String, Set<DayOfWeek>> userWorkingDaysMap = result.getUserWorkingDaysMap();

        log.info("Get all users timesheet");
        Map<String, List<TimesheetDto>> userTimesheetMap = fetchAndMapTimesheets(startDate, endDate, userIdArrays, userGroups, fixedMap, flexMap, userWorkingDaysMap);

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
                    flexMap
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
            Map<String, Set<DayOfWeek>> userWorkingDaysMap
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
        log.info("Return main projections");
        return mainProjections.stream()
                .map(p -> {
                    TimesheetDto dto = mapToTimesheetDto(p, userGroups, historyMap);
                    LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
                    Set<DayOfWeek> wokingDays = userWorkingDaysMap.getOrDefault(dto.getUserId(),
                            Collections.emptySet());
                    boolean isWorkingDay = wokingDays.contains(dto.getDate().getDayOfWeek());
                    ScheduleTypeInfo scheduledHours = TimesheetHelper.getScheduledHoursForUser(dto.getUserId(), dto.getDate(), fixedMap, flexMap);
                    setTimesheetStatusAndDayType(dto, isWorkingDay, dto.getDate(), today, scheduledHours);
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
            Map<String, Map<DayOfWeek, FlexibleWorkScheduleEntity>> flexMap
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
                            flexMap
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
            Map<String, Map<DayOfWeek, FlexibleWorkScheduleEntity>> flexMap
    ) {
        log.info("User projection is null");
        TimesheetDto timesheet = new TimesheetDto();
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
        timesheet.setHistory(Collections.emptyList());

        ScheduleTypeInfo scheduledHours = TimesheetHelper.getScheduledHoursForUser(user.getUserId(), date, fixedMap, flexMap);
        setTimesheetStatusAndDayType(timesheet, isWorkingDay, date, today, scheduledHours);
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
            ScheduleTypeInfo scheduledHours
    ) {
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
        if (!isWorkingDay) {
            timesheet.setUserDayType(TimesheetStatusEnum.HOLIDAY.getLabel());
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
                timesheet.setStatus(TimesheetStatusEnum.HOLIDAY.getLabel());
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
            Duration overtimeThreshold = scheduledHours.plusMinutes(extraWorkedMinutes);

            if (worked.compareTo(overtimeThreshold) > 0) {
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
            boolean isOvertime = actualEnd.isAfter(fixedEndTime.plusMinutes(30));


            if (!isLateClockIn && isOvertime && hasOvertimeHours) {
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

    private Long toLong(Object obj) {
        try {
            return obj != null ? Long.parseLong(obj.toString()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String toString(Object obj) {
        try {
            return obj != null ? obj.toString() : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate toLocalDate(Object obj) {
        if (obj instanceof Date) {
            return ((Date) obj).toLocalDate();
        } else if (obj instanceof Instant) {
            return ((Instant) obj).atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }

    private LocalTime toLocalTime(Object obj) {
        if (obj instanceof Time) {
            return ((Time) obj).toLocalTime();
        }
        return null;
    }

    private LocalDateTime toLocalDateTime(Object obj) {
        if (obj instanceof Timestamp) {
            return ((Timestamp) obj).toLocalDateTime();
        }
        return null;
    }

    private Duration parseTimeToDuration(Object timeObj, String label) {
        if (timeObj == null) return Duration.ZERO;

        try {
            if (timeObj instanceof String) {
                String intervalStr = (String) timeObj;
                // Parse PostgreSQL INTERVAL string format: "01:30:00"
                LocalTime lt = LocalTime.parse(intervalStr);
                return Duration.ofHours(lt.getHour()).plusMinutes(lt.getMinute()).plusSeconds(lt.getSecond());
            } else {
                System.err.println("Unexpected type for " + label + ": " + timeObj.getClass());
            }
        } catch (Exception e) {
            System.err.println("Error parsing " + label + ": " + timeObj);
            e.printStackTrace();
        }
        return Duration.ZERO;
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
        Map<Long, List<TimesheetHistoryEntity>> groupedLogs = savedLogs.stream()
                .filter(log -> log.getTimesheet() != null)
                .collect(Collectors.groupingBy(log -> log.getTimesheet().getId()));

        for (Map.Entry<Long, List<TimesheetHistoryEntity>> entry : groupedLogs.entrySet()) {
            Long timesheetId = entry.getKey();
            List<TimesheetHistoryEntity> historyLogs = entry.getValue();

            historyLogs.sort(Comparator.comparing(TimesheetHistoryEntity::getLoggedTimestamp));

            TimesheetEntity timesheet = timesheetRepository.findById(timesheetId)
                    .orElseThrow(() -> new RuntimeException("Timesheet not found for ID: " + timesheetId));

            long trackedSeconds = Optional.ofNullable(timesheet.getTrackedHours()).orElse(LocalTime.MIN).toSecondOfDay();
            long breakSeconds = Optional.ofNullable(timesheet.getTotalBreakHours()).orElse(LocalTime.MIN).toSecondOfDay();
            long regularSeconds = trackedSeconds;

            LocalTime lastClockIn = Optional.ofNullable(timesheet.getFirstClockIn()).orElse(null);
            LocalTime lastClockOut = Optional.ofNullable(timesheet.getLastClockOut()).orElse(null);

            for (TimesheetHistoryEntity log : historyLogs) {
                if (log.getLogType() == LogType.CLOCK_IN) {
                    if (lastClockOut != null) {
                        long breakTime = Duration.between(lastClockOut, log.getLogTime()).getSeconds();
                        if (breakTime > 0) {
                            breakSeconds += breakTime;
                        }
                    }
                    lastClockIn = log.getLogTime();
                } else if (log.getLogType() == LogType.CLOCK_OUT && lastClockIn != null) {
                    long trackedTime = Duration.between(lastClockIn, log.getLogTime()).getSeconds();
                    if (trackedTime > 0) {
                        trackedSeconds += trackedTime;
                        regularSeconds += trackedTime;
                    }
                    lastClockOut = log.getLogTime();
                }
            }
            timesheet.setTrackedHours(LocalTime.ofSecondOfDay(trackedSeconds));
            timesheet.setRegularHours(LocalTime.ofSecondOfDay(regularSeconds));
            timesheet.setTotalBreakHours(LocalTime.ofSecondOfDay(breakSeconds));
            timesheet.setLastClockOut(lastClockOut);
            timesheetRepository.save(timesheet);
        }
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
        // Fetch users
        List<TimesheetProjection> pagedUser = userRepository.findUsersByUserIds(userIdArray);
        log.info("Found {} users", pagedUser.size());
        pagedUser.forEach(u -> log.info("User: {}, Name: {}", u.getUserId(), u.getUserName()));
        if(startDate == null) {
            log.info("Startdate before:{}", startDate);
            startDate = pagedUser.getFirst().getDate();
            log.info("Startdate:{}", startDate);
        }
        //Fetch main timesheets
        List<TimesheetProjection> resultList = timesheetRepository.fetchMainTimesheets(startDate, endDate, userIdArray);

        // Prepare helper maps
        Set<Long> timesheetIdSet = resultList.stream().map(TimesheetProjection::getId).collect(Collectors.toSet());
        Map<Long, List<TimesheetHistoryDto>> historyMap = fetchTimesheetHistoryMap(timesheetIdSet);
        log.info("Fetching Ws");
        TimesheetHelper.WorkScheduleResult scheduleResult = timesheetHelper.fetchWorkSchedulesAndDays(userIdArray);
        log.info("Mapping Schules");
        Map<String, Map<DayOfWeek, FixedWorkScheduleEntity>> fixedMap = scheduleResult.getFixedMap();
        Map<String, Map<DayOfWeek, FlexibleWorkScheduleEntity>> flexMap = scheduleResult.getFlexMap();
        Map<String, Set<DayOfWeek>> userWorkingDaysMap = scheduleResult.getUserWorkingDaysMap();

        // Map projections to DTOs grouped by userId
        log.info("Mapping mobile projections to Dtos");
        Map<String, List<TimesheetDto>> userTimesheetMap = resultList.stream()
                .map(p -> {
                    TimesheetDto dto = mapToTimesheetDto(p, historyMap);
                    LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
                    Set<DayOfWeek> workingDays = userWorkingDaysMap.getOrDefault(dto.getUserId(), Collections.emptySet());
                    boolean isWorkingDay = workingDays.contains(dto.getDate().getDayOfWeek());
                    ScheduleTypeInfo scheduledHours = TimesheetHelper.getScheduledHoursForUser(dto.getUserId(), dto.getDate(), fixedMap, flexMap);
                    setTimesheetStatusAndDayType(dto, isWorkingDay, dto.getDate(), today, scheduledHours);
                    return dto;
                })
                .collect(Collectors.groupingBy(TimesheetDto::getUserId));

        // Build complete list including defaults for missing dates
        List<UserTimesheetDto> finalResponse = new ArrayList<>();
        for (TimesheetProjection user : pagedUser) {
            String userId = user.getUserId();

            List<TimesheetDto> userTimesheets = getUsersTimesheetsWithDefaults(
                    user,
                    userTimesheetMap.getOrDefault(userId, new ArrayList<>()),
                    startDate,
                    endDate,
                    userWorkingDaysMap.getOrDefault(userId, Collections.emptySet()),
                    fixedMap,
                    flexMap
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
            TimesheetProjection projection,
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
            TimesheetProjection user,
            List<TimesheetDto> existingTimesheets,
            LocalDate startDate,
            LocalDate endDate,
            Set<DayOfWeek> workingDays,
            Map<String, Map<DayOfWeek, FixedWorkScheduleEntity>> fixedMap,
            Map<String, Map<DayOfWeek, FlexibleWorkScheduleEntity>> flexMap
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
                            flexMap
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
            TimesheetProjection user,
            LocalDate date,
            LocalDate today,
            Set<DayOfWeek> workingDays,
            Map<String, Map<DayOfWeek, FixedWorkScheduleEntity>> fixedMap,
            Map<String, Map<DayOfWeek, FlexibleWorkScheduleEntity>> flexMap
    ) {
        log.info("User projection is null");

        TimesheetDto timesheet = new TimesheetDto();
        boolean isWorkingDay = workingDays.contains(date.getDayOfWeek());

        timesheet.setDate(date);
        timesheet.setUserName(user.getUserName());
        timesheet.setWorkScheduleName(user.getWorkScheduleName() != null ? user.getWorkScheduleName() : "null");
        timesheet.setFirstClockInTime("00:00");
        timesheet.setLastClockOutTime("00:00");
        timesheet.setTrackedHoursDuration("00h 00m");
        timesheet.setRegularHoursDuration("00h 00m");
        timesheet.setHistory(Collections.emptyList());

        ScheduleTypeInfo scheduledHours = TimesheetHelper.getScheduledHoursForUser(user.getUserId(), date, fixedMap, flexMap);
        setTimesheetStatusAndDayType(timesheet, isWorkingDay, date, today, scheduledHours);
        return timesheet;
    }

    @Override
    public void updateTimesheetHistory(Long id, LogType logType, LocalTime firstClockIn) {
        timesheetHistoryRepository.updateTimesheetHistory(id, logType, firstClockIn);
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

}
