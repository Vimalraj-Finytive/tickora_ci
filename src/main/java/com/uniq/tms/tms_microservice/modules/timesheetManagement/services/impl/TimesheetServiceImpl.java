package com.uniq.tms.tms_microservice.modules.timesheetManagement.services.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.Status;
import com.uniq.tms.tms_microservice.modules.locationManagement.adapter.LocationAdapter;
import com.uniq.tms.tms_microservice.modules.locationManagement.entity.LocationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.OrganizationAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.services.OrganizationCacheService;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.*;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.TimesheetHistoryEntity;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.TimesheetStatusEntity;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.*;
import com.uniq.tms.tms_microservice.modules.userManagement.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.modules.locationManagement.entity.UserLocationEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.PrivilegeConstants;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.RoleName;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.adapter.WorkScheduleAdapter;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.WorkScheduleTypeEntity;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.enums.DayOfWeekEnum;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.enums.WorkScheduleTypeEnum;
import com.uniq.tms.tms_microservice.shared.exception.CommonExceptionHandler;
import com.uniq.tms.tms_microservice.shared.helper.RolePrivilegeHelper;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.mapper.TimesheetDtoMapper;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.mapper.TimesheetEntityMapper;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.model.TimesheetHistory;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.model.TimesheetStatus;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.services.TimesheetService;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.UserRole;
import com.uniq.tms.tms_microservice.shared.util.TimesheetLogParserUtil;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.FixedWorkScheduleEntity;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.FlexibleWorkScheduleEntity;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.WorkScheduleEntity;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.RoundingMode;
import java.sql.Time;
import java.text.DecimalFormat;
import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.Logger;

import javax.swing.text.html.Option;

import static com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.TimesheetStatusEnum.*;
import static com.uniq.tms.tms_microservice.modules.workScheduleManagement.enums.WorkScheduleTypeEnum.FIXED;
import static com.uniq.tms.tms_microservice.modules.workScheduleManagement.enums.WorkScheduleTypeEnum.FLEXIBLE;

@Service
public class TimesheetServiceImpl implements TimesheetService {

    private static final Logger log = LogManager.getLogger(TimesheetServiceImpl.class);

    private final TimesheetAdapter timesheetAdapter;
    private final TimesheetEntityMapper timesheetEntityMapper;
    private final TimesheetDtoMapper timesheetDtoMapper;
    private final UserAdapter userAdapter;
    private final WorkScheduleAdapter workScheduleAdapter;
    private final OrganizationCacheService organizationCacheService;
    private final RolePrivilegeHelper rolePrivilegeHelper;
    private final LocationAdapter locationAdapter;
    private final OrganizationAdapter organizationAdapter;

    public TimesheetServiceImpl(TimesheetAdapter timesheetAdapter, TimesheetEntityMapper timesheetEntityMapper, TimesheetDtoMapper timesheetDtoMapper,
                                UserAdapter userAdapter, WorkScheduleAdapter workScheduleAdapter,
                                OrganizationCacheService organizationCacheService, RolePrivilegeHelper rolePrivilegeHelper,
                                LocationAdapter locationAdapter, OrganizationAdapter organizationAdapter) {
        this.timesheetAdapter = timesheetAdapter;
        this.timesheetEntityMapper = timesheetEntityMapper;
        this.timesheetDtoMapper = timesheetDtoMapper;
        this.userAdapter = userAdapter;
        this.workScheduleAdapter = workScheduleAdapter;
        this.organizationCacheService = organizationCacheService;
        this.rolePrivilegeHelper = rolePrivilegeHelper;
        this.locationAdapter = locationAdapter;
        this.organizationAdapter = organizationAdapter;
    }

    private final ZoneId zoneId = ZoneId.of("Asia/Kolkata");

