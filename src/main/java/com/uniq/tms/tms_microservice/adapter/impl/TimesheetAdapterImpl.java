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
        List<Object[]> resultList = timesheetRepository.fetchTimesheetsWithHistory(startDate, endDate, userId);
        System.out.println("Result List Size: " + (resultList == null ? "null" : resultList.size()));

        Map<Long, TimesheetDto> timesheetMap = new LinkedHashMap<>();

        for (Object[] row : resultList) {
            if (row[1] == null) continue;

            Long timesheetId = row[1] != null ? Long.parseLong(row[1].toString()) : null;

            TimesheetDto dto = timesheetMap.computeIfAbsent(timesheetId, id -> {
                TimesheetDto newDto = new TimesheetDto();
                newDto.setId(timesheetId);
                newDto.setUserId(row[2] != null ? Long.parseLong(row[2].toString()) : null);
                newDto.setDate(row[3] != null ? ((java.sql.Date) row[3]).toLocalDate() : null);
                newDto.setUserName((String) row[4]);
                newDto.setRole((String) row[5]);
                newDto.setDayType((String) row[6]);

                // Handle first_clock_in (java.sql.Time to LocalTime)
                newDto.setFirstClockIn(row[7] != null ? ((java.sql.Time) row[7]).toLocalTime() : null);

                // Handle last_clock_out (java.sql.Time to LocalTime)
                newDto.setLastClockOut(row[8] != null ? ((java.sql.Time) row[8]).toLocalTime() : null);

                // Handle tracked_hours
                if (row[9] != null) {
                    if (row[9] instanceof java.sql.Time) {
                        // Convert java.sql.Time to LocalTime
                        newDto.setTrackedHours(((java.sql.Time) row[9]).toLocalTime());
                    } else if (row[9] instanceof String) {
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
                        System.err.println("Unexpected type for tracked_hours: " + row[9].getClass());
                    }
                }

                if (row[10] != null) {
                    if (row[10] instanceof java.sql.Time) {
                        // Convert java.sql.Time to LocalTime
                        newDto.setRegularHours(((java.sql.Time) row[10]).toLocalTime());
                    } else if (row[10] instanceof String) {
                        // If it's a String, parse it
                        String timeString = (String) row[10];
                        try {
                            LocalTime regularHours = LocalTime.parse(timeString);
                            newDto.setRegularHours(regularHours);
                        } catch (DateTimeParseException e) {
                            // Handle error if parsing fails
                            System.err.println("Error parsing tracked_hours: " + timeString);
                            newDto.setRegularHours(LocalTime.MIDNIGHT);
                        }
                    } else {
                        System.err.println("Unexpected type for tracked_hours: " + row[10].getClass());
                    }
                }

                newDto.setUserDayType((String) row[11]);
                newDto.setWorkStatus((String) row[12]);
                newDto.setHistory(new ArrayList<>());
                return newDto;
            });


            System.out.println("Row Data: " + Arrays.toString(row));

            if (row[13] != null) {
                TimesheetHistoryDto historyDto = new TimesheetHistoryDto();
                historyDto.setTimesheetHistoryId(row[13] != null ? Long.parseLong(row[13].toString()) : null);
                historyDto.setLogTime(row[14] != null ? ((java.sql.Time) row[14]).toLocalTime() : null);

                try {
                    historyDto.setLogType(row[15] != null ? LogType.valueOf(row[15].toString()) : null);
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid LogType: " + row[15]);
                    e.printStackTrace();
                }

                try {
                    historyDto.setLocationId(row[16] != null ? Long.parseLong(row[16].toString()) : null);
                } catch (NumberFormatException e) {
                    System.err.println("LocationId parsing error: " + row[16]);
                    e.printStackTrace();
                }

                try {
                    historyDto.setLogFrom(row[17] != null ? LogFrom.valueOf(row[17].toString()) : null);
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid LogFrom: " + row[17]);
                    e.printStackTrace();
                }

                try {
                    historyDto.setLoggedTimestamp(row[18] != null ? ((java.sql.Timestamp) row[18]).toLocalDateTime() : null);
                } catch (ClassCastException e) {
                    System.err.println("LoggedTimestamp conversion error: " + row[18]);
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
    public TimesheetEntity save(TimesheetEntity timesheet) {
        return timesheetRepository.save(timesheet);
    }


}

