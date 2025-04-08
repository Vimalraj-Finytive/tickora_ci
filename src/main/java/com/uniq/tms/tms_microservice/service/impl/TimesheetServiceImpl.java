package com.uniq.tms.tms_microservice.service.impl;


import com.uniq.tms.tms_microservice.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.dto.LogType;
import com.uniq.tms.tms_microservice.dto.Timeperiod;
import com.uniq.tms.tms_microservice.dto.TimesheetDto;
import com.uniq.tms.tms_microservice.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.entity.TimesheetHistoryEntity;
import com.uniq.tms.tms_microservice.mapper.TimesheetDtoMapper;
import com.uniq.tms.tms_microservice.mapper.TimesheetEntityMapper;
import com.uniq.tms.tms_microservice.model.TimesheetHistory;
import com.uniq.tms.tms_microservice.service.TimesheetService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

    public TimesheetServiceImpl(TimesheetAdapter timesheetAdapter, TimesheetEntityMapper timesheetEntityMapper, TimesheetDtoMapper timesheetDtoMapper) {
        this.timesheetAdapter = timesheetAdapter;
        this.timesheetEntityMapper = timesheetEntityMapper;
        this.timesheetDtoMapper = timesheetDtoMapper;
    }


    public List<TimesheetDto> getAllTimesheets(LocalDate date, String timePeriod, Long userId) {
        LocalDate startDate = null;
        LocalDate endDate = null;

        if (date != null && timePeriod != null) {
            LocalDateRange range = calculateDateRange(date, timePeriod);
            startDate = range.startDate();
            endDate = range.endDate();
        }

        return timesheetAdapter.filterTimesheetsForAllUsers(startDate, endDate, userId);
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

                        System.out.println("New Timesheet Created for User: " + userId + " on Date: " + finalDate);
                        return timesheetAdapter.saveTimesheet(newTimesheet);
                    });

            history.setTimesheet(timesheet);
            savedEntities.add(timesheetAdapter.saveTimesheetHistory(history));

            if (history.getLogType() == LogType.CLOCK_IN) {
                if (timesheet.getFirstClockIn() == null) {
                    timesheet.setFirstClockIn(history.getLogTime());
                    System.out.println("First Clock-In Set: " + history.getLogTime());
                }
            } else if (history.getLogType() == LogType.CLOCK_OUT) {
                timesheet.setLastClockOut(history.getLogTime());
                System.out.println("Last Clock-Out Set: " + history.getLogTime());
            }
            timesheetAdapter.saveTimesheet(timesheet);
        }

        System.out.println("Calculating Tracked and Break Hours...");
        timesheetAdapter.calculateTrackedAndBreakHours(savedEntities);

        return savedEntities.stream()
                .map(timesheetEntityMapper::toMiddleware)
                .toList();
    }

    @Override
    public TimesheetDto updateClockInOut(Long userId, LocalDate date, TimesheetDto request) {
        TimesheetEntity timesheet = timesheetAdapter.findUserIdAndDate(userId, date);

        if (timesheet == null) {
            return null;
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
            long workedHours = workedDuration.toHours();
            long workedMinutes = workedDuration.toMinutesPart();

            timesheet.setRegularHours(LocalTime.of((int) workedHours, (int) workedMinutes));

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

