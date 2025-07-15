package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.adapter.WorkScheduleAdapter;
import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.entity.*;
import com.uniq.tms.tms_microservice.mapper.TimesheetDtoMapper;
import com.uniq.tms.tms_microservice.mapper.TimesheetEntityMapper;
import com.uniq.tms.tms_microservice.model.TimesheetHistory;
import com.uniq.tms.tms_microservice.model.TimesheetStatus;
import com.uniq.tms.tms_microservice.repository.TimesheetStatusRepository;
import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import com.uniq.tms.tms_microservice.service.TimesheetService;
import com.uniq.tms.tms_microservice.util.CacheKeyUtil;
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
import java.util.*;
import org.apache.logging.log4j.Logger;
import static com.uniq.tms.tms_microservice.dto.TimesheetStatusEnum.*;
import static com.uniq.tms.tms_microservice.enums.WorkScheduleTypeEnum.FIXED;
import static com.uniq.tms.tms_microservice.enums.WorkScheduleTypeEnum.FLEXIBLE;

@Service
public class TimesheetServiceImpl implements TimesheetService {

    private static final Logger log = LogManager.getLogger(TimesheetServiceImpl.class);

    private final TimesheetAdapter timesheetAdapter;
    private final TimesheetEntityMapper timesheetEntityMapper;
    private final TimesheetDtoMapper timesheetDtoMapper;
    private final UserAdapter userAdapter;
    private final WorkScheduleAdapter workScheduleAdapter;
    private final CacheLoaderService cacheLoaderService;
    private final CacheKeyUtil cacheKeyUtil;

