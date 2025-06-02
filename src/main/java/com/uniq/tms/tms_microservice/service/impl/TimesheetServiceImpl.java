package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.adapter.WorkScheduleAapter;
import com.uniq.tms.tms_microservice.dto.LogFrom;
import com.uniq.tms.tms_microservice.dto.LogType;
import com.uniq.tms.tms_microservice.dto.Privilege;
import com.uniq.tms.tms_microservice.dto.RoleName;
import com.uniq.tms.tms_microservice.dto.Timeperiod;
import com.uniq.tms.tms_microservice.dto.TimesheetDto;
import com.uniq.tms.tms_microservice.dto.TimesheetReportDto;
import com.uniq.tms.tms_microservice.dto.TimesheetStatusEnum;
import com.uniq.tms.tms_microservice.dto.UserAttendanceDto;
import com.uniq.tms.tms_microservice.dto.UserDashboard;
import com.uniq.tms.tms_microservice.dto.UserDashboardDto;
import com.uniq.tms.tms_microservice.dto.UserTimesheetDto;
import com.uniq.tms.tms_microservice.dto.UserTimesheetResponseDto;
import com.uniq.tms.tms_microservice.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.entity.TimesheetHistoryEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.entity.WorkScheduleEntity;
import com.uniq.tms.tms_microservice.mapper.RolePrivilegeMapper;
import com.uniq.tms.tms_microservice.mapper.TimesheetDtoMapper;
import com.uniq.tms.tms_microservice.mapper.TimesheetEntityMapper;
import com.uniq.tms.tms_microservice.mapper.UserEntityMapper;
import com.uniq.tms.tms_microservice.model.TimesheetHistory;
import com.uniq.tms.tms_microservice.service.TimesheetService;
import org.apache.logging.log4j.LogManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.Logger;
import static com.uniq.tms.tms_microservice.dto.TimesheetStatusEnum.*;

@Service
public class TimesheetServiceImpl implements TimesheetService {

    private static final Logger log =  LogManager.getLogger(TimesheetServiceImpl.class);

    private final TimesheetAdapter timesheetAdapter;
    private final TimesheetEntityMapper timesheetEntityMapper;
    private final TimesheetDtoMapper timesheetDtoMapper;
    private final UserAdapter userAdapter;
    private final PrivilegeService  privilegeService;
    private final UserEntityMapper userEntityMapper;
    private final WorkScheduleAapter workScheduleAdapter;

    public TimesheetServiceImpl(TimesheetAdapter timesheetAdapter, TimesheetEntityMapper timesheetEntityMapper, TimesheetDtoMapper timesheetDtoMapper, UserAdapter userAdapter, PrivilegeService privilegeService, UserEntityMapper userEntityMapper, WorkScheduleAapter workScheduleAdapter) {
        this.timesheetAdapter = timesheetAdapter;
        this.timesheetEntityMapper = timesheetEntityMapper;
        this.timesheetDtoMapper = timesheetDtoMapper;
        this.userAdapter = userAdapter;
        this.privilegeService = privilegeService;
        this.userEntityMapper = userEntityMapper;
        this.workScheduleAdapter = workScheduleAdapter;
    }