    public PaginationResponseDto getAllTimesheets(String userIdFromToken, String orgId, String role, TimesheetReportDto request) {
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
        Integer pageIndex = request.getPageIndex();
        Integer pageSize = request.getPageSize();
        String keyword = request.getKeyword();

        if (fromDate != null && !timePeriod.isEmpty()) {
            LocalDateRange range = calculateDateRange(fromDate, timePeriod);
            startDate = range.startDate();
            endDate = range.endDate();
            log.info("startDate: {}, endDate: {}", startDate, endDate);
            if (endDate.isAfter(LocalDate.now(ZoneId.of("Asia/Kolkata")))) {
                endDate = LocalDate.now(ZoneId.of("Asia/Kolkata"));
                log.info("endDate: {}", endDate);
            }
        } else if (fromDate != null && toDate != null) {
            startDate = fromDate;
            endDate = toDate;
            if (endDate.isAfter(LocalDate.now(ZoneId.of("Asia/Kolkata")))) {
                endDate = LocalDate.now(ZoneId.of("Asia/Kolkata"));
                log.info("endDate: {}", endDate);
            }
        }
        List<UserEntity> targetUsers ;
        if ((groupIds == null && roleIds == null && locationIds == null) && keyword != null && !keyword.isBlank()) {
            log.info("Group, role is null");
            String normalizaedKeyword = normalizeKeyword(keyword);
            targetUsers = userAdapter.searchUsers(normalizaedKeyword);
            if(targetUsers.isEmpty()){
                return TimesheetLogParserUtil.emptyPagination(pageIndex, pageSize);
            }
        }else{
            targetUsers = resolveTargetUsers(userIdFromToken, groupIds, userId, orgId, roleIds, locationIds, statusIds,startDate, endDate);
        }
        if ((groupIds != null || roleIds != null || locationIds != null) && keyword != null && !keyword.isBlank()) {
            log.info("Group, role is not null");
            String normalizedKeyword = normalizeKeyword(keyword);
            targetUsers = targetUsers.stream()
                    .filter(user ->
                            user.getUserId().equalsIgnoreCase(normalizedKeyword) ||
                                    user.getUserName().toLowerCase().contains(normalizedKeyword.toLowerCase()) ||
                                    (user.getMobileNumber() != null && user.getMobileNumber().contains(normalizedKeyword))
                    )
                    .toList();

            if (targetUsers.isEmpty()) {
                return TimesheetLogParserUtil.emptyPagination(pageIndex, pageSize);
            }
        }

        List<String> userIds = targetUsers.stream()
                .map(UserEntity::getUserId)
                .toList();

        PaginationResponseDto  timesheetDtos = timesheetAdapter.filterTimesheetsForAllUsers(startDate, endDate, userIds, orgId, pageIndex, pageSize);
        return timesheetDtos;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "";
        }
        String mobileNumber = keyword.replaceAll("[\\s\\-]", "");
        if (mobileNumber.startsWith("+91")) {
            mobileNumber = mobileNumber.substring(3);
        } else if (mobileNumber.startsWith("+1")) {
            mobileNumber = mobileNumber.substring(2);
        } else if (mobileNumber.startsWith("+")) {
            mobileNumber = mobileNumber.substring(1);
        }
        return mobileNumber.trim();
    }


    private List<UserEntity> resolveTargetUsers(
            String userIdFromToken,
            List<Long> groupIds,
            List<String> userId,
            String orgId,
            List<Long> roleIds,
            List<Long> locationIds,
            List<String> statusId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        log.info("userId : {} , userIdFromToken : {}", userId, userIdFromToken);
        UserEntity currentUser = userAdapter.getUserById(userIdFromToken);
        String roleName = currentUser.getRole().getName().toUpperCase();
        log.info("Role name: {}", roleName);
        boolean canSeeOwn = rolePrivilegeHelper.roleHasPrivilege(roleName,
                organizationCacheService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_OWN_TIMESHEET));
        boolean canSeeGroup = rolePrivilegeHelper.roleHasPrivilege(roleName,
                organizationCacheService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_GROUP_LEVEL_TIMESHEETS));
        boolean canSeeAll = rolePrivilegeHelper.roleHasPrivilege(roleName,
                organizationCacheService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_ALL_TIMESHEETS));
        log.info("Privileges - Own: {}, Group: {}, All: {}", canSeeOwn, canSeeGroup, canSeeAll);
        Set<String> userIdSet = userId != null ? new HashSet<>(userId) : Collections.emptySet();
        Set<Long> roleIdSet = roleIds != null ? new HashSet<>(roleIds) : Collections.emptySet();
        Set<Long> groupIdSet = groupIds != null ? new HashSet<>(groupIds) : Collections.emptySet();
        Set<Long> locationIdSet = locationIds != null ? new HashSet<>(locationIds) : Collections.emptySet();
        boolean hasGroupFilter = !groupIdSet.isEmpty();
        boolean hasRoleFilter = !roleIdSet.isEmpty();
        boolean hasLocationFilter = !locationIdSet.isEmpty();
        boolean hasStatusFilter = statusId != null && !statusId.isEmpty();
        boolean hasUserFilter = !userIdSet.isEmpty();
        if (canSeeOwn && canSeeGroup && canSeeAll) {
            if (!userIdSet.isEmpty()){
                if (userIdSet.contains(userIdFromToken)){
                    log.info("Logged user timesheet");
                    return List.of(currentUser);
                }
            }
            List<UserEntity> allUsers = userAdapter.getAllUsers(orgId, userIdFromToken,
                    UserRole.SUPERADMIN.getHierarchyLevel());

            if(hasUserFilter || hasLocationFilter || hasRoleFilter || hasGroupFilter || hasStatusFilter) {
                return applyFilters(allUsers, userIdSet, groupIdSet, locationIdSet, roleIdSet,
                        statusId, startDate, endDate, userIdFromToken);
            }
            return allUsers;
        }
        if (canSeeOwn && canSeeGroup) {
            if (!userIdSet.isEmpty()){
              if (userIdSet.contains(userIdFromToken)){
                    log.info("Logged user timesheet");
                    return List.of(currentUser);
                }
            }
            List<Long> supervisedGroupIds = userAdapter.findGroupIdsBySupervisorId(userIdFromToken);
            log.info("Supervised group ids: {}", supervisedGroupIds);
            List<UserEntity> supervisedUsers = userAdapter.findMembersByGroupIds(supervisedGroupIds, userIdFromToken);
            if(hasUserFilter || hasLocationFilter || hasRoleFilter || hasGroupFilter || hasStatusFilter) {
                return applyFilters(supervisedUsers, userIdSet, groupIdSet, locationIdSet, roleIdSet,
                        statusId, startDate, endDate, supervisedGroupIds, userIdFromToken);
            }
            return supervisedUsers;
        }
        if (canSeeOwn) {
            if (hasGroupFilter || hasUserFilter || hasRoleFilter || hasLocationFilter || hasStatusFilter) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not supervise the selected group(s)");
            } else if (!userIdSet.contains(userIdFromToken)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "You don't have access to view the higher official timesheet.");
            }
            return List.of(currentUser);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied based on role privileges.");
    }

    /**
     * Applies group, location, role, and status filters to the given users.
     */
    private List<UserEntity> applyFilters(
            List<UserEntity> baseUsers,
            Set<String> userIdSet,
            Set<Long> groupIdSet,
            Set<Long> locationIdSet,
            Set<Long> roleIdSet,
            List<String> statusId,
            LocalDate startDate,
            LocalDate endDate,
            String userIdFromToken
    ) {
        return applyFilters(baseUsers, userIdSet, groupIdSet, locationIdSet, roleIdSet,
                statusId, startDate, endDate, null,userIdFromToken);
    }

    private List<UserEntity> applyFilters(
            List<UserEntity> baseUsers,
            Set<String> userIdSet,
            Set<Long> groupIdSet,
            Set<Long> locationIdSet,
            Set<Long> roleIdSet,
            List<String> statusId,
            LocalDate startDate,
            LocalDate endDate,
            List<Long> supervisedGroupIds,
            String userIdFromToken
    ) {

        Stream<UserEntity> stream = baseUsers.stream();

        if (!userIdSet.isEmpty()) {
            stream = stream.filter(user -> userIdSet.contains(user.getUserId()));
        }

        if (!locationIdSet.isEmpty()) {
            List<UserLocationEntity> userLocations = locationAdapter.findUserLocationsByLocationId(new ArrayList<>(locationIdSet));
            List<Long> locationIds = userLocations.stream()
                    .map(UserLocationEntity::getLocation)
                    .map(LocationEntity::getLocationId)
                    .toList();

            if (locationIds.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found for selected location(s)");
            }

            log.info("Supervised groupIds : {}", supervisedGroupIds);
            List<UserEntity> userLocation = supervisedGroupIds != null
                    ? locationAdapter.findUsersByIdsAndLocationIds(baseUsers.stream().map(UserEntity::getUserId).toList(), locationIds)
                    : locationAdapter.findMembersByLocationIds(locationIds, userIdFromToken);

            log.info("location Group User : {}", userLocation.stream()
                    .map(UserEntity::getUserId)
                    .toList());

            Set<String> locationUserIds = userLocation.stream()
                    .map(UserEntity::getUserId)
                    .collect(Collectors.toSet());

            stream = stream.filter(user -> locationUserIds.contains(user.getUserId()));
        }

        if (!groupIdSet.isEmpty()) {
            List<Long> validGroupIds = supervisedGroupIds != null
                    ? groupIdSet.stream().filter(supervisedGroupIds::contains).toList()
                    : new ArrayList<>(groupIdSet);

            log.info("Valid groupIds : {}", validGroupIds);
            if (validGroupIds.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not supervise the selected group(s)");
            }

            List<UserEntity> groupUsers = supervisedGroupIds != null
                    ? userAdapter.findMembersByGroupIds(validGroupIds, userIdFromToken)
                    : userAdapter.findUsersByGroupIds(validGroupIds);
            Set<String> groupUserIds = groupUsers.stream()
                    .map(UserEntity::getUserId)
                    .collect(Collectors.toSet());

            if (groupUserIds.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found in selected group(s)");
            }

            stream = stream.filter(user -> groupUserIds.contains(user.getUserId()));
        }

        if (!roleIdSet.isEmpty()) {
            stream = stream.filter(user -> roleIdSet.contains(user.getRole().getRoleId()));
        }

        if (statusId != null && !statusId.isEmpty()) {

            Set<String> specialStatusId = Set.of(ABSENT.getId(), NOT_MARKED.getId());

            List<String> trimmedStatusIds = statusId.stream()
                    .map(String::trim)
                    .collect(Collectors.toList());

            LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
            if (trimmedStatusIds.contains(NOT_MARKED.getId()) && !endDate.isEqual(today)) {
                log.info("NOT_MARKED status can only be filtered for today's date. Ignoring it.");
                trimmedStatusIds.remove(NOT_MARKED.getId());
            }

            if(trimmedStatusIds.contains(ABSENT.getId()) && endDate.isEqual(today)){
                log.info("ABSENT status can only be filtered for past date. Ignoring it.");
                trimmedStatusIds.remove(ABSENT.getId());
            }

            Set<String> specialStatus = trimmedStatusIds.stream()
                    .filter(specialStatusId::contains)
                    .collect(Collectors.toSet());

            Set<String> normalStatus = trimmedStatusIds.stream()
                    .filter(s -> !specialStatusId.contains(s))
                    .collect(Collectors.toSet());

            log.info("SpecialStatus : {}", specialStatus);
            log.info("NormalStatus : {}", normalStatus);

            Set<String> usersToInclude = new HashSet<>();

            if (!normalStatus.isEmpty()) {
                List<TimesheetEntity> normalTimesheets =
                        timesheetAdapter.findUserByStatusId(new ArrayList<>(normalStatus), startDate, endDate);

                normalTimesheets.forEach(te -> usersToInclude.add(te.getUser().getUserId()));
            }

            if (!specialStatus.isEmpty()) {
                List<String> usersWithoutTimesheet = timesheetAdapter.findUserByStatusIdNotIn(startDate, endDate);
                usersToInclude.addAll(usersWithoutTimesheet);
            }

            stream = stream.filter(user -> usersToInclude.contains(user.getUserId()));
        }

        List<UserEntity> result = stream.toList();
        if (result.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.OK, "No users matched the applied filters");
        }
        return result;
    }

        private LocalDateRange calculateDateRange(LocalDate date, String timePeriod) {
        return Timeperiod.fromString(timePeriod).calculateDateRange(date);
    }

    public static record LocalDateRange(LocalDate startDate, LocalDate endDate) {
    }

    @Override
    @Transactional
    public List<TimesheetHistory> processTimesheetLogs(List<TimesheetHistory> timesheetMiddlewareLogs) {
        List<TimesheetHistoryEntity> entities = timesheetMiddlewareLogs.stream()
                .map(timesheetEntityMapper::toDto)
                .map(timesheetEntityMapper::toEntity)
                .toList();
        Map<String, Map<LocalDate, List<TimesheetHistoryEntity>>> groupedLogs =
                entities.stream().collect(Collectors.groupingBy(
                        e -> e.getTimesheet().getUser().getUserId(),
                        Collectors.groupingBy(e -> Optional.ofNullable(e.getTimesheet().getDate())
                                .orElse(LocalDate.now(ZoneId.of("Asia/Kolkata"))))
                ));
        List<TimesheetHistoryEntity> allSaved = new ArrayList<>();
        for (Map.Entry<String, Map<LocalDate, List<TimesheetHistoryEntity>>> userEntry : groupedLogs.entrySet()) {
            String userId = userEntry.getKey();
            UserEntity user = userAdapter.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
            for (Map.Entry<LocalDate, List<TimesheetHistoryEntity>> dateEntry : userEntry.getValue().entrySet()) {
                LocalDate date = dateEntry.getKey();
                List<TimesheetHistoryEntity> logs = dateEntry.getValue();
                TimesheetEntity timesheet = timesheetAdapter.findByUserIdAndDate(userId, date)
                        .orElseGet(() -> {
                            TimesheetEntity t = new TimesheetEntity();
                            t.setUser(user);
                            t.setDate(date);
                            t.setTrackedHours(LocalTime.MIN);
                            t.setRegularHours(LocalTime.MIN);
                            t.setTotalBreakHours(LocalTime.MIN);
                            t.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                            TimesheetStatusEntity status = timesheetAdapter.findByStatusName(
                                            TimesheetStatusEnum.PRESENT.getLabel())
                                    .orElseThrow(() -> new IllegalStateException("Status not found"));
                            t.setStatus(status);
                            return timesheetAdapter.saveTimesheet(t);
                        });
                logs.sort(Comparator.comparing(TimesheetHistoryEntity::getLoggedTimestamp));
                LocalTime lastClockIn = timesheet.getFirstClockIn();
                LocalTime lastClockOut = timesheet.getLastClockOut();
                long trackedSeconds = timesheet.getTrackedHours().toSecondOfDay();
                long breakSeconds = timesheet.getTotalBreakHours().toSecondOfDay();
                for (TimesheetHistoryEntity log : logs) {
                    log.setTimesheet(timesheet);
                    log.setLoggedTimestamp(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    log.setLogTime(LocalTime.now(ZoneId.of("Asia/Kolkata")));
                    if (log.getLogType() == LogType.CLOCK_IN) {
                        if (timesheet.getFirstClockIn() != null && lastClockOut == null) {
                            throw new IllegalStateException("User has already clocked in for today and not clocked out yet.");
                        }
                        if (lastClockOut != null) {
                            long breakTime = Duration.between(lastClockOut, log.getLogTime()).getSeconds();
                            if (breakTime > 0) breakSeconds += breakTime;
                        }
                        if (timesheet.getFirstClockIn() == null)
                            timesheet.setFirstClockIn(log.getLogTime());
                        lastClockIn = log.getLogTime();
                    }
                    else if (log.getLogType() == LogType.CLOCK_OUT) {
                        if (timesheet.getFirstClockIn() == null) {
                            throw new IllegalStateException("User has not clocked in yet. Cannot clock out.");
                        }
                        if (timesheet.getLastClockOut() != null) {
                            throw new IllegalStateException("User has already clocked out for the day.");
                        }
                        if (lastClockIn != null) {
                            long workTime = Duration.between(lastClockIn, log.getLogTime()).getSeconds();
                            if (workTime > 0) trackedSeconds += workTime;
                            lastClockOut = log.getLogTime();
                        }
                    }
                    allSaved.add(log);
                }
                timesheet.setTrackedHours(LocalTime.ofSecondOfDay(trackedSeconds));
                timesheet.setRegularHours(LocalTime.ofSecondOfDay(trackedSeconds));
                timesheet.setTotalBreakHours(LocalTime.ofSecondOfDay(breakSeconds));
                timesheet.setLastClockOut(lastClockOut);
                timesheetAdapter.saveTimesheet(timesheet);
            }
        }
        timesheetAdapter.saveAllTimesheetHistories(allSaved);
        return allSaved.stream().map(timesheetEntityMapper::toMiddleware).toList();
    }

    @Override
    @Transactional
    public TimesheetDto updateClockInOut(
            String userIdFromToken,
            String roleName,
            String userId,
            LocalDate date,
            TimesheetDto request,
            String orgId) {
        boolean isSuperAdmin = UserRoleName.SUPERADMIN.getRoleName().equalsIgnoreCase(roleName);
        if (!isSuperAdmin && userIdFromToken.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied: you cannot edit your own timesheet.");
        }
        try {
            TimesheetEntity timesheet = timesheetAdapter.findUserIdAndDate(userId, date);
            boolean isNew = false;
            if (timesheet == null) {
                isNew = true;
                timesheet = new TimesheetEntity();
                UserEntity user = userAdapter.findById(userId)
                        .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
                timesheet.setUser(user);
                timesheet.setDate(date);
                timesheet.setCreatedAt(LocalDateTime.now());
                timesheet.setFirstClockIn(null);
                timesheet.setLastClockOut(null);
                timesheet.setTrackedHours(null);
                timesheet.setRegularHours(null);
                timesheet.setTotalBreakHours(null);
                TimesheetStatusEntity defaultStatus = timesheetAdapter.findByStatusName(NOT_MARKED.getLabel())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Default 'Not Marked' status not found"));
                timesheet.setStatus(defaultStatus);
            }
            TimesheetStatusEntity statusEntity = null;
            if (request.getStatusId() != null && !request.getStatusId().isEmpty()) {
                statusEntity = timesheetAdapter.findById(request.getStatusId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Invalid status ID: " + request.getStatusId()));
            }
            if (Objects.equals(timesheet.getStatus().getStatusName(), PAID_LEAVE.getLabel())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Timesheet update not allowed. User is on paid leave.");
            }
            if (statusEntity != null && PAID_LEAVE.getLabel().equalsIgnoreCase(statusEntity.getStatusName())) {
                if (timesheet.getFirstClockIn() != null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Cannot apply for paid leave after clock-in.");
                }
                timesheet.setStatus(statusEntity);
                timesheet = timesheetAdapter.save(timesheet);
                return timesheetDtoMapper.toDto(timesheet);
            }
            if (request.getLastClockOut() != null &&
                    request.getFirstClockIn() == null &&
                    timesheet.getFirstClockIn() == null) {
                throw new IllegalArgumentException("Cannot set clock-out without a clock-in.");
            }
            boolean clockInChanged = false;
            boolean clockOutChanged = false;
            if (request.getFirstClockIn() != null &&
                    !Objects.equals(request.getFirstClockIn(), timesheet.getFirstClockIn())) {

                timesheet.setFirstClockIn(request.getFirstClockIn());
                clockInChanged = true;

                if (statusEntity == null) {
                    statusEntity = timesheetAdapter.findByStatusName(PRESENT.getLabel())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "Default 'Present' status not found"));
                }
                timesheet.setStatus(statusEntity);
            }
            if (request.getLastClockOut() != null &&
                    !Objects.equals(request.getLastClockOut(), timesheet.getLastClockOut())) {
                timesheet.setLastClockOut(request.getLastClockOut());
                clockOutChanged = true;
            }
            if (statusEntity != null) {
                timesheet.setStatus(statusEntity);
            }
            timesheet.setUpdatedAt(LocalDateTime.now());
                UserEntity users = timesheet.getUser();
                WorkScheduleEntity workSchedule = users.getWorkSchedule();
                calculateHours(timesheet, workSchedule, true);
            timesheet = timesheetAdapter.save(timesheet);
            LocationEntity locationEntity = locationAdapter.findDefaultLocationByOrgId(orgId);
            if(locationEntity == null){
                throw new CommonExceptionHandler.DefaultLocationNotFoundException("Default location not found");
            }
            if ((isNew || clockInChanged) && request.getFirstClockIn() != null) {
                TimesheetHistoryEntity clockInHistory = new TimesheetHistoryEntity();
                clockInHistory.setTimesheet(timesheet);
                clockInHistory.setLogTime(request.getFirstClockIn());
                clockInHistory.setLogType(LogType.CLOCK_IN);
                clockInHistory.setLogFrom(LogFrom.MANUAL_ENTRY);
                clockInHistory.setLocationId(locationEntity);
                clockInHistory.setLoggedTimestamp(LocalDateTime.now());
                timesheetAdapter.saveTimesheetHistory(clockInHistory);
            }
            if ((isNew || clockOutChanged) && request.getLastClockOut() != null) {
                TimesheetHistoryEntity clockOutHistory = new TimesheetHistoryEntity();
                clockOutHistory.setTimesheet(timesheet);
                clockOutHistory.setLogTime(request.getLastClockOut());
                clockOutHistory.setLogType(LogType.CLOCK_OUT);
                clockOutHistory.setLogFrom(LogFrom.MANUAL_ENTRY);
                clockOutHistory.setLocationId(locationEntity);
                clockOutHistory.setLoggedTimestamp(LocalDateTime.now());
                timesheetAdapter.saveTimesheetHistory(clockOutHistory);
            }
            return timesheetDtoMapper.toDto(timesheet);
    } catch (Exception e) {
        log.error("Unhandled error in updateClockInOut", e);
        throw e;
    }
}