    public TimesheetServiceImpl(TimesheetAdapter timesheetAdapter, TimesheetEntityMapper timesheetEntityMapper, TimesheetDtoMapper timesheetDtoMapper, UserAdapter userAdapter, WorkScheduleAdapter workScheduleAdapter, CacheLoaderService cacheLoaderService, CacheKeyUtil cacheKeyUtil) {
        this.timesheetAdapter = timesheetAdapter;
        this.timesheetEntityMapper = timesheetEntityMapper;
        this.timesheetDtoMapper = timesheetDtoMapper;
        this.userAdapter = userAdapter;
        this.workScheduleAdapter = workScheduleAdapter;
        this.cacheLoaderService = cacheLoaderService;
        this.cacheKeyUtil = cacheKeyUtil;
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
        List<Long> roleIds = request.getRoleId();

        if (fromDate != null && !timePeriod.isEmpty()) {
            LocalDateRange range = calculateDateRange(fromDate, timePeriod);
            startDate = range.startDate();
            endDate = range.endDate();
            log.info("startDate: {}, endDate: {}", startDate, endDate);
            if (endDate.isAfter(LocalDate.now())) {
                endDate = LocalDate.now();
                log.info("endDate: {}", endDate);
            }
        } else if (fromDate != null && toDate != null) {
            startDate = fromDate;
            endDate = toDate;
            if (endDate.isAfter(LocalDate.now())) {
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
        List<UserEntity> targetUsers = resolveTargetUsers(userIdFromToken, groupIds, userId, orgId, roleIds);

        List<Long> userIds = targetUsers.stream()
                .map(UserEntity::getUserId)
                .toList();

        // Fetch timesheets for the filtered users and date range
        List<UserTimesheetResponseDto> timesheetDtos = timesheetAdapter.filterTimesheetsForAllUsers(startDate, endDate, userIds, orgId);
        return timesheetDtos;
    }

    private List<UserEntity> resolveTargetUsers(Long userIdFromToken, List<Long> groupIds, List<Long> userId, Long orgId, List<Long> roleIds) {

        UserEntity currentUser = userAdapter.getUserById(userIdFromToken);
        String roleName = currentUser.getRole().getName().toUpperCase();
        log.info("Role name: {}", roleName);

        String canSeeOwnKey = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_OWN_TIMESHEET);
        boolean canSeeOwn = cacheKeyUtil.roleHasPrivilege(roleName, canSeeOwnKey);
        String canSeeGroupKey = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_GROUP_LEVEL_TIMESHEETS);
        boolean canSeeGroup = cacheKeyUtil.roleHasPrivilege(roleName, canSeeGroupKey);
        String canSeeAllKey = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_ALL_TIMESHEETS);
        boolean canSeeAll = cacheKeyUtil.roleHasPrivilege(roleName, canSeeAllKey);

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
            } else if (roleIds != null && !roleIds.isEmpty()) {
                List<UserEntity> userRole = userAdapter.findByRoleId(roleIds, orgId);
                if (userRole.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "No user found in the selected role(s)");
                }
                if (userId != null && !userId.isEmpty()) {
                    List<UserEntity> matchedUsers = userRole.stream()
                            .filter(user -> userId.contains(user.getUserId()))
                            .toList();
                    if (matchedUsers.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "No user found in the selected roles with the given userIds");
                    }
                    return matchedUsers;
                }
                return userRole;
            } else if (userId != null && !userId.isEmpty()) {
                return userAdapter.getUsersByIds(userId, currentUser.getOrganizationId());
            } else {
                int hierarchyLevel = UserRole.SUPERADMIN.getHierarchyLevel();
                return userAdapter.getAllUsers(orgId, userIdFromToken, hierarchyLevel);
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
            } else if (roleIds != null && !roleIds.isEmpty()) {
                List<UserEntity> userRole = userAdapter.findByRoleId(roleIds, orgId);
                if (userRole.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "No user found in the selected role(s)");
                }
                if (userId != null && !userId.isEmpty()) {
                    List<UserEntity> matchedUsers = userRole.stream()
                            .filter(user -> userId.contains(user.getUserId()))
                            .toList();
                    if (matchedUsers.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "No user found in the selected roles with the given userIds");
                    }
                    return matchedUsers;
                }
                return userRole;
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
                        TimesheetStatusEntity presentStatus = timesheetAdapter.findByStatusName(TimesheetStatusEnum.PRESENT.getLabel())
                                .orElseThrow(() -> new IllegalStateException("Status Present Not Found"));
                        log.info("PresentStatus:", presentStatus);
                        newTimesheet.setStatus(presentStatus);
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
            TimesheetStatusEntity defaultStatus = timesheetAdapter.findByStatusName(NOT_MARKED.getLabel())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Default 'Not Marked' status not found"));
            timesheet.setStatus(defaultStatus);
        }

           TimesheetStatusEntity statusEntity = timesheetAdapter.findById(request.getStatusId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status ID: " + request.getStatusId()));

        // Block editing if already on paid leave
        if (Objects.equals(timesheet.getStatus().getStatusName(), PAID_LEAVE.getLabel())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Timesheet update not allowed. User is on paid leave.");
        }

        // Handle paid leave request
        if (PAID_LEAVE.getLabel().equalsIgnoreCase(statusEntity.getStatusName())) {
            if (timesheet.getFirstClockIn() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot apply for paid leave after clock-in.");
            }
            timesheet.setStatus(statusEntity);
        } else {
            // Validate: do not allow clockOut without clockIn
            if (request.getLastClockOut() != null && request.getFirstClockIn() == null && timesheet.getFirstClockIn() == null) {
                throw new IllegalArgumentException("Cannot set clock-out without a clock-in.");
            }

            // Allow clock in and mark as present
            if (request.getFirstClockIn() != null) {
                timesheet.setFirstClockIn(request.getFirstClockIn());
                timesheet.setStatus(statusEntity);
            }

            // Allow clock out
            if (request.getLastClockOut() != null) {
                timesheet.setLastClockOut(request.getLastClockOut());
            }

            // Update status if provided (like Half Day, Permission)
            if (statusEntity != null) {
                timesheet.setStatus(statusEntity);
            }
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
                } else {
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

    public List<UserDashboardDto> getAllUserInfo(Long orgId, Long userIdFromToken, LocalDate fromDate, LocalDate toDate, Long userId, List<Long> groupIds, String type) {
        UserEntity currentUser = userAdapter.getUserById(userIdFromToken);
        String roleName = currentUser.getRole().getName();
        log.info("Role dashboard: {}", roleName);

        String canSeeOwnKey = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_OWN_TIMESHEET);
        boolean canSeeOwn = cacheKeyUtil.roleHasPrivilege(roleName, canSeeOwnKey);
        String canSeeGroupKey = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_GROUP_LEVEL_TIMESHEETS);
        boolean canSeeGroup = cacheKeyUtil.roleHasPrivilege(roleName, canSeeGroupKey);
        String canSeeAllKey = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_ALL_TIMESHEETS);
        boolean canSeeAll = cacheKeyUtil.roleHasPrivilege(roleName, canSeeAllKey);
        log.info("Privileges - Own: {}, Group: {}, All: {}", canSeeOwn, canSeeGroup, canSeeAll);

        List<UserEntity> filterUsers;

        // 1. Base user list based on userId or role privileges
        if (userId != null) {
            filterUsers = Collections.singletonList(userAdapter.getUserById(userId));
        } else if (canSeeAll) {
            if (type != null && type.equalsIgnoreCase(RoleName.STAFF.getRoleName())) {
                // Get all users with STAFF / ADMIN / MANAGER roles
                Set<String> roles = Set.of(RoleName.STAFF.getRoleName(), RoleName.ADMIN.getRoleName(), RoleName.MANAGER.getRoleName());
                log.info("Roles: {}", roles);
                filterUsers = userAdapter.getUsersByRoles(roles, orgId);
                log.info("Filtered users: {}", filterUsers);
            } else if (type != null && !type.isBlank()) {
                // Get all users for the given role
                filterUsers = userAdapter.getUsersByRoles(Set.of(type), orgId);
            } else if ((type == null || type.isBlank()) && groupIds != null && !groupIds.isEmpty()) {
                List<UserEntity> groupFilteredUsers = userAdapter.findUsersByGroupIds(groupIds);
                filterUsers = groupFilteredUsers;
            } else {
                log.info("userIdFrom Token: {}, orgId:{}", userIdFromToken, orgId);
                int heriarchyLevel = UserRole.SUPERADMIN.getHierarchyLevel();
                filterUsers = userAdapter.getAllUsers(orgId, userIdFromToken, heriarchyLevel);
                log.info("filteredUsers:{}", filterUsers);
            }
        } else if (canSeeGroup) {
            List<Long> supervisedGroupIds = userAdapter.findGroupIdsBySupervisorId(userIdFromToken);
            if (type != null && type.equalsIgnoreCase(RoleName.STAFF.getRoleName())) {
                Set<String> roles = Set.of(RoleName.STAFF.getRoleName(), RoleName.ADMIN.getRoleName(), RoleName.MANAGER.getRoleName());
                log.info("Roles: {}", roles);
                filterUsers = userAdapter.findUsersByRolesAndGroupIds(roles, supervisedGroupIds, orgId);
                log.info("Filtered users (supervised): {}", filterUsers);
            } else if (type != null && !type.isBlank()) {
                // Filter by given role within supervised groups
                filterUsers = userAdapter.findUsersByRolesAndGroupIds(Set.of(type), supervisedGroupIds, orgId);
            } else if ((type == null || type.isBlank()) && groupIds != null && !groupIds.isEmpty()) {
                // Groups explicitly selected by frontend (assumed to be part of supervised groups)
                filterUsers = userAdapter.findMembersByGroupIds(groupIds, userIdFromToken);
            } else {
                // All members from groups they supervise
                filterUsers = userAdapter.findMembersByGroupIds(supervisedGroupIds, userIdFromToken);
            }
        } else if (canSeeOwn) {
            filterUsers = Collections.singletonList(userAdapter.getUserById(userIdFromToken));
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        // 4. Prepare user IDs and proceed
        List<Long> userIds = filterUsers.stream().map(UserEntity::getUserId).toList();
        log.info("User IDs: {}", userIds);
        timesheetAdapter.getDashboard(orgId, userIds, fromDate, toDate);
        return calculateAttendanceSummaryForUsers(userIds, fromDate, toDate, orgId);
    }

    private List<UserDashboardDto> calculateAttendanceSummaryForUsers(List<Long> userIds, LocalDate fromDate, LocalDate toDate, Long orgId) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.singletonList(UserDashboardDto.empty());
        }

        // Fetch attendance data
        List<UserAttendanceDto> attendanceList = timesheetAdapter.findAttendanceForUserInRange(userIds, fromDate, toDate);

        // Build a map of userId|date to status
        Map<String, String> attendanceMap = new HashMap<>();
        for (UserAttendanceDto dto : attendanceList) {
            String key = dto.getUserId() + "|" + dto.getDate();
            attendanceMap.put(key, dto.getStatus());
        }

        // Build working days per user like timesheet logic
        Map<Long, Set<DayOfWeek>> userWorkingDaysMap = new HashMap<>();
        for (Long userId : userIds) {
            WorkScheduleEntity ws = workScheduleAdapter.getScheduleForUser(userId);
            Set<DayOfWeek> workingDays = new HashSet<>();

            if (ws.getType().getType().name().equalsIgnoreCase(FLEXIBLE.getScheduleType())) {
                List<FlexibleWorkScheduleEntity> flexDays = workScheduleAdapter.findByWorkScheduleId(ws.getScheduleId());
                for (FlexibleWorkScheduleEntity f : flexDays) {
                    workingDays.add(DayOfWeek.valueOf(f.getDay().toString()));
                }
            } else if (ws.getType().getType().name().equalsIgnoreCase(FIXED.getScheduleType())) {
                List<FixedWorkScheduleEntity> fixedDays = workScheduleAdapter.findByFixedScheduleId(ws.getScheduleId());
                for (FixedWorkScheduleEntity f : fixedDays) {
                    workingDays.add(DayOfWeek.valueOf(f.getDay().toString()));
                }
            }
            userWorkingDaysMap.put(userId, workingDays);
        }

        // Track counts
        int present = 0, absent = 0, paidLeave = 0, notMarked = 0, holiday = 0, halfDay = 0, permission = 0;
        int total = 0;

        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            DayOfWeek currentDay = date.getDayOfWeek();
            boolean isToday = date.equals(LocalDate.now());

            for (Long userId : userIds) {
                total++;
                String key = userId + "|" + date;
                Set<DayOfWeek> workingDays = userWorkingDaysMap.getOrDefault(userId, Collections.emptySet());

                // Check if it's a rest day
                if (!workingDays.contains(currentDay)) {
                    String status = attendanceMap.get(key);
                    if ("Present".equalsIgnoreCase(status)) {
                        present++; // worked on rest day
                    } else {
                        holiday++; // true holiday
                    }
                    continue;
                }

                // Else: working day
                String status = attendanceMap.get(key);
                if (status == null) {
                    if (isToday) {
                        notMarked++;
                    } else {
                        absent++;
                    }
                } else {
                    switch (status) {
                        case "Present" -> present++;
                        case "Paid Leave" -> paidLeave++;
                        case "Half Day" -> halfDay++;
                        case "Permission" -> permission++;
                        case "Not Marked" -> notMarked++;
                        case "Absent" -> absent++; // in case absent is stored explicitly
                    }
                }
            }
        }

        // Build response DTO
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
        double totalCount = totalDays * userIds.size();
        double totalCountPercentage = totalCount > 0 ? totalCount : 1.0;

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
            if (endDate.isAfter(LocalDate.now())) {
                endDate = LocalDate.now();
                log.info("endDate: {}", endDate);
            }
        } else if (fromDate != null && toDate != null) {
            startDate = fromDate;
            endDate = toDate;
            log.info("startDate: {}, endDate: {}", startDate, endDate);
            if (endDate.isAfter(LocalDate.now())) {
                endDate = LocalDate.now();
                log.info("endDate: {}", endDate);
            }
        } else if (fromDate == null && toDate == null && userIds != null) {
            endDate = LocalDate.now();
        }

        List<UserTimesheetDto> rawResults = timesheetAdapter.fetchUserTimesheetsWithHistory(startDate, endDate, userIds, orgId);
        return rawResults;
    }

    @Override
    public List<TimesheetStatus> getStatus(){
        List<TimesheetStatus> status = timesheetAdapter.getStatus().stream()
                .map(timesheetDtoMapper::toStatusModel)
                .toList();
        return status;
    }
}
