package com.uniq.tms.tms_microservice.adapter.impl;

import com.uniq.tms.tms_microservice.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.adapter.WorkScheduleAdapter;
import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.entity.*;
import com.uniq.tms.tms_microservice.repository.TimesheetHistoryRepository;
import com.uniq.tms.tms_microservice.repository.TimesheetRepository;
import com.uniq.tms.tms_microservice.repository.TimesheetStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
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
import java.util.stream.Collectors;
import static com.uniq.tms.tms_microservice.enums.WorkScheduleTypeEnum.FIXED;
import static com.uniq.tms.tms_microservice.enums.WorkScheduleTypeEnum.FLEXIBLE;

@Component
public class TimesheetAdapterImpl implements TimesheetAdapter {

    private final TimesheetRepository timesheetRepository;
    private final TimesheetHistoryRepository timesheetHistoryRepository;
    private final WorkScheduleAdapter workScheduleAdapter;
    private final TimesheetStatusRepository timesheetStatusRepository;

    public TimesheetAdapterImpl(TimesheetRepository timesheetRepository, TimesheetHistoryRepository timesheetHistoryRepository, WorkScheduleAdapter workScheduleAdapter, TimesheetStatusRepository timesheetStatusRepository) {
        this.timesheetRepository = timesheetRepository;
        this.timesheetHistoryRepository = timesheetHistoryRepository;
        this.workScheduleAdapter = workScheduleAdapter;
        this.timesheetStatusRepository = timesheetStatusRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(TimesheetAdapterImpl.class);

    @Override
    public List<UserTimesheetResponseDto> filterTimesheetsForAllUsers(LocalDate startDate, LocalDate endDate, List<Long> userIds, String orgId) {
        Long[] userIdArray = userIds.toArray(new Long[0]);

        List<Object[]> resultList = timesheetRepository.fetchTimesheetsWithHistory(startDate, endDate, userIdArray, orgId);

        Map<String, TimesheetDto> timesheetMap = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        Map<Long, List<TimesheetDto>> userTimesheetListMap = new HashMap<>();

        for (Object[] row : resultList) {
            log.info("---- Row Start ----");
            for (int i = 0; i < row.length; i++) {
                log.info("Column {}: {}", i, row[i]);
            }
            log.info("---- Row End ----");

            if (row[1] == null) continue; // skip if userId is null

            Long userId = toLong(row[1]);
            LocalDate date = toLocalDate(row[0]);
            if (date == null) continue;

            String compositeKey = userId + "-" + date;

            TimesheetDto dto = timesheetMap.computeIfAbsent(compositeKey, key -> {
                TimesheetDto newDto = new TimesheetDto();
                newDto.setId(toLong(row[7]));
                newDto.setUserId(userId);
                newDto.setDate(date);
                newDto.setUserName((String) row[2]);
                newDto.setRole((String) row[3]);
                newDto.setMobileNumber((String) row[4]);
                newDto.setWorkScheduleName((String) row[5]);
                newDto.setGroupname((String) row[6]);
                newDto.setDayType((String) row[14]);
                newDto.setFirstClockIn(toLocalTime(row[8]));
                newDto.setLastClockOut(toLocalTime(row[9]));
                newDto.setTrackedHours(parseTimeToDuration(row[10], "tracked_hours"));
                newDto.setRegularHours(parseTimeToDuration(row[11], "regular_hours"));
                newDto.setStatus((String)row[13]);
                newDto.setUserDayType((String) row[15]);
                newDto.setWorkStatus((String) row[16]);
                newDto.setHistory(new ArrayList<>());
                newDto.setFirstClockInTime(formatTime(newDto.getFirstClockIn()));
                newDto.setLastClockOutTime(formatTime(newDto.getLastClockOut()));
                newDto.setTrackedHoursDuration(formatDuration(newDto.getTrackedHours()));
                newDto.setRegularHoursDuration(formatDuration(newDto.getRegularHours()));

                return newDto;
            });

            // Add history if present
            if (row[17] != null) {
                TimesheetHistoryDto historyDto = new TimesheetHistoryDto();
                historyDto.setTimesheetHistoryId(toLong(row[17]));
                historyDto.setLogTime(toLocalTime(row[18]));

                try {
                    historyDto.setLogType(row[19] != null ? LogType.valueOf(row[19].toString()) : null);
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid LogType: " + row[19]);
                }

                historyDto.setLocationId(toLong(row[20]));

                try {
                    historyDto.setLogFrom(row[21] != null ? LogFrom.valueOf(row[21].toString()) : null);
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid LogFrom: " + row[21]);
                }

                historyDto.setLoggedTimestamp(toLocalDateTime(row[22]));
                dto.getHistory().add(historyDto);
            }

            // Add dto to user list if not already present for that date
            userTimesheetListMap.computeIfAbsent(userId, k -> new ArrayList<>());
            List<TimesheetDto> userTimesheets = userTimesheetListMap.get(userId);
            if (userTimesheets.stream().noneMatch(t -> t.getDate().equals(dto.getDate()))) {
                userTimesheets.add(dto);
            }
        }

        Map<Long, Set<DayOfWeek>> userWorkingDaysMap = new HashMap<>();

        for (Long userId : userIds) {
            WorkScheduleEntity ws = workScheduleAdapter.getScheduleForUser(userId); // get user work schedule

            Set<DayOfWeek> workingDays = new HashSet<>();
            if (ws.getType().getType().name().equalsIgnoreCase(FLEXIBLE.getScheduleType())) { // Flexible
                List<FlexibleWorkScheduleEntity> flexDays = workScheduleAdapter.findByWorkScheduleId(ws.getScheduleId());
                for (FlexibleWorkScheduleEntity f : flexDays) {
                    workingDays.add(DayOfWeek.valueOf(f.getDay().toString()));
                }
            } else if (ws.getType().getType().name().equalsIgnoreCase(FIXED.getScheduleType())) { // Fixed
                List<FixedWorkScheduleEntity> fixedDays = workScheduleAdapter.findByFixedScheduleId(ws.getScheduleId());
                for (FixedWorkScheduleEntity f : fixedDays) {
                    workingDays.add(DayOfWeek.valueOf(f.getDay().toString()));
                }
            }
            userWorkingDaysMap.put(userId, workingDays);
        }

        // Build per-user summary
        Map<Long, TimesheetSummaryDto> userSummaryMap = new HashMap<>();
        for (Map.Entry<Long, List<TimesheetDto>> entry : userTimesheetListMap.entrySet()) {
            Long userId = entry.getKey();
            List<TimesheetDto> timesheets = entry.getValue();

            int present = 0, absent = 0, holiday = 0, notMarked = 0, paidLeave = 0, halfDay = 0, permission = 0;
            for (TimesheetDto t : timesheets) {
                Set<DayOfWeek> userWorkingDays = userWorkingDaysMap.getOrDefault(t.getUserId(), Collections.emptySet());
                DayOfWeek currentDay = t.getDate().getDayOfWeek();
                log.info("Current Day:{}", currentDay);

                if (!userWorkingDays.contains(currentDay)) {
                    // It's a rest day for this user
                    holiday++;
                    if (t.getStatus() == null) t.setStatus(TimesheetStatusEnum.HOLIDAY.getLabel());
                } else if (t.getStatus() == null) {
                    if (endDate.equals(LocalDate.now())) {
                        notMarked++;
                        t.setStatus(TimesheetStatusEnum.NOT_MARKED.getLabel());
                    } else {
                        absent++;
                        t.setStatus(TimesheetStatusEnum.ABSENT.getLabel());
                    }
                } else {
                    switch (t.getStatus()) {
                        case "Present" -> present++;
                        case "Paid Leave" -> paidLeave++;
                        case "Half Day" -> halfDay++;
                        case "Permission" -> permission++;
                    }
                }
            }

            TimesheetSummaryDto summary = new TimesheetSummaryDto();
            TimesheetDto first = timesheets.get(0);
            summary.setUserId(first.getUserId());
            summary.setUserName(first.getUserName());
            summary.setMobileNumber(first.getMobileNumber());
            summary.setRole(first.getRole());
            summary.setGroupname(first.getGroupname());

            summary.setPresentCount(present);
            summary.setAbsentCount(absent);
            summary.setHolidayCount(holiday);
            summary.setNotMarkedCount(notMarked);
            summary.setPaidLeaveCount(paidLeave);
            summary.setHalfDayCount(halfDay);
            summary.setPermissionCount(permission);
            summary.setTotalCount(timesheets.size()); // total per user

            userSummaryMap.put(userId, summary);
        }

        List<UserTimesheetResponseDto> finalResponse = new ArrayList<>();

        for (Long userId : userSummaryMap.keySet()) {
            UserTimesheetResponseDto userResponse = new UserTimesheetResponseDto();
            userResponse.setSummary(userSummaryMap.get(userId));
            userResponse.setTimesheets(userTimesheetListMap.getOrDefault(userId, new ArrayList<>()));

            finalResponse.add(userResponse);
        }

        return finalResponse;
    }

    private Long toLong(Object obj) {
        try {
            return obj != null ? Long.parseLong(obj.toString()) : null;
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

    // Update the parseTimeToDuration method to handle SQL Time correctly
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

    // Format Duration in "XXh XXm" format
    private String formatDuration(Duration duration) {
        if (duration == null || duration.isZero()) {
            return "00h 00m"; // Return this when duration is zero
        }
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        return String.format("%02dh %02dm", hours, minutes); // Format as "09h 00m"
    }

    // Method to format LocalTime to 12-hour format (AM/PM)
    private String formatTime(LocalTime localTime) {
        if (localTime == null) {
            return "00:00";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a"); // 12-hour format with AM/PM
        return localTime.format(formatter);
    }

    @Override
    public Optional<TimesheetEntity> findByUserIdAndDate(Long userId, LocalDate date) {
        return timesheetRepository.findByUserIdAndDate(userId, date);
    }

    @Override
    public TimesheetEntity saveTimesheet(TimesheetEntity timesheet) {
        return timesheetRepository.save(timesheet);
    }

    @Override
    public TimesheetHistoryEntity saveTimesheetHistory(TimesheetHistoryEntity history) {
        TimesheetEntity timesheet = timesheetRepository.findByUserIdAndDate(history.getTimesheet().getUserId(), history.getTimesheet().getDate())
                .orElseThrow(() -> new IllegalArgumentException("Timesheet not found for user: " + history.getTimesheet().getUserId()));

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
    public TimesheetEntity findUserIdAndDate(Long userId, LocalDate date) {
        Optional<TimesheetEntity> timesheetOptional = timesheetRepository.findByUserIdAndDate(userId, date);
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
    public List<TimesheetEntity> getLatestLogsByTimesheetIds(List<Long> memberIds, String orgId, LocalDate date) {
        return timesheetHistoryRepository.findLatestLogByTimesheet(memberIds, date);
    }

    @Override
    public TimesheetEntity save(TimesheetEntity timesheet) {
        return timesheetRepository.save(timesheet);
    }

    @Override
    public List<UserAttendanceDto> findAttendanceForUserInRange(List<Long> userId, LocalDate fromDate, LocalDate toDate) {
        return timesheetRepository.findAttendanceForUsersInRange(userId, fromDate, toDate);
    }

    @Override
    public List<UserTimesheetDto> fetchUserTimesheetsWithHistory(LocalDate startDate, LocalDate endDate, List<Long> userIds, String orgId) {
        Long[] userIdArray = userIds.toArray(new Long[0]);

        List<Object[]> resultList = timesheetRepository.fetchUserTimesheetsWithHistory(startDate, endDate, userIdArray, orgId);

        Map<String, UserTimesheetDto> timesheetMap = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        WorkScheduleEntity schedule = workScheduleAdapter.findDefaultActiveSchedule(orgId);

        // Map to collect DTOs grouped by userId
        Map<Long, List<UserTimesheetDto>> userTimesheetListMap = new HashMap<>();

        for (Object[] row : resultList) {

            log.info("---- Row Start ----");
            for (int i = 0; i < row.length; i++) {
                log.info("Column {}: {}", i, row[i]);
            }
            log.info("---- Row End ----");

            if (row[1] == null) continue;

            Long userId = toLong(row[1]);
            LocalDate date = toLocalDate(row[0]);
            if (date == null) continue;

            String compositeKey = userId + "-" + date;

            UserTimesheetDto dto = timesheetMap.computeIfAbsent(compositeKey, key -> {
                UserTimesheetDto newDto = new UserTimesheetDto();

                newDto.setDate(date);
                newDto.setUserId(userId);
                newDto.setUserName((String) row[2]);
                newDto.setWorkScheduleName((String) row[5]);
                newDto.setFirstClockIn(toLocalTime(row[8]));
                newDto.setLastClockOut(toLocalTime(row[9]));
                newDto.setTrackedHours(parseTimeToDuration(row[10], "tracked_hours"));
                newDto.setRegularHours(parseTimeToDuration(row[11], "regular_hours"));
                newDto.setStatus((String) row[13]);

                newDto.setWorkStatus((String) row[16]);
                newDto.setHistory(new ArrayList<>());
                newDto.setFirstClockInTime(formatTime(newDto.getFirstClockIn()));
                newDto.setLastClockOutTime(formatTime(newDto.getLastClockOut()));
                newDto.setTrackedHoursDuration(formatDuration(newDto.getTrackedHours()));
                newDto.setRegularHoursDuration(formatDuration(newDto.getRegularHours()));

                return newDto;
            });

            // Add history if present
            if (row[17] != null) {
                TimesheetHistoryDto historyDto = new TimesheetHistoryDto();
                historyDto.setTimesheetHistoryId(toLong(row[17]));
                historyDto.setLogTime(toLocalTime(row[18]));

                try {
                    historyDto.setLogType(row[19] != null ? LogType.valueOf(row[19].toString()) : null);
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid LogType: " + row[19]);
                }

                historyDto.setLocationId(toLong(row[20]));

                try {
                    historyDto.setLogFrom(row[21] != null ? LogFrom.valueOf(row[21].toString()) : null);
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid LogFrom: " + row[21]);
                }

                historyDto.setLoggedTimestamp(toLocalDateTime(row[22]));
                dto.getHistory().add(historyDto);
            }

            // Add dto to user list map (group by userId)
            userTimesheetListMap.computeIfAbsent(userId, k -> new ArrayList<>());
            List<UserTimesheetDto> userTimesheets = userTimesheetListMap.get(userId);

            // Add dto if not already present for this date
            if (userTimesheets.stream().noneMatch(t -> t.getDate().equals(dto.getDate()))) {
                userTimesheets.add(dto);
            }
        }

        // Flatten all DTOs from map to a single list
        List<UserTimesheetDto> finalResponse = userTimesheetListMap.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        Map<Long, Set<DayOfWeek>> userWorkingDaysMap = new HashMap<>();

        for (Long userId : userIds) {
            WorkScheduleEntity ws = workScheduleAdapter.getScheduleForUser(userId); // get user work schedule

            Set<DayOfWeek> workingDays = new HashSet<>();
            if (ws.getType().getType().name().equalsIgnoreCase(FLEXIBLE.getScheduleType())) { // Flexible
                List<FlexibleWorkScheduleEntity> flexDays = workScheduleAdapter.findByWorkScheduleId(ws.getScheduleId());
                for (FlexibleWorkScheduleEntity f : flexDays) {
                    workingDays.add(DayOfWeek.valueOf(f.getDay().toString()));
                }
            } else if (ws.getType().getType().name().equalsIgnoreCase(FIXED.getScheduleType())) { // Fixed
                List<FixedWorkScheduleEntity> fixedDays = workScheduleAdapter.findByFixedScheduleId(ws.getScheduleId());
                for (FixedWorkScheduleEntity f : fixedDays) {
                    workingDays.add(DayOfWeek.valueOf(f.getDay().toString()));
                }
            }
            userWorkingDaysMap.put(userId, workingDays);
        }

        // Build per-user summary
        Map<Long, TimesheetSummaryDto> userSummaryMap = new HashMap<>();
        for (Map.Entry<Long, List<UserTimesheetDto>> entry : userTimesheetListMap.entrySet()) {
            Long userId = entry.getKey();
            List<UserTimesheetDto> timesheets = entry.getValue();

            int present = 0, absent = 0, holiday = 0, notMarked = 0, paidLeave = 0, halfDay = 0, permission = 0;
            for (UserTimesheetDto t : timesheets) {
                DayOfWeek currentDay = t.getDate().getDayOfWeek();
                log.info("Current Day:{}", currentDay);
                Set<DayOfWeek> userWorkingDays = userWorkingDaysMap.getOrDefault(t.getUserId(), Collections.emptySet());

                if (!userWorkingDays.contains(currentDay)) {
                    // It's a rest day for the user
                    holiday++;
                    if (t.getStatus() == null) {
                        t.setStatus(TimesheetStatusEnum.HOLIDAY.getLabel());
                    }
                } else if (t.getStatus() == null) {
                    if (t.getDate().equals(LocalDate.now())) {
                        notMarked++;
                        t.setStatus(TimesheetStatusEnum.NOT_MARKED.getLabel());
                    } else {
                        absent++;
                        t.setStatus(TimesheetStatusEnum.ABSENT.getLabel());
                    }
                }
                else {
                    switch (t.getStatus()) {
                        case "Present" -> present++;
                        case "Paid Leave" -> paidLeave++;
                        case "Half Day" -> halfDay++;
                        case "Permission" -> permission++;
                    }
                }
            }

        }
        return finalResponse;
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
    public List<UserDashboard> getDashboard(String orgId, List<Long> userIds, LocalDate fromDate, LocalDate toDate) {
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
}
