package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.adapter.WorkScheduleAdapter;
import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.entity.*;
import com.uniq.tms.tms_microservice.enums.*;
import com.uniq.tms.tms_microservice.helper.RolePrivilegeHelper;
import com.uniq.tms.tms_microservice.mapper.TimesheetDtoMapper;
import com.uniq.tms.tms_microservice.mapper.TimesheetEntityMapper;
import com.uniq.tms.tms_microservice.model.TimesheetHistory;
import com.uniq.tms.tms_microservice.model.TimesheetStatus;
import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import com.uniq.tms.tms_microservice.service.TimesheetService;
import org.apache.logging.log4j.LogManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.Logger;
import static com.uniq.tms.tms_microservice.enums.TimesheetStatusEnum.*;
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
    private final RolePrivilegeHelper rolePrivilegeHelper;

    public TimesheetServiceImpl(TimesheetAdapter timesheetAdapter, TimesheetEntityMapper timesheetEntityMapper, TimesheetDtoMapper timesheetDtoMapper, UserAdapter userAdapter, WorkScheduleAdapter workScheduleAdapter, CacheLoaderService cacheLoaderService, RolePrivilegeHelper rolePrivilegeHelper) {
        this.timesheetAdapter = timesheetAdapter;
        this.timesheetEntityMapper = timesheetEntityMapper;
        this.timesheetDtoMapper = timesheetDtoMapper;
        this.userAdapter = userAdapter;
        this.workScheduleAdapter = workScheduleAdapter;
        this.cacheLoaderService = cacheLoaderService;
        this.rolePrivilegeHelper = rolePrivilegeHelper;
    }

    public List<UserTimesheetResponseDto> getAllTimesheets(String userIdFromToken, String orgId, String role, TimesheetReportDto request) {
        LocalDate fromDate = request.getFromDate();
        LocalDate toDate = request.getToDate();
        String timePeriod = request.getTimePeriod();
        log.info("Time period: {}", timePeriod);
        List<String> userId = request.getUserId();
        List<Long> groupIds = request.getGroupId();
        LocalDate startDate = null;
        LocalDate endDate = null;
        List<Long> roleIds = request.getRoleId();
        List<Long> locationIds = request.getLocationId();
        List<String> statusIds = request.getStatusId();

        if (fromDate != null && !timePeriod.isEmpty()) {
            LocalDateRange range = calculateDateRange(fromDate, timePeriod);
            startDate = range.startDate();
            endDate = range.endDate();
            log.info("startDate: {}, endDate: {}", startDate, endDate);
            if (endDate.isAfter(LocalDate.now(ZoneId.of("Asia/Kolkata"))
)) {
                endDate = LocalDate.now(ZoneId.of("Asia/Kolkata"))
;
                log.info("endDate: {}", endDate);
            }
        } else if (fromDate != null && toDate != null) {
            startDate = fromDate;
            endDate = toDate;
            if (endDate.isAfter(LocalDate.now(ZoneId.of("Asia/Kolkata"))
)) {
                endDate = LocalDate.now(ZoneId.of("Asia/Kolkata"))
;
                log.info("endDate: {}", endDate);
            }
        }

        List<UserEntity> targetUsers = resolveTargetUsers(userIdFromToken, groupIds, userId, orgId, roleIds, locationIds, statusIds,startDate, endDate);

        List<String> userIds = targetUsers.stream()
                .map(UserEntity::getUserId)
                .toList();

        List<UserTimesheetResponseDto> timesheetDtos = timesheetAdapter.filterTimesheetsForAllUsers(startDate, endDate, userIds, orgId);
        return timesheetDtos;
    }

    private List<UserEntity> resolveTargetUsers(String userIdFromToken, List<Long> groupIds, List<String> userId, String orgId, List<Long> roleIds, List<Long> locationIds, List<String> statusId, LocalDate startDate, LocalDate endDate) {

        UserEntity currentUser = userAdapter.getUserById(userIdFromToken);
        String roleName = currentUser.getRole().getName().toUpperCase();
        log.info("Role name: {}", roleName);

        String canSeeOwnKey = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_OWN_TIMESHEET);
        boolean canSeeOwn = rolePrivilegeHelper.roleHasPrivilege(roleName, canSeeOwnKey);
        String canSeeGroupKey = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_GROUP_LEVEL_TIMESHEETS);
        boolean canSeeGroup = rolePrivilegeHelper.roleHasPrivilege(roleName, canSeeGroupKey);
        String canSeeAllKey = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_ALL_TIMESHEETS);
        boolean canSeeAll = rolePrivilegeHelper.roleHasPrivilege(roleName, canSeeAllKey);

        log.info("Privileges - Own: {}, Group: {}, All: {}", canSeeOwn, canSeeGroup, canSeeAll);
        boolean hasGroupFilter = groupIds != null && !groupIds.isEmpty();
        boolean hasRoleFilter = roleIds != null && !roleIds.isEmpty();
        boolean hasLocationFilter = locationIds != null && !locationIds.isEmpty();
        boolean hasStatusFilter = statusId != null && !statusId.isEmpty();

        if (canSeeOwn && canSeeGroup && canSeeAll) {
            if(userId != null && !userId.isEmpty() && userId.contains(userIdFromToken)){
                log.info("Logged user timesheet");
                return List.of(currentUser);
            }
            List<UserEntity> allUsers = userAdapter.getAllUsers(orgId, userIdFromToken, UserRole.SUPERADMIN.getHierarchyLevel());
            Stream<UserEntity> filteredStream = allUsers.stream();

            if(userId!=null && !userId.isEmpty()){
                log.info("Heirarchy level user timesheet");
                filteredStream = filteredStream
                        .filter(user -> userId.contains(user.getUserId()));
            }
            if (hasLocationFilter) {
                List<GroupEntity> groupLocations = userAdapter.findGroupLocationByLocationId(locationIds);
                List<Long> locationGroupIds = groupLocations.stream()
                        .map(loc -> loc.getGroupId())
                        .toList();

                if (locationGroupIds.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found for selected location(s)");
                }
                List<UserEntity> groupUsers = userAdapter.findUsersByGroupIds(locationGroupIds);

                Set<String> groupUserIds = groupUsers.stream()
                        .map(UserEntity::getUserId)
                        .collect(Collectors.toSet());

                filteredStream = filteredStream
                        .filter(user -> groupUserIds.contains(user.getUserId()));
            }
            if (hasGroupFilter) {
                List<UserEntity> groupUsers = userAdapter.findUsersByGroupIds(groupIds);
                Set<String> groupUserIds = groupUsers.stream()
                        .map(UserEntity::getUserId)
                        .collect(Collectors.toSet());

                if (groupUserIds.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found in selected group(s)");
                }

                filteredStream = filteredStream
                        .filter(user -> groupUserIds.contains(user.getUserId()));
            }
            if (hasRoleFilter) {
                Set<Long> roleIdSet = new HashSet<>(roleIds);
                filteredStream = filteredStream
                        .filter(user -> roleIdSet.contains(user.getRole().getRoleId()));
            }
            if (hasStatusFilter) {
                List<TimesheetEntity> timesheetEntities = timesheetAdapter.findUserByStatusId(statusId, startDate, endDate);
                Set<String> statusUserIds = timesheetEntities.stream()
                        .map(TimesheetEntity::getUserId)
                        .collect(Collectors.toSet());

                if (statusUserIds.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found with selected status(s)");
                }

                filteredStream = filteredStream
                        .filter(user -> statusUserIds.contains(user.getUserId()));
            }
            List<UserEntity> filteredUsers = filteredStream.toList();
            if (filteredUsers.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.OK, "No users matched the applied filters");
            }
            return filteredUsers;
        }

        if (canSeeOwn && canSeeGroup) {
            if(userId != null && !userId.isEmpty() && userId.equals(userIdFromToken)){
                return List.of(currentUser);
            }
            List<Long> supervisedGroupIds = userAdapter.findGroupIdsBySupervisorId(userIdFromToken);
            log.info("Supervised group ids: {}", supervisedGroupIds);

            List<UserEntity> allSupervisedUsers = userAdapter.findMembersByGroupIds(supervisedGroupIds, userIdFromToken);
            Stream<UserEntity> filteredStream = allSupervisedUsers.stream();
            if (userId != null && !userId.isEmpty()) {
                filteredStream = filteredStream
                        .filter(user -> userId.contains(user.getUserId()));
            }
            if (hasLocationFilter) {
                List<GroupEntity> groupLocations = userAdapter.findGroupLocationByLocationId(locationIds);
                List<Long> locationGroupIds = groupLocations.stream()
                        .map(GroupEntity::getGroupId)
                        .filter(supervisedGroupIds::contains)
                        .toList();

                if (locationGroupIds.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found for selected location(s)");
                }
                List<UserEntity> locationGroupUsers = userAdapter.findMembersByGroupIds(locationGroupIds, userIdFromToken);

                Set<String> locationGroupUserIds = locationGroupUsers.stream()
                        .map(UserEntity::getUserId)
                        .collect(Collectors.toSet());

                filteredStream = filteredStream
                        .filter(user -> locationGroupUserIds.contains(user.getUserId()));
            }
            if (hasGroupFilter) {
                List<Long> validGroupIds = groupIds.stream()
                        .filter(supervisedGroupIds::contains)
                        .toList();

                if (validGroupIds.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not supervise the selected group(s)");
                }

                List<UserEntity> groupFilteredUsers = userAdapter.findMembersByGroupIds(validGroupIds, userIdFromToken);
                Set<String> groupUserIds = groupFilteredUsers.stream()
                        .map(UserEntity::getUserId)
                        .collect(Collectors.toSet());

                filteredStream = filteredStream
                        .filter(user -> groupUserIds.contains(user.getUserId()));
            }
            if (hasRoleFilter) {
                Set<Long> roleIdSet = new HashSet<>(roleIds);
                filteredStream = filteredStream
                        .filter(user -> roleIdSet.contains(user.getRole().getRoleId()));
            }
            if (hasStatusFilter) {
                List<TimesheetEntity> timesheetEntities = timesheetAdapter.findUserByStatusId(statusId, startDate, endDate);
                Set<String> statusUserIds = timesheetEntities.stream()
                        .map(TimesheetEntity::getUserId)
                        .collect(Collectors.toSet());

                if (statusUserIds.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found with selected status(s)");
                }

                filteredStream = filteredStream
                        .filter(user -> statusUserIds.contains(user.getUserId()));
            }
            List<UserEntity> filteredUsers = filteredStream.toList();
            if (filteredUsers.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.OK, "No users matched the applied filters");
            }
            return filteredUsers;
        }

        if (canSeeOwn) {
            if (hasGroupFilter) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not supervise the selected group(s)");
            } else if (hasRoleFilter) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to view the higher official timesheet.");
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
            String userId = timesheet.getUserId();
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
    public TimesheetDto updateClockInOut(String userId, LocalDate date, TimesheetDto request, String orgId) {
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

        TimesheetStatusEntity statusEntity = null;
        if (request.getStatusId() != null && !request.getStatusId().isEmpty()) {
            statusEntity = timesheetAdapter.findById(request.getStatusId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status ID: " + request.getStatusId()));
        }

        if (Objects.equals(timesheet.getStatus().getStatusName(), PAID_LEAVE.getLabel())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Timesheet update not allowed. User is on paid leave.");
        }

        if (statusEntity != null && PAID_LEAVE.getLabel().equalsIgnoreCase(statusEntity.getStatusName())) {
            if (timesheet.getFirstClockIn() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot apply for paid leave after clock-in.");
            }
            timesheet.setStatus(statusEntity);
        } else {
            if (request.getLastClockOut() != null && request.getFirstClockIn() == null && timesheet.getFirstClockIn() == null) {
                throw new IllegalArgumentException("Cannot set clock-out without a clock-in.");
            }
            if (request.getFirstClockIn() != null) {
                timesheet.setFirstClockIn(request.getFirstClockIn());

                if (statusEntity == null) {
                    statusEntity = timesheetAdapter.findByStatusName(PRESENT.getLabel())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Default 'Present' status not found"));
                }
                timesheet.setStatus(statusEntity);
            }

            if (request.getLastClockOut() != null) {
                timesheet.setLastClockOut(request.getLastClockOut());
            }

            if (statusEntity != null) {
                timesheet.setStatus(statusEntity);
            }
        }

        calculateHours(timesheet);
        timesheet = timesheetAdapter.save(timesheet);

        LocationEntity locationEntity = userAdapter.findDefaultLocationByOrgId(orgId);
        if (isNew) {
            if (request.getFirstClockIn() != null) {
                TimesheetHistoryEntity clockInHistory = new TimesheetHistoryEntity();
                clockInHistory.setTimesheet(timesheet);
                clockInHistory.setLogTime(request.getFirstClockIn());
                clockInHistory.setLogType(LogType.CLOCK_IN);
                clockInHistory.setLogFrom(LogFrom.MANUAL_ENTRY);
                clockInHistory.setLocationId(locationEntity.getLocationId());
                clockInHistory.setLoggedTimestamp(LocalDateTime.now());
                timesheetAdapter.saveTimesheetHistory(clockInHistory);
            }
            log.info("Saved clock-in history ID");
            if (request.getLastClockOut() != null) {
                TimesheetHistoryEntity clockOutHistory = new TimesheetHistoryEntity();
                clockOutHistory.setTimesheet(timesheet);
                clockOutHistory.setLogTime(request.getLastClockOut());
                clockOutHistory.setLogType(LogType.CLOCK_OUT);
                clockOutHistory.setLogFrom(LogFrom.MANUAL_ENTRY);
                clockOutHistory.setLocationId(locationEntity.getLocationId());
                clockOutHistory.setLoggedTimestamp(LocalDateTime.now());
                timesheetAdapter.saveTimesheetHistory(clockOutHistory);
            }
            log.info("Saved clock-out history ID");
        } else {
            if (request.getFirstClockIn() != null) {
                timesheetAdapter.updateTimesheetHistory(
                        timesheet.getId(), LogType.CLOCK_IN, request.getFirstClockIn());
            }
            log.info("Saved clock-In history for existing");
            if (request.getLastClockOut() != null) {
                timesheetAdapter.updateTimesheetHistory(
                        timesheet.getId(), LogType.CLOCK_OUT, request.getLastClockOut());
            }
            log.info("Saved clock-out history for existing");
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
    public void autoClockOut(String orgId) {
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Kolkata"))
.minusDays(1);
        List<TimesheetEntity> openClockIns = timesheetAdapter.findActiveTimesheetsByDate(yesterday);
        List<TimesheetHistoryEntity> historyEntries = new ArrayList<>();

        log.info("Timesheets fetched for {}: {}", yesterday, openClockIns.size());
        LocationEntity locationEntity = new LocationEntity();
        LocationEntity location = timesheetAdapter.getDefaultLocation(orgId);
        for (TimesheetEntity entry : openClockIns) {
            if (entry.getFirstClockIn() != null && entry.getLastClockOut() == null) {
                log.info("Auto clock-out for userId={}, setting lastClockOut to 23:59", entry.getUserId());

                entry.setLastClockOut(LocalTime.of(23, 59));
                calculateHours(entry);

                TimesheetHistoryEntity history = new TimesheetHistoryEntity();
                history.setLocationId(location.getLocationId());
                history.setLogTime(LocalTime.of(23, 59));
                history.setLogType(LogType.CLOCK_OUT);
                history.setLogFrom(LogFrom.SYSTEM_GENERATED);
                history.setLoggedTimestamp(LocalDateTime.now());

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

    public List<UserDashboardDto> getAllUserInfo(String orgId, String userIdFromToken, LocalDate fromDate, LocalDate toDate, String userId, List<Long> groupIds, String type) {
        UserEntity currentUser = userAdapter.getUserById(userIdFromToken);
        String roleName = currentUser.getRole().getName();
        log.info("Role dashboard: {}", roleName);

        String canSeeOwnKey = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_OWN_TIMESHEET);
        boolean canSeeOwn = rolePrivilegeHelper.roleHasPrivilege(roleName, canSeeOwnKey);
        String canSeeGroupKey = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_GROUP_LEVEL_TIMESHEETS);
        boolean canSeeGroup = rolePrivilegeHelper.roleHasPrivilege(roleName, canSeeGroupKey);
        String canSeeAllKey = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_ALL_TIMESHEETS);
        boolean canSeeAll = rolePrivilegeHelper.roleHasPrivilege(roleName, canSeeAllKey);
        log.info("Privileges - Own: {}, Group: {}, All: {}", canSeeOwn, canSeeGroup, canSeeAll);

        List<UserEntity> filterUsers;

        if (userId != null) {
            filterUsers = Collections.singletonList(userAdapter.getUserById(userId));
        } else if (canSeeAll) {
            if (type != null && type.equalsIgnoreCase(RoleName.STAFF.getRoleName())) {
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
                filterUsers = userAdapter.findUsersByRolesAndGroupIds(Set.of(type), supervisedGroupIds, orgId);
            } else if ((type == null || type.isBlank()) && groupIds != null && !groupIds.isEmpty()) {
                filterUsers = userAdapter.findMembersByGroupIds(groupIds, userIdFromToken);
            } else {
                filterUsers = userAdapter.findMembersByGroupIds(supervisedGroupIds, userIdFromToken);
            }
        } else if (canSeeOwn) {
            filterUsers = Collections.singletonList(userAdapter.getUserById(userIdFromToken));
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        List<String> userIds = filterUsers.stream().map(UserEntity::getUserId).toList();
        log.info("User IDs: {}", userIds);
        timesheetAdapter.getDashboard(orgId, userIds, fromDate, toDate);
        return calculateAttendanceSummaryForUsers(userIds, fromDate, toDate, orgId);
    }

    private List<UserDashboardDto> calculateAttendanceSummaryForUsers(List<String> userIds, LocalDate fromDate, LocalDate toDate, String orgId) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.singletonList(UserDashboardDto.empty());
        }

        List<UserAttendanceDto> attendanceList = timesheetAdapter.findAttendanceForUserInRange(userIds, fromDate, toDate);

        Map<String, String> attendanceMap = new HashMap<>();
        for (UserAttendanceDto dto : attendanceList) {
            String key = dto.getUserId() + "|" + dto.getDate();
            attendanceMap.put(key, dto.getStatus());
        }

        Map<String, Set<DayOfWeek>> userWorkingDaysMap = new HashMap<>();
        for (String userId : userIds) {
            WorkScheduleEntity ws = workScheduleAdapter.getScheduleForUser(userId);
            if (ws == null) {
                throw new IllegalStateException("Work Schedule not assigned or inactive for user: " + userId);
            }
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

        int present = 0, absent = 0, paidLeave = 0, notMarked = 0, holiday = 0, halfDay = 0, permission = 0;
        int total = 0;

        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            DayOfWeek currentDay = date.getDayOfWeek();
            boolean isToday = date.equals(LocalDate.now(ZoneId.of("Asia/Kolkata"))
);

            for (String userId : userIds) {
                total++;
                String key = userId + "|" + date;
                Set<DayOfWeek> workingDays = userWorkingDaysMap.getOrDefault(userId, Collections.emptySet());

                if (!workingDays.contains(currentDay)) {
                    String status = attendanceMap.get(key);
                    if ("Present".equalsIgnoreCase(status)) {
                        present++;
                    } else {
                        holiday++;
                    }
                    continue;
                }

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
                        case "Absent" -> absent++;
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
    public List<UserTimesheetDto> getUserTimesheets(String userIdFromToken, String orgId, String role, TimesheetReportDto request) {
        LocalDate fromDate = request.getFromDate();
        LocalDate toDate = request.getToDate();
        String timePeriod = request.getTimePeriod();
        List<String> userIds = request.getUserId();

        LocalDate startDate = null;
        LocalDate endDate = null;

        if (fromDate != null && timePeriod != null) {
            LocalDateRange range = calculateDateRange(fromDate, timePeriod);
            startDate = range.startDate();
            endDate = range.endDate();
            log.info("startDate: {}, endDate: {}", startDate, endDate);
            if (endDate.isAfter(LocalDate.now(ZoneId.of("Asia/Kolkata"))
)) {
                endDate = LocalDate.now(ZoneId.of("Asia/Kolkata"))
;
                log.info("endDate: {}", endDate);
            }
        } else if (fromDate != null && toDate != null) {
            startDate = fromDate;
            endDate = toDate;
            log.info("startDate: {}, endDate: {}", startDate, endDate);
            if (endDate.isAfter(LocalDate.now(ZoneId.of("Asia/Kolkata"))
)) {
                endDate = LocalDate.now(ZoneId.of("Asia/Kolkata"))
;
                log.info("endDate: {}", endDate);
            }
        } else if (fromDate == null && toDate == null && userIds != null) {
            endDate = LocalDate.now(ZoneId.of("Asia/Kolkata"))
;
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

    @Override
    public List<TimesheetHistory> processTimesheet(List<TimesheetHistory> timesheetMiddlewareLogs) {
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
            String userId = timesheet.getUserId();
            LocalDate date = timesheet.getDate();

            if (userId == null) {
                throw new IllegalArgumentException("User ID must not be null. Check mapping!");
            }

            if (date == null) {
                date = history.getLoggedTimestamp().toLocalDate();
            }

            LocalDate finalDate = date;

            Optional<TimesheetEntity> existingTimesheetOpt = timesheetAdapter.findByUserIdAndDate(userId, date);

            if (existingTimesheetOpt.isPresent()) {
                timesheet = existingTimesheetOpt.get();

                // Condition: if already clocked out, reject new clock-in
                if (history.getLogType() == LogType.CLOCK_IN && timesheet.getLastClockOut() != null) {
                    throw new IllegalStateException("User has already clocked out for today. Cannot clock in again.");
                }

                // Condition: if already clocked in, reject duplicate clock-in
                if (history.getLogType() == LogType.CLOCK_IN && timesheet.getFirstClockIn() != null) {
                    throw new IllegalStateException("User has already clocked in for today.");
                }

                // Condition: if clocking out but no clock-in yet
                if (history.getLogType() == LogType.CLOCK_OUT && timesheet.getFirstClockIn() == null) {
                    throw new IllegalStateException("User has not clocked in yet. Cannot clock out.");
                }

            } else {
                // Create new timesheet if none exists
                timesheet = new TimesheetEntity();
                timesheet.setUserId(userId);
                timesheet.setDate(finalDate);
                timesheet.setCreatedAt(LocalDateTime.now());
                timesheet.setTrackedHours(LocalTime.of(0, 0));
                timesheet.setTotalBreakHours(LocalTime.of(0, 0));
                timesheet.setRegularHours(LocalTime.of(0, 0));

                TimesheetStatusEntity presentStatus = timesheetAdapter.findByStatusName(TimesheetStatusEnum.PRESENT.getLabel())
                        .orElseThrow(() -> new IllegalStateException("Status Present Not Found"));
                timesheet.setStatus(presentStatus);

                // Set clock-in/out depending on the first log
                if (history.getLogType() == LogType.CLOCK_IN) {
                    timesheet.setFirstClockIn(LocalTime.now());
                } else if (history.getLogType() == LogType.CLOCK_OUT) {
                    throw new IllegalStateException("Cannot clock out before clocking in.");
                }

                timesheet = timesheetAdapter.saveTimesheet(timesheet);
            }

            // Set timesheet in history
            history.setTimesheet(timesheet);

            // Save history and update timesheet
            history.setLogTime(LocalTime.now());
            history.setLoggedTimestamp(LocalDateTime.now());
            savedEntities.add(timesheetAdapter.saveTimesheetHistory(history));

            // Update timesheet clock-in/out if not already set
            if (history.getLogType() == LogType.CLOCK_IN && timesheet.getFirstClockIn() == null) {
                timesheet.setFirstClockIn(LocalTime.now());
            } else if (history.getLogType() == LogType.CLOCK_OUT && timesheet.getLastClockOut() == null) {
                timesheet.setLastClockOut(LocalTime.now());
            }

            timesheetAdapter.saveTimesheet(timesheet);
        }

        timesheetAdapter.calculateTrackedAndBreakHours(savedEntities);

        return savedEntities.stream()
                .map(timesheetEntityMapper::toMiddleware)
                .toList();
    }

}
