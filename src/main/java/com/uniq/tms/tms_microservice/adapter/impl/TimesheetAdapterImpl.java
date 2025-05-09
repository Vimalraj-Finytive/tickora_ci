package com.uniq.tms.tms_microservice.adapter.impl;

import com.uniq.tms.tms_microservice.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.dto.LogFrom;
import com.uniq.tms.tms_microservice.dto.LogType;
import com.uniq.tms.tms_microservice.dto.TimesheetDto;
import com.uniq.tms.tms_microservice.dto.TimesheetHistoryDto;
import com.uniq.tms.tms_microservice.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.entity.TimesheetHistoryEntity;
import com.uniq.tms.tms_microservice.repository.TimesheetHistoryRepository;
import com.uniq.tms.tms_microservice.repository.TimesheetRepository;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TimesheetAdapterImpl implements TimesheetAdapter {

    private final TimesheetRepository timesheetRepository;
    private final TimesheetHistoryRepository timesheetHistoryRepository;

    public TimesheetAdapterImpl(TimesheetRepository timesheetRepository, TimesheetHistoryRepository timesheetHistoryRepository) {
        this.timesheetRepository = timesheetRepository;
        this.timesheetHistoryRepository = timesheetHistoryRepository;
    }

    @Override
    public List<TimesheetDto> filterTimesheetsForAllUsers(LocalDate startDate, LocalDate endDate, List<Long> userIds) {
        Long[] userIdArray = userIds.toArray(new Long[0]);

        List<Object[]> resultList = timesheetRepository.fetchTimesheetsWithHistory(startDate, endDate, userIdArray);

        Map<String, TimesheetDto> timesheetMap = new LinkedHashMap<>();

        for (Object[] row : resultList) {
            if (row[1] == null) {
                continue;
            }

            Long userId = Long.parseLong(row[1].toString());

            // Extract date and convert properly
            LocalDate date = row[0] != null ?
                    (row[0] instanceof java.sql.Date ?
                            ((java.sql.Date) row[0]).toLocalDate() :
                            (row[0] instanceof java.time.Instant ?
                                    ((java.time.Instant) row[0]).atZone(ZoneId.systemDefault()).toLocalDate() :
                                    null))
                    : null;

            // NEW: Composite key = userId + "-" + date
            String compositeKey = userId + "-" + date;

            TimesheetDto dto = timesheetMap.computeIfAbsent(compositeKey, key -> {
                TimesheetDto newDto = new TimesheetDto();
                newDto.setId(row[4] != null ? Long.parseLong(row[4].toString()) : null);
                newDto.setUserId(userId);
                newDto.setDate(date);
                newDto.setUserName((String) row[2]);
                newDto.setRole((String) row[3]);
                newDto.setDayType((String) row[9]);
                newDto.setFirstClockIn(row[5] != null ? ((java.sql.Time) row[5]).toLocalTime() : null);
                newDto.setLastClockOut(row[6] != null ? ((java.sql.Time) row[6]).toLocalTime() : null);
                // trackedHours
                newDto.setTrackedHours(parseTimeToDuration(row[7], "tracked_hours"));
                // regularHours
                newDto.setRegularHours(parseTimeToDuration(row[8], "regular_hours"));
                newDto.setUserDayType((String) row[10]);
                newDto.setWorkStatus((String) row[11]);
                newDto.setHistory(new ArrayList<>());
                // Format values for display
                newDto.setFirstClockInTime(formatTime(newDto.getFirstClockIn()));
                newDto.setLastClockOutTime(formatTime(newDto.getLastClockOut()));
                newDto.setTrackedHoursDuration(formatDuration(newDto.getTrackedHours()));
                newDto.setRegularHoursDuration(formatDuration(newDto.getRegularHours()));
                return newDto;
            });

            // Add TimesheetHistoryDto
            if (row[12] != null) {
                TimesheetHistoryDto historyDto = new TimesheetHistoryDto();
                historyDto.setTimesheetHistoryId(Long.parseLong(row[12].toString()));
                historyDto.setLogTime(row[13] != null ? ((java.sql.Time) row[13]).toLocalTime() : null);

                try {
                    historyDto.setLogType(row[14] != null ? LogType.valueOf(row[14].toString()) : null);
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid LogType: " + row[14]);
                    e.printStackTrace();
                }

                try {
                    historyDto.setLocationId(row[15] != null ? Long.parseLong(row[15].toString()) : null);
                } catch (NumberFormatException e) {
                    System.err.println("LocationId parsing error: " + row[15]);
                    e.printStackTrace();
                }

                try {
                    historyDto.setLogFrom(row[16] != null ? LogFrom.valueOf(row[16].toString()) : null);
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid LogFrom: " + row[16]);
                    e.printStackTrace();
                }

                try {
                    historyDto.setLoggedTimestamp(row[17] != null ? ((java.sql.Timestamp) row[17]).toLocalDateTime() : null);
                } catch (ClassCastException e) {
                    System.err.println("LoggedTimestamp conversion error: " + row[17]);
                    e.printStackTrace();
                }

                dto.getHistory().add(historyDto);
            }
        }
        return new ArrayList<>(timesheetMap.values());
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
    public List<TimesheetEntity> getLatestLogsByTimesheetIds(List<Long> memberIds, Long orgId, LocalDate date) {
        return timesheetHistoryRepository.findLatestLogByTimesheet(memberIds, date);
    }

    @Override
    public TimesheetEntity save(TimesheetEntity timesheet) {
        return timesheetRepository.save(timesheet);
    }
}
