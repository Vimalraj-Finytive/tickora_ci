package com.uniq.tms.tms_microservice.adapter.impl;

import com.uniq.tms.tms_microservice.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.dto.LogFrom;
import com.uniq.tms.tms_microservice.dto.LogType;
import com.uniq.tms.tms_microservice.dto.TimesheetDto;
import com.uniq.tms.tms_microservice.dto.TimesheetHistoryDto;
import com.uniq.tms.tms_microservice.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.entity.TimesheetHistoryEntity;
import com.uniq.tms.tms_microservice.mapper.TimesheetDtoMapper;
import com.uniq.tms.tms_microservice.mapper.TimesheetEntityMapper;
import com.uniq.tms.tms_microservice.repository.TimesheetHistoryRepository;
import com.uniq.tms.tms_microservice.repository.TimesheetRepository;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final TimesheetDtoMapper timesheetDtoMapper;
    private final TimesheetEntityMapper timesheetEntityMapper;

    public TimesheetAdapterImpl(TimesheetRepository timesheetRepository, TimesheetHistoryRepository timesheetHistoryRepository, TimesheetDtoMapper timesheetDtoMapper, TimesheetEntityMapper timesheetEntityMapper) {
        this.timesheetRepository = timesheetRepository;
        this.timesheetHistoryRepository = timesheetHistoryRepository;
        this.timesheetDtoMapper = timesheetDtoMapper;
        this.timesheetEntityMapper = timesheetEntityMapper;
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");

    @Override
    public List<TimesheetDto> filterTimesheetsForAllUsers(LocalDate startDate, LocalDate endDate, Long userId) {
        System.out.println("StartDate: " + startDate + ", EndDate: " + endDate + ", userId: " + userId);
        List<Object[]> resultList = timesheetRepository.fetchTimesheetsWithHistory(startDate, endDate, userId);
        System.out.println("Result List Size: " + (resultList == null ? "null" : resultList.size()));

        Map<Long, TimesheetDto> timesheetMap = new LinkedHashMap<>();

        for (Object[] row : resultList) {
            if (row[1] == null) continue;

            Long timesheetId = row[4] != null ? Long.parseLong(row[4].toString()) : null;

            TimesheetDto dto = timesheetMap.computeIfAbsent(timesheetId, id -> {
                TimesheetDto newDto = new TimesheetDto();
                newDto.setId(timesheetId);
                newDto.setUserId(row[1] != null ? Long.parseLong(row[1].toString()) : null);
                newDto.setDate(row[0] != null ?
                        (row[0] instanceof java.sql.Date ?
                                ((java.sql.Date) row[0]).toLocalDate() :
                                (row[0] instanceof java.time.Instant ?
                                        ((java.time.Instant) row[0]).atZone(ZoneId.systemDefault()).toLocalDate() :
                                        null))
                        : null);
                newDto.setUserName((String) row[2]);
                newDto.setRole((String) row[3]);
                newDto.setDayType((String) row[9]);

                // Handle first_clock_in (java.sql.Time to LocalTime)
                newDto.setFirstClockIn(row[5] != null ? ((java.sql.Time) row[5]).toLocalTime() : null);

                // Handle last_clock_out (java.sql.Time to LocalTime)
                newDto.setLastClockOut(row[6] != null ? ((java.sql.Time) row[6]).toLocalTime() : null);

                // Handle tracked_hours
                if (row[7] != null) {
                    if (row[7] instanceof java.sql.Time) {
                        // Convert java.sql.Time to LocalTime
                        newDto.setTrackedHours(((java.sql.Time) row[7]).toLocalTime());
                    } else if (row[7] instanceof String) {
                        // If it's a String, parse it
                        String timeString = (String) row[7];
                        try {
                            LocalTime trackedHours = LocalTime.parse(timeString);
                            newDto.setTrackedHours(trackedHours);
                        } catch (DateTimeParseException e) {
                            // Handle error if parsing fails
                            System.err.println("Error parsing tracked_hours: " + timeString);
                            newDto.setTrackedHours(LocalTime.MIDNIGHT); // Default value on error
                        }
                    } else {
                        System.err.println("Unexpected type for tracked_hours: " + row[7].getClass());
                    }
                }

                if (row[8] != null) {
                    if (row[8] instanceof java.sql.Time) {
                        // Convert java.sql.Time to LocalTime
                        newDto.setRegularHours(((java.sql.Time) row[8]).toLocalTime());
                    } else if (row[8] instanceof String) {
                        // If it's a String, parse it
                        String timeString = (String) row[8];
                        try {
                            LocalTime regularHours = LocalTime.parse(timeString);
                            newDto.setRegularHours(regularHours);
                        } catch (DateTimeParseException e) {
                            // Handle error if parsing fails
                            System.err.println("Error parsing tracked_hours: " + timeString);
                            newDto.setRegularHours(LocalTime.MIDNIGHT);
                        }
                    } else {
                        System.err.println("Unexpected type for tracked_hours: " + row[8].getClass());
                    }
                }

                newDto.setUserDayType((String) row[10]);
                newDto.setWorkStatus((String) row[11]);
                newDto.setHistory(new ArrayList<>());
                return newDto;
            });


            System.out.println("Row Data: " + Arrays.toString(row));

            if (row[12] != null) {
                TimesheetHistoryDto historyDto = new TimesheetHistoryDto();
                historyDto.setTimesheetHistoryId(row[12] != null ? Long.parseLong(row[12].toString()) : null);
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

    private LocalDate parseDate(Object obj) {
        if (obj == null) return null;
        try {
            return LocalDateTime.parse(obj.toString(), DATE_TIME_FORMATTER)
                    .atZone(ZoneOffset.UTC)
                    .withZoneSameInstant(ZoneId.systemDefault())
                    .toLocalDate();
        } catch (Exception e) {
            return LocalDate.parse(obj.toString().substring(0, 10));
        }
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

            System.out.println("\n=== Processing Timesheet ID: " + timesheetId + " ===");
            System.out.println("Initial Tracked: " + LocalTime.ofSecondOfDay(trackedSeconds));
            System.out.println("Initial Break: " + LocalTime.ofSecondOfDay(breakSeconds));
            System.out.println("Initial Regular Hours: " + LocalTime.ofSecondOfDay(regularSeconds));
            System.out.println("Initial Last Clock-Out: " + lastClockOut);

            for (TimesheetHistoryEntity log : historyLogs) {
                System.out.println("Processing Log: " + log.getLogType() + " at " + log.getLogTime());

                if (log.getLogType() == LogType.CLOCK_IN) {
                    if (lastClockOut != null) {
                        long breakTime = Duration.between(lastClockOut, log.getLogTime()).getSeconds();
                        if (breakTime > 0) {
                            System.out.println("Break Time: " + LocalTime.ofSecondOfDay(breakTime));
                            breakSeconds += breakTime;
                        }
                    }
                    lastClockIn = log.getLogTime();
                } else if (log.getLogType() == LogType.CLOCK_OUT && lastClockIn != null) {
                    long trackedTime = Duration.between(lastClockIn, log.getLogTime()).getSeconds();
                    if (trackedTime > 0) {
                        System.out.println("Tracked Time: " + LocalTime.ofSecondOfDay(trackedTime));
                        trackedSeconds += trackedTime;
                        regularSeconds += trackedTime;
                    }
                    lastClockOut = log.getLogTime();
                }
            }

            System.out.println("Final Tracked Hours: " + LocalTime.ofSecondOfDay(trackedSeconds));
            System.out.println("Final Break Hours: " + LocalTime.ofSecondOfDay(breakSeconds));
            System.out.println("Final Regular Hours: " + LocalTime.ofSecondOfDay(regularSeconds));
            System.out.println("Final Last Clock-Out: " + lastClockOut);

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
    public List<TimesheetEntity> findByFirstClockInNotNullAndLastClockOutIsNullAndDate(LocalDate today) {
        return timesheetRepository.findByFirstClockInNotNullAndLastClockOutIsNullAndDate(today);
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

