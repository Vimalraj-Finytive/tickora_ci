package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.dto.LogType;
import com.uniq.tms.tms_microservice.dto.Privilege;
import com.uniq.tms.tms_microservice.dto.Role;
import com.uniq.tms.tms_microservice.dto.Timeperiod;
import com.uniq.tms.tms_microservice.dto.TimesheetDto;
import com.uniq.tms.tms_microservice.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.entity.TimesheetHistoryEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.mapper.RolePrivilegeMapper;
import com.uniq.tms.tms_microservice.mapper.TimesheetDtoMapper;
import com.uniq.tms.tms_microservice.mapper.TimesheetEntityMapper;
import com.uniq.tms.tms_microservice.model.TimesheetHistory;
import com.uniq.tms.tms_microservice.service.TimesheetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Service
public class TimesheetServiceImpl implements TimesheetService {

    private final TimesheetAdapter timesheetAdapter;
    private final TimesheetEntityMapper timesheetEntityMapper;
    private final TimesheetDtoMapper timesheetDtoMapper;
    private final UserAdapter userAdapter;

    public TimesheetServiceImpl(TimesheetAdapter timesheetAdapter, TimesheetEntityMapper timesheetEntityMapper, TimesheetDtoMapper timesheetDtoMapper, UserAdapter userAdapter) {
        this.timesheetAdapter = timesheetAdapter;
        this.timesheetEntityMapper = timesheetEntityMapper;
        this.timesheetDtoMapper = timesheetDtoMapper;
        this.userAdapter = userAdapter;
    }

    private static final Logger log = LoggerFactory.getLogger(TimesheetServiceImpl.class);

    public List<TimesheetDto> getAllTimesheets(Long userIdFromToken, String role, LocalDate date, String timePeriod, Long userId, List<Long> groupIds) {
        LocalDate startDate = null;
        LocalDate endDate = null;

        if (date != null && timePeriod != null) {
            LocalDateRange range = calculateDateRange(date, timePeriod);
            startDate = range.startDate();
            endDate = range.endDate();
        } else if (userId != null) {
            startDate = LocalDate.of(2025, 1, 1);
            endDate = LocalDate.now();
        }

        // Determine target users based on privileges
        List<UserEntity> targetUsers = (userId != null)
                ? List.of(userAdapter.getUserById(userId))
                : resolveTargetUsers(userIdFromToken, groupIds);

        List<Long> userIds = targetUsers.stream()
                .map(UserEntity::getUserId)
                .toList();

        // Fetch timesheets for the filtered users and date range
        List<TimesheetDto> timesheetDtos = timesheetAdapter.filterTimesheetsForAllUsers(startDate, endDate, userIds);
        return timesheetDtos;
    }

    private List<UserEntity> resolveTargetUsers(Long userIdFromToken, List<Long> groupIds) {

        UserEntity currentUser = userAdapter.getUserById(userIdFromToken);
        String roleName = currentUser.getRole().getName().toUpperCase();

        boolean canSeeOwn = RolePrivilegeMapper.hasPrivilege(Role.valueOf(roleName), Privilege.CAN_SEE_OWN_TIMESHEET);
        boolean canSeeAll = RolePrivilegeMapper.hasPrivilege(Role.valueOf(roleName), Privilege.CAN_SEE_ALL_TIMESHEETS);
        boolean canSeeGroup = RolePrivilegeMapper.hasPrivilege(Role.valueOf(roleName), Privilege.CAN_SEE_GROUP_LEVEL_TIMESHEETS);

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
            // If groupIds are passed → get groups supervised by the logged-in user
            if (groupIds != null && !groupIds.isEmpty()) {
                List<Long> supervisedGroupIds = userAdapter.findGroupIdsBySupervisorId(userIdFromToken);
                // Keep only groups the logged-in user actually supervises
                List<Long> filteredGroupIds = groupIds.stream()
                        .filter(supervisedGroupIds::contains)
                        .toList();
                if (filteredGroupIds.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not supervise the selected group(s)");
                }
                // Return only members from the supervised group(s) — exclude supervisors and logged-in user
                return userAdapter.findUsersByGroupIdsAndRoleTypeExcludingUser(
                        filteredGroupIds,
                        userIdFromToken
                );
            } else if (userIdFromToken!=null){
                return List.of(userAdapter.getUserById(userIdFromToken));                }
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
                .findByFirstClockInNotNullAndLastClockOutIsNullAndDate(today);

        for (TimesheetEntity entry : openClockIns) {
            entry.setLastClockOut(LocalTime.now());
            calculateHours(entry);
        }
        timesheetAdapter.saveAll(openClockIns);
    }
}
