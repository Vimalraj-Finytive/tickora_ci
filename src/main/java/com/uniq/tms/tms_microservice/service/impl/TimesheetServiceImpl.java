package com.uniq.tms.tms_microservice.service.impl;


import com.uniq.tms.tms_microservice.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.dto.LogType;
import com.uniq.tms.tms_microservice.dto.Timeperiod;
import com.uniq.tms.tms_microservice.dto.TimesheetDto;
import com.uniq.tms.tms_microservice.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.entity.TimesheetHistoryEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    public List<TimesheetDto> getAllTimesheets(LocalDate date, String timePeriod, Long userId) {
        LocalDate startDate = null;
        LocalDate endDate = null;

        if (date != null && timePeriod != null) {
            LocalDateRange range = calculateDateRange(date, timePeriod);
            startDate = range.startDate();
            endDate = range.endDate();
        }

        List<TimesheetDto> timesheetDtos = timesheetAdapter.filterTimesheetsForAllUsers(startDate, endDate, userId);

        List<UserEntity> allUsers = (userId != null)
                ? Collections.singletonList(userAdapter.getUserById(userId))
                : userAdapter.getAllUsers();

        return fillMissingDates(timesheetDtos, allUsers, startDate, endDate);
    }


    private List<TimesheetDto> fillMissingDates(List<TimesheetDto> existingDtos, List<UserEntity> allUsers, LocalDate startDate, LocalDate endDate) {
        Map<String, TimesheetDto> existingMap = new HashMap<>();

        for (TimesheetDto dto : existingDtos) {
            String key = dto.getUserId() + "|" + dto.getDate();
            existingMap.put(key, dto);
        }

        List<TimesheetDto> finalList = new ArrayList<>();

        for (UserEntity user : allUsers) {
            for (LocalDate currentDate = startDate; !currentDate.isAfter(endDate); currentDate = currentDate.plusDays(1)) {
                String key = user.getUserId() + "|" + currentDate;
                if (existingMap.containsKey(key)) {
                    finalList.add(existingMap.get(key));
                } else {
                    TimesheetDto newDto = new TimesheetDto();
                    newDto.setUserId(user.getUserId());
                    newDto.setUserName(user.getUserName());
                    newDto.setRole(user.getRole().getName());
                    newDto.setDate(currentDate);
                    newDto.setFirstClockIn(LocalTime.MIDNIGHT);
                    newDto.setLastClockOut(LocalTime.MIDNIGHT);
                    newDto.setTrackedHours(LocalTime.MIDNIGHT);
                    newDto.setRegularHours(LocalTime.MIDNIGHT);
                    newDto.setDayType("Holiday");
                    newDto.setHistory(new ArrayList<>());
                    finalList.add(newDto);
                }
            }
        }

        return finalList;
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

