package com.uniq.tms.tms_microservice.modules.timesheetManagement.services.impl;

import com.uniq.tms.tms_microservice.modules.locationManagement.adapter.LocationAdapter;
import com.uniq.tms.tms_microservice.modules.locationManagement.entity.LocationEntity;
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
import org.apache.logging.log4j.LogManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.RoundingMode;
import java.sql.Time;
import java.text.DecimalFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.Logger;
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

    public TimesheetServiceImpl(TimesheetAdapter timesheetAdapter, TimesheetEntityMapper timesheetEntityMapper, TimesheetDtoMapper timesheetDtoMapper,
                                UserAdapter userAdapter, WorkScheduleAdapter workScheduleAdapter,
                                OrganizationCacheService organizationCacheService, RolePrivilegeHelper rolePrivilegeHelper,
                                LocationAdapter locationAdapter) {
        this.timesheetAdapter = timesheetAdapter;
        this.timesheetEntityMapper = timesheetEntityMapper;
        this.timesheetDtoMapper = timesheetDtoMapper;
        this.userAdapter = userAdapter;
        this.workScheduleAdapter = workScheduleAdapter;
        this.organizationCacheService = organizationCacheService;
        this.rolePrivilegeHelper = rolePrivilegeHelper;
        this.locationAdapter = locationAdapter;
    }

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

        // Privilege checks
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

        // Flags
        boolean hasGroupFilter = !groupIdSet.isEmpty();
        boolean hasRoleFilter = !roleIdSet.isEmpty();
        boolean hasLocationFilter = !locationIdSet.isEmpty();
        boolean hasStatusFilter = statusId != null && !statusId.isEmpty();
        boolean hasUserFilter = !userIdSet.isEmpty();
        // Own + Group + All
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

        // Own + Group
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

        //  Own only
        if (canSeeOwn) {
            if (hasGroupFilter || hasUserFilter || hasRoleFilter || hasLocationFilter || hasStatusFilter) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not supervise the selected group(s)");
            } else if (!userIdSet.contains(userIdFromToken)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "You don't have access to view the higher official timesheet.");
            }
            return List.of(currentUser);
        }
        // No Access
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

        // User filter
        if (!userIdSet.isEmpty()) {
            stream = stream.filter(user -> userIdSet.contains(user.getUserId()));
        }

        // Location filter
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

        // Group filter
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

        // Role filter
        if (!roleIdSet.isEmpty()) {
            stream = stream.filter(user -> roleIdSet.contains(user.getRole().getRoleId()));
        }

        // Status filter
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

            stream = baseUsers.stream()
                    .filter(user -> usersToInclude.contains(user.getUserId()));
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
            String userId = timesheet.getUser().getUserId();
            LocalDate date = timesheet.getDate();

            if (userId == null) {
                throw new IllegalArgumentException("User ID must not be null. Check mapping!");
            }

            if (date == null) {
                date = LocalDate.now(ZoneId.of("Asia/Kolkata"));
            }

            LocalDate finalDate = date;

            Optional<TimesheetEntity> existingTimesheetOpt = timesheetAdapter.findByUserIdAndDate(userId, date);

            if (existingTimesheetOpt.isPresent()) {
                timesheet = existingTimesheetOpt.get();

                if (history.getLogType() == LogType.CLOCK_IN && timesheet.getLastClockOut() != null) {
                    throw new IllegalStateException("User has already clocked out for today. Cannot clock in again.");
                }

                if (history.getLogType() == LogType.CLOCK_IN && timesheet.getFirstClockIn() != null) {
                    throw new IllegalStateException("User has already clocked in for today.");
                }

                if (history.getLogType() == LogType.CLOCK_OUT && timesheet.getFirstClockIn() == null) {
                    throw new IllegalStateException("User has not clocked in yet. Cannot clock out.");
                }

                if(history.getLogType() == LogType.CLOCK_OUT && timesheet.getLastClockOut() != null){
                    throw new IllegalStateException("User has already Clocked out for a day, cannot clock out again.");
                }

            } else {
                timesheet = new TimesheetEntity();
                UserEntity user = userAdapter.findById(userId)
                        .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
                log.info("User : {}", user);
                timesheet.setUser(user);
                timesheet.setDate(finalDate);
                timesheet.setCreatedAt(LocalDateTime.now());
                timesheet.setTrackedHours(LocalTime.of(0, 0));
                timesheet.setTotalBreakHours(LocalTime.of(0, 0));
                timesheet.setRegularHours(LocalTime.of(0, 0));

                TimesheetStatusEntity presentStatus = timesheetAdapter.findByStatusName(TimesheetStatusEnum.PRESENT.getLabel())
                        .orElseThrow(() -> new IllegalStateException("Status Present Not Found"));
                timesheet.setStatus(presentStatus);

                if (history.getLogType() == LogType.CLOCK_IN) {
                    timesheet.setFirstClockIn(LocalTime.now(ZoneId.of("Asia/Kolkata")));
                } else if (history.getLogType() == LogType.CLOCK_OUT) {
                    throw new IllegalStateException("Cannot clock out before clocking in.");
                }

                timesheet = timesheetAdapter.saveTimesheet(timesheet);
            }

            history.setTimesheet(timesheet);

            history.setLogTime(LocalTime.now(ZoneId.of("Asia/Kolkata")));
            history.setLoggedTimestamp(LocalDateTime.now());
            savedEntities.add(timesheetAdapter.saveTimesheetHistory(history));

            if (history.getLogType() == LogType.CLOCK_IN && timesheet.getFirstClockIn() == null) {
                timesheet.setFirstClockIn(LocalTime.now(ZoneId.of("Asia/Kolkata")));
            } else if (history.getLogType() == LogType.CLOCK_OUT && timesheet.getLastClockOut() == null) {
                timesheet.setLastClockOut(LocalTime.now(ZoneId.of("Asia/Kolkata")));
            }

            timesheetAdapter.saveTimesheet(timesheet);
        }

        timesheetAdapter.calculateTrackedAndBreakHours(savedEntities);

        return savedEntities.stream()
                .map(timesheetEntityMapper::toMiddleware)
                .toList();
    }

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

        if (!isSuperAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied: only superadmin can edit other users' timesheets.");
        }


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
        } else {
            if (request.getLastClockOut() != null && request.getFirstClockIn() == null
                    && timesheet.getFirstClockIn() == null) {
                throw new IllegalArgumentException("Cannot set clock-out without a clock-in.");
            }

            if (request.getFirstClockIn() != null) {
                timesheet.setFirstClockIn(request.getFirstClockIn());

                if (statusEntity == null) {
                    statusEntity = timesheetAdapter.findByStatusName(PRESENT.getLabel())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "Default 'Present' status not found"));
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

        LocationEntity locationEntity = locationAdapter.findDefaultLocationByOrgId(orgId);
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
        } else {
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
    public void autoClockOut(String orgId) {
        LocalDate day = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        if(LocalTime.now(ZoneId.of("Asia/Kolkata")).equals(LocalTime.MIDNIGHT)){
            day = day.minusDays(1);
        }

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
                        calculateHours(entry);

                        TimesheetHistoryEntity history = new TimesheetHistoryEntity();
                        history.setLocationId(location.getLocationId());
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
        log.info("Saving successfully...");
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
}