    public List<UserTimesheetResponseDto> getAllTimesheets(Long userIdFromToken, Long orgId, String role, TimesheetReportDto request) {
        LocalDate fromDate = request.getFromDate();
        LocalDate toDate = request.getToDate();
        String timePeriod = request.getTimePeriod();
        log.info("Time period: {}", timePeriod);
        List<Long> userId = request.getUserId();
        List<Long> groupIds = request.getGroupId();
        LocalDate startDate = null;
        LocalDate endDate = null;

        if (fromDate != null && !timePeriod.isEmpty()) {
            LocalDateRange range = calculateDateRange(fromDate, timePeriod);
            startDate = range.startDate();
            endDate = range.endDate();
            log.info("startDate: {}, endDate: {}", startDate, endDate);
            if(endDate.isAfter(LocalDate.now())) {
                endDate = LocalDate.now();
                log.info("endDate: {}", endDate);
            }
        }
         else if (fromDate != null && toDate != null) {
                startDate = fromDate;
                endDate = toDate;
            if(endDate.isAfter(LocalDate.now())) {
                endDate = LocalDate.now();
                log.info("endDate: {}", endDate);
            }
         }

        // Determine target users based on privileges
        List<UserEntity> targetUsers = resolveTargetUsers(userIdFromToken, groupIds, userId, orgId);

        List<Long> userIds = targetUsers.stream()
                .map(UserEntity::getUserId)
                .toList();

        // Fetch timesheets for the filtered users and date range
        List<UserTimesheetResponseDto> timesheetDtos = timesheetAdapter.filterTimesheetsForAllUsers(startDate, endDate, userIds,orgId);
        return timesheetDtos;
    }