private void calculateHours(TimesheetEntity timesheet, WorkScheduleEntity workSchedule, boolean isManualClockOut) {
        try {
            if (timesheet.getFirstClockIn() == null || timesheet.getLastClockOut() == null) {
                log.warn("Clock-in or clock-out time is missing for timesheet ID: {}", timesheet.getId());
                return;
            }

            LocalTime clockIn = timesheet.getFirstClockIn();
            LocalTime clockOut = timesheet.getLastClockOut();
            LocalDate timesheetDate = timesheet.getDate();
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
            boolean isPreviousDay = clockOut.isBefore(clockIn);
            Duration workedDuration;
            Duration regularWorkDuration = Duration.ZERO;
            Duration totalOverTime = Duration.ZERO;
            Duration startTimeDuration = Duration.ZERO;
            Duration endTimeDuration = Duration.ZERO;
            if (clockOut.isBefore(clockIn)) {
                workedDuration = Duration.between(clockIn, clockOut).plusHours(24);
                log.info("Overnight Work Duration: {}", workedDuration);
            } else {
                workedDuration = Duration.between(clockIn, clockOut);
                log.info("Same-day Work Duration: {}", workedDuration);
            }

            DayOfWeekEnum day = DayOfWeekEnum.valueOf(timesheetDate.getDayOfWeek().name());

            if (workSchedule.getType().getType() == WorkScheduleTypeEnum.FIXED) {

                FixedWorkScheduleEntity fixedEntity =
                        timesheetAdapter.findByWorkScheduleIdAndDay(workSchedule.getScheduleId(), day);

                if (fixedEntity == null) {
                    log.warn("No fixed schedule found for workScheduleId={} day={}", workSchedule.getScheduleId(), day);
                    return;
                }

                LocalTime scheduleStart = fixedEntity.getStartTime().toLocalTime();
                LocalTime scheduleEnd = fixedEntity.getEndTime().toLocalTime();

                if (clockIn.isBefore(scheduleStart) && !(scheduleStart.isAfter(scheduleEnd) && clockIn.isBefore(scheduleEnd))) {
                    startTimeDuration = Duration.between(clockIn, scheduleStart);
                    totalOverTime = totalOverTime.plus(startTimeDuration);
                }
                if (isPreviousDay && scheduleStart.isBefore(scheduleEnd) && clockIn.isAfter(scheduleStart)) {
                    startTimeDuration = Duration.between(clockIn, scheduleStart);
                    startTimeDuration = startTimeDuration.plusHours(24);
                    totalOverTime = totalOverTime.plus(startTimeDuration);
                }
                if (clockOut.isAfter(scheduleEnd) && !(clockIn.isBefore(clockOut) && scheduleStart.isAfter(scheduleEnd))) {
                    endTimeDuration = Duration.between(scheduleEnd, clockOut);
                    totalOverTime = totalOverTime.plus(endTimeDuration);
                }
                if (isPreviousDay && scheduleEnd.isAfter(scheduleStart) && clockOut.isBefore(scheduleEnd)) {
                    endTimeDuration = Duration.between(scheduleEnd, clockOut);
                    endTimeDuration = endTimeDuration.plusHours(24);
                    totalOverTime = totalOverTime.plus(endTimeDuration);
                }
                log.info("Before regular hours");
                regularWorkDuration = calculateWorkDurationWithinSchedule(clockIn, clockOut, scheduleStart, scheduleEnd, startTimeDuration, endTimeDuration, isPreviousDay);
            }
            else if (workSchedule.getType().getType() == FLEXIBLE) {

                FlexibleWorkScheduleEntity flexibleEntity =
                        timesheetAdapter.findByWorkScheduleIdAndDays(workSchedule.getScheduleId(), day);

                if (flexibleEntity == null) {
                    log.warn("No flexible schedule found for workScheduleId={} day={}", workSchedule.getScheduleId(), day);
                    return;
                }

                Duration expectedDuration = Duration.ofHours(flexibleEntity.getDuration().longValue());
                regularWorkDuration = workedDuration;
                if (workedDuration.compareTo(expectedDuration) > 0) {
                    totalOverTime = workedDuration.minus(expectedDuration);
                    regularWorkDuration = expectedDuration;
                }

            }

            LocalTime workedTime = LocalTime.ofSecondOfDay(workedDuration.toSeconds());
            timesheet.setTrackedHours(workedTime);
            timesheet.setRegularHours(
                    regularWorkDuration.isZero() ? null : LocalTime.of((int)regularWorkDuration.toHours(),regularWorkDuration.toMinutesPart())
            );
            timesheet.setStartTimeDuration(startTimeDuration.isZero() ? null :
                    LocalTime.of((int)startTimeDuration.toHours(),startTimeDuration.toMinutesPart()));

            timesheet.setEndTimeDuration(endTimeDuration.isZero() ? null :
                    LocalTime.of((int)endTimeDuration.toHours(),endTimeDuration.toMinutesPart()));

            timesheet.setTotalOverTime(totalOverTime.isZero() ? null :
                    LocalTime.of((int)totalOverTime.toHours(),totalOverTime.toMinutesPart()));

            if (!isManualClockOut) {
                workedTime = workedTime.minus(endTimeDuration);
                timesheet.setTotalOverTime(startTimeDuration.isZero() ? null :
                        LocalTime.of((int)startTimeDuration.toHours(),startTimeDuration.toMinutesPart()));
                timesheet.setEndTimeDuration(null);
                timesheet.setTrackedHours(workedTime);
            }
            log.info("Regular Hours: {}", workedTime);
            log.info("Tracked Hours: {}", workedTime);

        } catch (Exception e) {
            log.error("Error calculating hours for timesheet ID: {}", timesheet.getId(), e);
            throw new RuntimeException(e);
        }
    }

    private Duration calculateWorkDurationWithinSchedule(LocalTime clockIn, LocalTime clockOut,
                                                                LocalTime startTime, LocalTime endTime, Duration startTimeDuration, Duration endTimeDuration, boolean isPreviousDay) {

        LocalTime effectiveStart = clockIn.isBefore(startTime) ? startTime : clockIn;
        LocalTime effectiveEnd = clockOut.isAfter(endTime) ? endTime : clockOut;

        if (!isPreviousDay && startTime.isAfter(endTime) && clockIn.isBefore(startTime)) {
            effectiveStart = clockIn;
        }
        if(isPreviousDay && startTime.isBefore(endTime) && clockIn.isAfter(startTime)){
            effectiveStart = startTime;
        }
        if(!isPreviousDay && startTime.isAfter(endTime) && clockOut.isAfter(endTime)){
            effectiveEnd = clockOut;
        }

        if (isPreviousDay && endTime.isAfter(startTime) && clockOut.isBefore(endTime)) {
            effectiveEnd = endTime;
        }

        if (effectiveEnd.isBefore(effectiveStart)) {
            return Duration.between(effectiveStart, effectiveEnd).plusHours(24);
        }
        return Duration.between(effectiveStart, effectiveEnd);
    }

    @Override
    public void autoClockOut(String orgId) {
        LocalDate day = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Kolkata"));
        LocalDate yesterday = day.minusDays(1);

        List<TimesheetEntity> openClockIns = timesheetAdapter.findActiveTimesheetsByDate(day);
        List<TimesheetHistoryEntity> historyEntries = new ArrayList<>();

        log.info("Timesheets fetched for {}: {}", day, openClockIns.size());
        LocationEntity locationEntity = new LocationEntity();
        LocationEntity location = timesheetAdapter.getDefaultLocation(orgId);
        for (TimesheetEntity entry : openClockIns) {
            UserEntity users = entry.getUser();
            WorkScheduleEntity workSchedule = users.getWorkSchedule();
            Time splitTime = workSchedule.getSplitTime();
            if (entry.getFirstClockIn() != null && entry.getLastClockOut() == null) {
                if(workSchedule.getAutoClockOut()) {
                    LocalTime currentTime = LocalTime.now(ZoneId.of("Asia/Kolkata"));
                    LocalTime splitLocalTime = (splitTime != null) ? splitTime.toLocalTime() : null;

                    if (splitLocalTime != null && (currentTime.getHour() == splitLocalTime.getHour())) {
                        log.info("Auto clock-out for userId={}, setting lastClockOut to 23:59", entry.getUser().getUserId());

                        entry.setLastClockOut(splitLocalTime.minusMinutes(1));
                        calculateHours(entry, workSchedule, false);
                        TimesheetHistoryEntity history = new TimesheetHistoryEntity();
                        history.setLocationId(location);
                        history.setLogTime(splitLocalTime.minusMinutes(1));
                        history.setLogType(LogType.CLOCK_OUT);
                        history.setLogFrom(LogFrom.SYSTEM_GENERATED);
                        history.setLoggedTimestamp(LocalDateTime.now());
                        TimesheetEntity timesheetRef = new TimesheetEntity();
                        timesheetRef.setId(entry.getId());
                        history.setTimesheet(timesheetRef);
                        historyEntries.add(history);
                        log.info("Added SYSTEM_GENERATED CLOCK_OUT history for userId={}, date={}", entry.getUser().getUserId(), entry.getDate());
                    }
                }
            }
        }
        log.info("Saving updated timesheets and history entries...");
        timesheetAdapter.saveAll(openClockIns);
        log.info("Today Timesheet Saving successfully...");
            openClockIns = timesheetAdapter.findActiveTimesheetsByDate(yesterday);
            for (TimesheetEntity entry : openClockIns) {
                UserEntity users = entry.getUser();
                WorkScheduleEntity workSchedule = users.getWorkSchedule();
                Time splitTime = workSchedule.getSplitTime();
                if (entry.getFirstClockIn() != null && entry.getLastClockOut() == null) {
                    if (workSchedule.getAutoClockOut()) {
                        LocalTime currentTime = LocalTime.now(ZoneId.of("Asia/Kolkata"));
                        LocalTime splitLocalTime = (splitTime != null) ? splitTime.toLocalTime() : null;

                        if (splitLocalTime != null && (currentTime.getHour() == splitLocalTime.getHour())) {
                            log.info("Auto clock-out for userId={}, setting lastClockOut to 23:59", entry.getUser().getUserId());
                            entry.setLastClockOut(splitLocalTime.minusMinutes(1));
                            calculateHours(entry, workSchedule, false);
                            log.info("Calculated Duration for Yesterday schedule");
                            TimesheetHistoryEntity history = new TimesheetHistoryEntity();
                            history.setLocationId(location);
                            history.setLogTime(splitLocalTime.minusMinutes(1));
                            history.setLogType(LogType.CLOCK_OUT);
                            history.setLogFrom(LogFrom.SYSTEM_GENERATED);
                            history.setLoggedTimestamp(LocalDateTime.now());
                            TimesheetEntity timesheetRef = new TimesheetEntity();
                            timesheetRef.setId(entry.getId());
                            history.setTimesheet(timesheetRef);
                            historyEntries.add(history);
                            log.info("Added SYSTEM_GENERATED CLOCK_OUT history for userId={}, date={}", entry.getUser().getUserId(), entry.getDate());
                        }
                    }
                }
            }
            log.info("Saving updated timesheets and history entries...");
            timesheetAdapter.saveAll(openClockIns);
            timesheetAdapter.saveAllTimesheetHistories(historyEntries);
            log.info("yesterday Timesheet Saving successfully...");
    }

    public List<UserDashboardDto> getAllUserInfo(String orgId, String userIdFromToken, LocalDate fromDate, LocalDate toDate, String userId, List<Long> groupIds, String type) {
        UserEntity currentUser = userAdapter.getUserById(userIdFromToken);
        String roleName = currentUser.getRole().getName();
        log.info("Role dashboard: {}", roleName);

        String canSeeOwnKey = organizationCacheService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_OWN_TIMESHEET);
        boolean canSeeOwn = rolePrivilegeHelper.roleHasPrivilege(roleName, canSeeOwnKey);
        String canSeeGroupKey = organizationCacheService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_GROUP_LEVEL_TIMESHEETS);
        boolean canSeeGroup = rolePrivilegeHelper.roleHasPrivilege(roleName, canSeeGroupKey);
        String canSeeAllKey = organizationCacheService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_ALL_TIMESHEETS);
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

    private List<UserDashboardDto> calculateAttendanceSummaryForUsers(
            List<String> userIds, LocalDate fromDate, LocalDate toDate, String orgId) {

        if (userIds == null || userIds.isEmpty()) {
            return Collections.singletonList(UserDashboardDto.empty());
        }

        // Fetch attendance data for the given users and date range
        List<UserAttendanceDto> attendanceList = timesheetAdapter.findAttendanceForUserInRange(userIds, fromDate, toDate);

        // Use LocalDate as key instead of String to avoid mismatches
        Map<String, Map<LocalDate, String>> attendanceMap = new HashMap<>();
        for (UserAttendanceDto dto : attendanceList) {
            attendanceMap
                    .computeIfAbsent(dto.getUserId(), k -> new HashMap<>())
                    .put(dto.getDate(), dto.getStatus());
        }

        // Fetch working days for each user
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

        // Attendance counters
        int present = 0, absent = 0, paidLeave = 0, notMarked = 0, holiday = 0, halfDay = 0, permission = 0;
        int total = 0;

        // Loop through each date in the range
        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            DayOfWeek currentDay = date.getDayOfWeek();
            boolean isToday = date.equals(LocalDate.now(ZoneId.of("Asia/Kolkata")));

            for (String userId : userIds) {
                total++;

                Set<DayOfWeek> workingDays = userWorkingDaysMap.getOrDefault(userId, Collections.emptySet());
                String status = attendanceMap.getOrDefault(userId, Collections.emptyMap()).get(date);

                // If the user does NOT work on this day → mark as holiday unless they have attendance
                if (!workingDays.contains(currentDay)) {
                    if ("Present".equalsIgnoreCase(status)) {
                        present++;
                    } else {
                        holiday++;
                    }
                    continue;
                }

                // If no attendance status recorded
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

        // Prepare the summary DTO
        UserDashboardDto summary = new UserDashboardDto();
        summary.setPresentCount(present);
        summary.setAbsentCount(absent);
        summary.setPaidLeaveCount(paidLeave);
        summary.setNotMarkedCount(notMarked);
        summary.setHalfDayCount(halfDay);
        summary.setPermissionCount(permission);
        summary.setHolidayCount(holiday);
        summary.setTotalCount(total);

        // Calculate percentages safely
        int totalDays = (int) ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        double totalCount = totalDays * userIds.size();
        double divisor = totalCount > 0 ? totalCount : 1.0;

        summary.setPresentPercentage(Double.parseDouble(formatToDecimal(present / divisor * 100.0)));
        summary.setAbsentPercentage(Double.parseDouble(formatToDecimal(absent / divisor * 100.0)));
        summary.setPaidLeavePercentage(Double.parseDouble(formatToDecimal(paidLeave / divisor * 100.0)));
        summary.setNotMarkedPercentage(Double.parseDouble(formatToDecimal(notMarked / divisor * 100.0)));
        summary.setHolidayPercentage(Double.parseDouble(formatToDecimal(holiday / divisor * 100.0)));
        summary.setHalfDayPercentage(Double.parseDouble(formatToDecimal(halfDay / divisor * 100.0)));
        summary.setPermissionPercentage(Double.parseDouble(formatToDecimal(permission / divisor * 100.0)));

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
        String timePeriod = request.getTimePeriod();
        List<String> userIds = request.getUserId();

        LocalDate startDate = null;
        LocalDate endDate = null;
        if (fromDate == null && timePeriod == null && userIds != null) {
            endDate = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        }else {
            startDate = fromDate;
            endDate = fromDate;
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
    public List<DashboardSummaryDto> getDashboardSummary(String orgId, LocalDate fromDate, LocalDate toDate) {
            OrganizationEntity organization = organizationAdapter.findByOrgId(orgId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));

            String orgName = organization.getOrgName();
            List<UserEntity> users = userAdapter.findByOrganizationIdAndActiveTrue(orgId);

            List<DashboardSummaryDto> result = new ArrayList<>();

            for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {

                long presentCount = timesheetAdapter.countByUserIdsAndDateAndStatus(
                        users.stream().map(UserEntity::getUserId).toList(),
                        date,
                        "PRESENT"
                );
                long absentCount = users.size() - presentCount;

                DashboardOrganizationSummaryDto orgSummary = timesheetDtoMapper
                        .toDashboardOrgSummary(orgId, orgName, (int) presentCount, (int) absentCount);

                DashboardSummaryDto dailyDto = timesheetDtoMapper
                        .toDashboardSummary(date, List.of(orgSummary));

                result.add(dailyDto);
            }

            return result;
    }

    @Override
    public void createTimesheet(TimesheetStatusEnum status, String userId, LocalDate date){
        Optional<UserEntity> userEntity = userAdapter.findById(userId);
        if (userEntity.isEmpty()) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        TimesheetStatusEntity timesheetStatus =
                timesheetAdapter.findByStatusName(status.getLabel())
                        .orElseThrow(() ->
                                new IllegalArgumentException("Invalid Timesheet Status: " + status));
        TimesheetEntity entity = new TimesheetEntity();
        entity.setUser(userEntity.get());
        entity.setDate(date);
        entity.setCreatedAt(LocalDateTime.now(zoneId));
        entity.setUpdatedAt(LocalDateTime.now(zoneId));
        entity.setStatus(timesheetStatus);
        TimesheetEntity savedTimesheet = timesheetAdapter.save(entity);
        TimesheetHistoryEntity timesheetHistoryEntity = new TimesheetHistoryEntity();
        timesheetHistoryEntity.setTimesheet(savedTimesheet);
        timesheetHistoryEntity.setLoggedTimestamp(LocalDateTime.now(zoneId));
        LocationEntity locationEntity = locationAdapter.findDefaultLocation();
        if(locationEntity == null){
            throw new CommonExceptionHandler.DefaultLocationNotFoundException("Default location not found");
        }
        timesheetHistoryEntity.setLocationId(locationEntity);
        timesheetHistoryEntity.setLogFrom(LogFrom.SYSTEM_GENERATED);
        timesheetHistoryEntity.setLogTime(LocalTime.now(zoneId));
        timesheetHistoryEntity.setLogType(null);
        timesheetAdapter.saveTimesheetHistory(timesheetHistoryEntity);
    }

    @Override
    public void deleteTimesheet(String userId, LocalDate date){
        timesheetAdapter.deleteTimesheet(userId,date);
    }
}
