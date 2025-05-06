package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.dto.RoleName;
import com.uniq.tms.tms_microservice.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.entity.TimesheetHistoryEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.mapper.RolePrivilegeMapper;
import com.uniq.tms.tms_microservice.mapper.TimesheetDtoMapper;
import com.uniq.tms.tms_microservice.mapper.TimesheetEntityMapper;
import com.uniq.tms.tms_microservice.mapper.UserEntityMapper;
import com.uniq.tms.tms_microservice.model.TimesheetHistory;
import com.uniq.tms.tms_microservice.service.TimesheetService;
import org.apache.logging.log4j.LogManager;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.Logger;

@Service
public class TimesheetServiceImpl implements TimesheetService {

    private static final Logger log =  LogManager.getLogger(TimesheetServiceImpl.class);

    private final TimesheetAdapter timesheetAdapter;
    private final TimesheetEntityMapper timesheetEntityMapper;
    private final TimesheetDtoMapper timesheetDtoMapper;
    private final UserAdapter userAdapter;
    private final PrivilegeService  privilegeService;
    private final UserEntityMapper userEntityMapper;

    public TimesheetServiceImpl(TimesheetAdapter timesheetAdapter, TimesheetEntityMapper timesheetEntityMapper, TimesheetDtoMapper timesheetDtoMapper, UserAdapter userAdapter, PrivilegeService privilegeService, UserEntityMapper userEntityMapper) {
        this.timesheetAdapter = timesheetAdapter;
        this.timesheetEntityMapper = timesheetEntityMapper;
        this.timesheetDtoMapper = timesheetDtoMapper;
        this.userAdapter = userAdapter;
        this.privilegeService = privilegeService;
        this.userEntityMapper = userEntityMapper;
    }

    public List<TimesheetDto> getAllTimesheets(Long userIdFromToken, Long orgId, String role, TimesheetReportDto request) {
        LocalDate fromDate = request.getFromDate();
        LocalDate toDate = request.getToDate();
        String timePeriod = request.getTimePeriod();
        List<Long> userId = request.getUserId();
        List<Long> groupIds = request.getGroupId();
        LocalDate startDate = null;
        LocalDate endDate = null;

        if (fromDate != null && timePeriod != null) {
            LocalDateRange range = calculateDateRange(fromDate, timePeriod);
            startDate = range.startDate();
            endDate = range.endDate();
        }
         else if (fromDate != null && toDate != null) {
                startDate = fromDate;
                endDate = toDate;
            }
        else if (userId != null) {
            startDate = LocalDate.of(2025, 1, 1);
            endDate = LocalDate.now();
        }

        // Determine target users based on privileges
        List<UserEntity> targetUsers = resolveTargetUsers(userIdFromToken, groupIds, userId);

        List<Long> userIds = targetUsers.stream()
                .map(UserEntity::getUserId)
                .toList();

        // Fetch timesheets for the filtered users and date range
        List<TimesheetDto> timesheetDtos = timesheetAdapter.filterTimesheetsForAllUsers(startDate, endDate, userIds);
        return timesheetDtos;
    }

    private List<UserEntity> resolveTargetUsers(Long userIdFromToken, List<Long> groupIds, List<Long> userId) {

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
                return userAdapter.findUsersByGroupIds(groupIds); // includes members + all supervisors
            } else if (userIdFromToken != null) {
                return List.of(userAdapter.getUserById(userIdFromToken));
            } else {
                return userAdapter.getAllUsers();
            }
        }

        // Admin / Manager / Staff
        if (canSeeOwn && canSeeGroup && !canSeeAll) {
            List<Long> supervisedGroupIds = userAdapter.findGroupIdsBySupervisorId(userIdFromToken);

            // Case 1: If groupIds are passed → only keep supervised ones
            if (groupIds != null && !groupIds.isEmpty()) {
                List<Long> filteredGroupIds = groupIds.stream()
                        .filter(supervisedGroupIds::contains)
                        .toList();

                if (filteredGroupIds.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not supervise the selected group(s)");
                }

                List<UserEntity> groupUserEntities = userAdapter.findMembersByGroupIds(filteredGroupIds, userIdFromToken);

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
                return List.of(userAdapter.getUserById(userIdFromToken));
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

        if (timesheet == null) {
            timesheet = new TimesheetEntity();
            timesheet.setUserId(userId);
            timesheet.setDate(date);
            timesheet.setCreatedAt(LocalDateTime.now());
        }

        if (request.getFirstClockIn() != null) {
            timesheet.setFirstClockIn(request.getFirstClockIn());
        }
        if (request.getLastClockOut() != null) {
            timesheet.setLastClockOut(request.getLastClockOut());
        }

        calculateHours(timesheet);

        timesheet = timesheetAdapter.save(timesheet);

        return timesheetDtoMapper.toDto(timesheet);
    }

    private void calculateHours(TimesheetEntity timesheet) {
        if (timesheet.getFirstClockIn() != null && timesheet.getLastClockOut() != null) {
            Duration workedDuration = Duration.between(timesheet.getFirstClockIn(), timesheet.getLastClockOut());
            timesheet.setRegularHours(LocalTime.ofSecondOfDay(workedDuration.toSeconds()));
            timesheet.setTrackedHours(LocalTime.ofSecondOfDay(workedDuration.toSeconds()));
        }
    }

    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    public void autoClockOutForAllEmployees() {
        LocalDate today = LocalDate.now();

        List<TimesheetEntity> openClockIns = timesheetAdapter
                .findActiveTimesheetsByDate(today);

        for (TimesheetEntity entry : openClockIns) {
            entry.setLastClockOut(LocalTime.MIDNIGHT);
            calculateHours(entry);
        }
        timesheetAdapter.saveAll(openClockIns);
    }
}