    private List<UserEntity> resolveTargetUsers(Long userIdFromToken, List<Long> groupIds, List<Long> userId, Long orgId) {

        UserEntity currentUser = userAdapter.getUserById(userIdFromToken);
        String roleName = currentUser.getRole().getName().toUpperCase();

        //Cache mechanism for getting privileges
        Map<String, Set<String>> dbPrivileges = privilegeService.getRolePrivilegeMap();
        log.info("dbPrivileges: {}", dbPrivileges);
        Set<String> userPrivilege = dbPrivileges.entrySet().stream()
                        .filter(entry -> entry.getKey().equalsIgnoreCase(roleName.trim()))
                                .map(Map.Entry::getValue)
                                .findFirst()
                                        .orElseThrow(() -> new IllegalArgumentException("Invalid role name."));
        log.info("userPrivilege: {}", userPrivilege);


        boolean canSeeOwn = RolePrivilegeMapper.hasPrivilege(RoleName.valueOf(roleName), Privilege.CAN_SEE_OWN_TIMESHEET);
        boolean canSeeAll = RolePrivilegeMapper.hasPrivilege(RoleName.valueOf(roleName), Privilege.CAN_SEE_ALL_TIMESHEETS);
        boolean canSeeGroup = RolePrivilegeMapper.hasPrivilege(RoleName.valueOf(roleName), Privilege.CAN_SEE_GROUP_LEVEL_TIMESHEETS);

        log.info("Privileges - Own: {}, Group: {}, All: {}", canSeeOwn, canSeeGroup, canSeeAll);

        // Superadmin
        if (canSeeOwn && canSeeGroup && canSeeAll) {
            if (groupIds != null && !groupIds.isEmpty()) {
                List<UserEntity> groupUserEntities = userAdapter.findUsersByGroupIds(groupIds);
                log.info("Group user entities: {}", groupUserEntities);
                if (groupUserEntities.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "No users found in the selected group(s)");
                }

                if (userId != null && !userId.isEmpty()) {
                    // Filter group users to only include requested userIds
                    List<UserEntity> matchedUsers = groupUserEntities.stream()
                            .filter(user -> userId.contains(user.getUserId()))
                            .toList();

                    if (matchedUsers.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "No user found in the selected group(s) with the given userId(s)");
                    }

                    return matchedUsers;
                }
                return groupUserEntities;
            } else if (userId != null && !userId.isEmpty()) {
                return userAdapter.getUsersByIds(userId,currentUser.getOrganizationId());
            } else {
                return userAdapter.getAllUsers(orgId,userIdFromToken);
            }
        }

        // Admin / Manager / Staff
        if (canSeeOwn && canSeeGroup && !canSeeAll) {
            List<Long> supervisedGroupIds = userAdapter.findGroupIdsBySupervisorId(userIdFromToken);
            log.info("Supervised group ids: {}", supervisedGroupIds);
            // Case 1: If groupIds are passed → only keep supervised ones
            if (groupIds != null && !groupIds.isEmpty()) {
                List<Long> filteredGroupIds = groupIds.stream()
                        .filter(supervisedGroupIds::contains)
                        .toList();

                if (filteredGroupIds.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not supervise the selected group(s)");
                }

                List<UserEntity> groupUserEntities = userAdapter.findMembersByGroupIds(filteredGroupIds, userIdFromToken);
                log.info("Group user entities: {}", groupUserEntities);
                if (groupUserEntities.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "No users found in the selected group(s)");
                }

                List<Long> groupUserIds = groupUserEntities.stream()
                        .map(UserEntity::getUserId)
                        .toList();

                if (userId != null && !userId.isEmpty()) {
                    // Filter group users to only include requested userIds
                    List<UserEntity> matchedUsers = groupUserEntities.stream()
                            .filter(user -> userId.contains(user.getUserId()))
                            .toList();

                    if (matchedUsers.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "No user found in the selected group(s) with the given userId(s)");
                    }

                    return matchedUsers;
                }

                log.info("Requested user IDs: {}", userId);
                log.info("Group IDs passed: {}", groupIds);
                log.info("Filtered group IDs (supervised): {}", filteredGroupIds);
                log.info("Users in filtered groups: {}", groupUserIds);
                return groupUserEntities;
            } else if (userId != null && !userId.isEmpty()) {
                return userAdapter.getUsersByIds(userId, currentUser.getOrganizationId());
            } else {
                List<UserEntity> groupUserEntities = userAdapter.findMembersByGroupIds(supervisedGroupIds, userIdFromToken);
                return groupUserEntities;
            }
        }
        // Student
        if (canSeeOwn && !canSeeGroup && !canSeeAll) {
            if (groupIds != null && !groupIds.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not supervise the selected group(s)");
            }
            return List.of(currentUser);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied based on role privileges.");
    }

    private LocalDateRange calculateDateRange(LocalDate date, String timePeriod) {
        return Timeperiod.fromString(timePeriod).calculateDateRange(date);
    }

    public static record LocalDateRange(LocalDate startDate, LocalDate endDate) {
    }

    @Override
    public List<TimesheetHistory> processTimesheetLogs(List<TimesheetHistory> timesheetMiddlewareLogs) {
        List<TimesheetHistoryEntity> timesheetEntities = timesheetMiddlewareLogs.stream()
                .map(timesheetEntityMapper::toDto)
                .map(timesheetEntityMapper::toEntity)
                .toList();
        List<TimesheetHistoryEntity> savedEntities = new ArrayList<>();

        for (TimesheetHistoryEntity history : timesheetEntities) {
            if (history.getTimesheet() == null) {
                throw new IllegalArgumentException("Timesheet must not be null in TimesheetHistoryEntity.");
            }

            TimesheetEntity timesheet = history.getTimesheet();
            Long userId = timesheet.getUserId();
            LocalDate date = timesheet.getDate();

            if (userId == null) {
                throw new IllegalArgumentException("User ID must not be null. Check mapping!");
            }

            if (date == null) {
                date = history.getLoggedTimestamp().toLocalDate();
            }

            LocalDate finalDate = date;
            timesheet = timesheetAdapter.findByUserIdAndDate(userId, date)
                    .orElseGet(() -> {
                        TimesheetEntity newTimesheet = new TimesheetEntity();
                        newTimesheet.setUserId(userId);
                        newTimesheet.setDate(finalDate);
                        newTimesheet.setFirstClockIn(history.getLogType() == LogType.CLOCK_IN ? history.getLogTime() : null);
                        newTimesheet.setLastClockOut(history.getLogType() == LogType.CLOCK_OUT ? history.getLogTime() : null);
                        newTimesheet.setCreatedAt(LocalDateTime.now());
                        newTimesheet.setTrackedHours(LocalTime.of(0, 0));
                        newTimesheet.setTotalBreakHours(LocalTime.of(0, 0));
                        newTimesheet.setRegularHours(LocalTime.of(0, 0));
                        newTimesheet.setStatusId(TimesheetStatusEnum.PRESENT.getId());
                        return timesheetAdapter.saveTimesheet(newTimesheet);
                    });

            history.setTimesheet(timesheet);
            savedEntities.add(timesheetAdapter.saveTimesheetHistory(history));

            if (history.getLogType() == LogType.CLOCK_IN) {
                if (timesheet.getFirstClockIn() == null) {
                    timesheet.setFirstClockIn(history.getLogTime());
                }
            } else if (history.getLogType() == LogType.CLOCK_OUT) {
                timesheet.setLastClockOut(history.getLogTime());
            }
            timesheetAdapter.saveTimesheet(timesheet);
        }
        timesheetAdapter.calculateTrackedAndBreakHours(savedEntities);

        return savedEntities.stream()
                .map(timesheetEntityMapper::toMiddleware)
                .toList();
    }

    @Override
    public TimesheetDto updateClockInOut(Long userId, LocalDate date, TimesheetDto request) {
        TimesheetEntity timesheet = timesheetAdapter.findUserIdAndDate(userId, date);
        boolean isNew = false;

        if (timesheet == null) {
            isNew = true;
            timesheet = new TimesheetEntity();
            timesheet.setUserId(userId);
            timesheet.setDate(date);
            timesheet.setCreatedAt(LocalDateTime.now());
            timesheet.setFirstClockIn(null);
            timesheet.setLastClockOut(null);
            timesheet.setTrackedHours(null);
            timesheet.setRegularHours(null);
            timesheet.setTotalBreakHours(null);
        }

        // Update entity fields
        if (request.getFirstClockIn() != null) {
            timesheet.setFirstClockIn(request.getFirstClockIn());
            timesheet.setStatusId(TimesheetStatusEnum.PRESENT.getId());
        }

        if (request.getLastClockOut() != null) {
            timesheet.setLastClockOut(request.getLastClockOut());
        }

        if (PAID_LEAVE.getLabel().equalsIgnoreCase(request.getStatus())) {
            timesheet.setStatusId(PAID_LEAVE.getId());
        } else if (HALF_DAY.getLabel().equalsIgnoreCase(request.getStatus())) {
            timesheet.setStatusId(HALF_DAY.getId());
        } else if (PERMISSION.getLabel().equalsIgnoreCase(request.getStatus())) {
            timesheet.setStatusId(PERMISSION.getId());
        }

        calculateHours(timesheet);
        timesheet = timesheetAdapter.save(timesheet);

        // Handle TimesheetHistory only for new timesheet
        if (isNew) {
            if (request.getFirstClockIn() != null) {
                TimesheetHistoryEntity clockInHistory = new TimesheetHistoryEntity();
                clockInHistory.setTimesheet(timesheet);
                clockInHistory.setLogTime(request.getFirstClockIn());
                clockInHistory.setLogType(LogType.CLOCK_IN);
                clockInHistory.setLogFrom(LogFrom.WEB_APP);
                clockInHistory.setLocationId(0L); // Update if location is dynamic
                clockInHistory.setLoggedTimestamp(LocalDateTime.now());
                timesheetAdapter.saveTimesheetHistory(clockInHistory);
            }

            if (request.getLastClockOut() != null) {
                TimesheetHistoryEntity clockOutHistory = new TimesheetHistoryEntity();
                clockOutHistory.setTimesheet(timesheet);
                clockOutHistory.setLogTime(request.getLastClockOut());
                clockOutHistory.setLogType(LogType.CLOCK_OUT);
                clockOutHistory.setLogFrom(LogFrom.WEB_APP);
                clockOutHistory.setLocationId(0L);
                clockOutHistory.setLoggedTimestamp(LocalDateTime.now());
                timesheetAdapter.saveTimesheetHistory(clockOutHistory);
            }
        } else {
            // Optional: update existing history rows for clock-in/out if needed
            if (request.getFirstClockIn() != null) {
                timesheetAdapter.updateTimesheetHistory(
                        timesheet.getId(), LogType.CLOCK_IN, request.getFirstClockIn());
            }

            if (request.getLastClockOut() != null) {
                timesheetAdapter.updateTimesheetHistory(
                        timesheet.getId(), LogType.CLOCK_OUT, request.getLastClockOut());
            }
        }

        return timesheetDtoMapper.toDto(timesheet);
    }


    private void calculateHours(TimesheetEntity timesheet) {
        try {
            if (timesheet.getFirstClockIn() != null && timesheet.getLastClockOut() != null) {
                if (!timesheet.getLastClockOut().isBefore(timesheet.getFirstClockIn())) {
                    Duration workedDuration = Duration.between(timesheet.getFirstClockIn(), timesheet.getLastClockOut());
                    log.info("Worked duration: {}", workedDuration);
                    timesheet.setRegularHours(LocalTime.ofSecondOfDay(workedDuration.toSeconds()));
                    log.info("Regular hours: {}", timesheet.getRegularHours());
                    timesheet.setTrackedHours(LocalTime.ofSecondOfDay(workedDuration.toSeconds()));
                    log.info("Tracked hours: {}", timesheet.getTrackedHours());
                }
                else {
                    log.warn("Invalid clock-out time: earlier than clock-in.");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void autoClockOut() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<TimesheetEntity> openClockIns = timesheetAdapter.findActiveTimesheetsByDate(yesterday);
        List<TimesheetHistoryEntity> historyEntries = new ArrayList<>();

        log.info("Timesheets fetched for {}: {}", yesterday, openClockIns.size());

        for (TimesheetEntity entry : openClockIns) {
            if (entry.getFirstClockIn() != null && entry.getLastClockOut() == null) {
                log.info("Auto clock-out for userId={}, setting lastClockOut to 23:59", entry.getUserId());

                // 1. Set lastClockOut
                entry.setLastClockOut(LocalTime.of(23, 59));
                calculateHours(entry);

                // 2. Create history entry
                TimesheetHistoryEntity history = new TimesheetHistoryEntity();
                history.setLocationId(0L);
                history.setLogTime(LocalTime.of(23, 59));
                history.setLogType(LogType.CLOCK_OUT);
                history.setLogFrom(LogFrom.SYSTEM_GENERATED);
                history.setLoggedTimestamp(LocalDateTime.now());

                // Properly reference timesheet
                TimesheetEntity timesheetRef = new TimesheetEntity();
                timesheetRef.setId(entry.getId());
                history.setTimesheet(timesheetRef);

                historyEntries.add(history);

                log.info("Added SYSTEM_GENERATED CLOCK_OUT history for userId={}, date={}", entry.getUserId(), entry.getDate());
            }
        }

        log.info("Saving updated timesheets and history entries...");
        timesheetAdapter.saveAll(openClockIns);
        timesheetAdapter.saveAllTimesheetHistories(historyEntries);
    }

    @Override
    public List<UserDashboardDto> getAllUserInfo(Long orgId, Long userIdFromToken, LocalDate fromDate, LocalDate toDate, Long userId) {
        UserEntity currentUser = userAdapter.getUserById(userIdFromToken);
        String roleName = currentUser.getRole().getName().toUpperCase();

        boolean canSeeOwn = RolePrivilegeMapper.hasPrivilege(RoleName.valueOf(roleName), Privilege.CAN_SEE_OWN_TIMESHEET);
        boolean canSeeAll = RolePrivilegeMapper.hasPrivilege(RoleName.valueOf(roleName), Privilege.CAN_SEE_ALL_TIMESHEETS);
        boolean canSeeGroup = RolePrivilegeMapper.hasPrivilege(RoleName.valueOf(roleName), Privilege.CAN_SEE_GROUP_LEVEL_TIMESHEETS);

        List<UserDashboard> activeUsers;
        List<Long> userIdsForAttendance;

        if (canSeeOwn && canSeeAll && canSeeGroup) {
            // Full access (Superadmin or similar)
            if (userId != null) {
                timesheetAdapter.findAllByOrgIdExcludingUser(orgId, Collections.emptyList(), fromDate, toDate, true, userIdFromToken, userId);
                userIdsForAttendance = Collections.singletonList(userId);
            } else {
                activeUsers = timesheetAdapter.findAllByOrgIdExcludingUser(orgId, Collections.emptyList(), fromDate, toDate, true, userIdFromToken, userId);
                userIdsForAttendance = activeUsers.stream()
                        .map(UserDashboard::getUserId)
                        .toList();
            }
        } else if (canSeeGroup) {
            if (userId != null) {
                timesheetAdapter.findAllByOrgIdExcludingUser(orgId, Collections.emptyList(), fromDate, toDate, true, userIdFromToken, userId);
                userIdsForAttendance = Collections.singletonList(userId);
            } else {
                List<Long> supervisedGroupIds = userAdapter.findGroupIdsBySupervisorId(userIdFromToken);
                if (supervisedGroupIds.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not supervising any groups.");
                }

                List<UserEntity> groupMembers = userAdapter.findMembersByGroupIds(supervisedGroupIds, userIdFromToken);
                List<Long> groupMemberIds = groupMembers.stream()
                        .map(UserEntity::getUserId)
                        .toList();

                log.info("Group member IDs: {}", groupMemberIds);
                if (groupMemberIds.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found in your supervised groups.");
                }

                timesheetAdapter.findAllByOrgIdExcludingUser(orgId, groupMemberIds, fromDate, toDate, false, userIdFromToken, null);
                userIdsForAttendance = groupMemberIds;
            }
        } else {
            // Limited access, only own or no privilege to see others
            timesheetAdapter.findAllByOrgIdExcludingUser(orgId, Collections.emptyList(), fromDate, toDate, false, userIdFromToken, userId);
            userIdsForAttendance = userId != null ? Collections.singletonList(userId) : Collections.singletonList(userIdFromToken);
        }

        // Calculate attendance summary for the collected userIds
        return calculateAttendanceSummaryForUsers(userIdsForAttendance, fromDate, toDate, orgId);
    }


    private List<UserDashboardDto> calculateAttendanceSummaryForUsers(List<Long> userIds, LocalDate fromDate, LocalDate toDate, Long orgId) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Fetch attendance for all users in the list
        List<UserAttendanceDto> attendanceList = timesheetAdapter.findAttendanceForUserInRange(userIds, fromDate, toDate);

        // Build a map of userId|date to statusId
        Map<String, Long> attendanceMap = new HashMap<>();
        for (UserAttendanceDto dto : attendanceList) {
            String key = dto.getUserId() + "|" + dto.getDate();
            attendanceMap.put(key, dto.getStatusId());
        }

        WorkScheduleEntity defaultWs = workScheduleAdapter.findDefaultActiveSchedule(orgId);
        DayOfWeek restDay = DayOfWeek.valueOf(defaultWs.getRestDay().toUpperCase());

        int present = 0, absent = 0, paidLeave = 0, notMarked = 0, holiday = 0, halfDay = 0, permission = 0;
        int total = 0;

        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            DayOfWeek day = date.getDayOfWeek();
            boolean isRestDay = day.equals(restDay);
            boolean isToday = date.equals(LocalDate.now());

            for (Long userId : userIds) {
                total++;
                String key = userId + "|" + date;

                if (isRestDay) {
                    Long statusId = attendanceMap.get(key);
                    if (statusId != null && statusId == 1L) {
                        present++; // person worked on rest day
                        // Don't count this as holiday
                    } else {
                        holiday++; // true holiday (rest day, not worked)
                    }
                    continue; // skip further checks for rest days
                }

                Long statusId = attendanceMap.get(key);
                if (statusId == null) {
                    if (isToday) {
                        notMarked++;
                    } else {
                        absent++;
                    }
                } else {
                    switch (statusId.intValue()) {
                        case 1 -> present++;
                        case 2 -> absent++;
                        case 3 -> paidLeave++;
                        case 4 -> notMarked++;
                        case 6 -> halfDay++;
                        case 7 -> permission++;
                    }
                }
            }
        }

        UserDashboardDto summary = new UserDashboardDto();
        summary.setPresentCount(present);
        summary.setAbsentCount(absent);
        summary.setPaidLeaveCount(paidLeave);
        summary.setNotMarkedCount(notMarked);
        summary.setHalfDayCount(halfDay);
        summary.setPermissionCount(permission);
        summary.setHolidayCount(holiday);
        summary.setTotalCount(total);

        int totalDays = (int) ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        log.info("totalDays: {}", totalDays);
        double totalCount = totalDays * userIds.size();
        log.info("totalCount: {}", totalCount);
        double totalCountPercentage = totalCount > 0 ? totalCount : 1.0;
        log.info("totalCountPercentage: {}", totalCountPercentage);

        summary.setPresentPercentage(Double.parseDouble(formatToDecimal(present / totalCountPercentage * 100.0)));
        summary.setAbsentPercentage(Double.parseDouble(formatToDecimal(absent / totalCountPercentage * 100.0)));
        summary.setPaidLeavePercentage(Double.parseDouble(formatToDecimal(paidLeave / totalCountPercentage * 100.0)));
        summary.setNotMarkedPercentage(Double.parseDouble(formatToDecimal(notMarked / totalCountPercentage * 100.0)));
        summary.setHolidayPercentage(Double.parseDouble(formatToDecimal(holiday / totalCountPercentage * 100.0)));
        summary.setHalfDayPercentage(Double.parseDouble(formatToDecimal(halfDay / totalCountPercentage * 100.0)));
        summary.setPermissionPercentage(Double.parseDouble(formatToDecimal(permission / totalCountPercentage * 100.0)));
        return Collections.singletonList(summary);
    }

    private String formatToDecimal(double value) {
        DecimalFormat df = new DecimalFormat("0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(value);
    }

    @Override
    public List<UserTimesheetDto> getUserTimesheets(Long userIdFromToken, Long orgId, String role, TimesheetReportDto request) {
        LocalDate fromDate = request.getFromDate();
        LocalDate toDate = request.getToDate();
        String timePeriod = request.getTimePeriod();
        List<Long> userIds = request.getUserId();

        LocalDate startDate = null;
        LocalDate endDate = null;

        if (fromDate != null && timePeriod != null) {
            LocalDateRange range = calculateDateRange(fromDate, timePeriod);
            startDate = range.startDate();
            endDate = range.endDate();
            log.info("startDate: {}, endDate: {}", startDate, endDate);
            if(endDate.isAfter(LocalDate.now())) {
                endDate = LocalDate.now();
                log.info("endDate: {}", endDate);
            }
        }
        else if (fromDate != null && toDate != null) {
            startDate = fromDate;
            endDate = toDate;
            log.info("startDate: {}, endDate: {}", startDate, endDate);
            if(endDate.isAfter(LocalDate.now())) {
                endDate = LocalDate.now();
                log.info("endDate: {}", endDate);
            }
        } else if (fromDate == null && toDate == null && userIds != null) {
            endDate = LocalDate.now();
        }

        List<UserTimesheetDto> rawResults = timesheetAdapter.fetchUserTimesheetsWithHistory(startDate, endDate, userIds, orgId);
        return rawResults;
    }

}
