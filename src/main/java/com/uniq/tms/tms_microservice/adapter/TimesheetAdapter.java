package com.uniq.tms.tms_microservice.adapter;

import com.uniq.tms.tms_microservice.dto.LogType;
import com.uniq.tms.tms_microservice.dto.UserAttendanceDto;
import com.uniq.tms.tms_microservice.dto.UserDashboard;
import com.uniq.tms.tms_microservice.dto.UserTimesheetDto;
import com.uniq.tms.tms_microservice.dto.UserTimesheetResponseDto;
import com.uniq.tms.tms_microservice.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.entity.TimesheetHistoryEntity;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TimesheetAdapter {

    List<UserTimesheetResponseDto> filterTimesheetsForAllUsers(LocalDate startDate, LocalDate endDate, List<Long> userIds, Long orgId);
    Optional<TimesheetEntity> findByUserIdAndDate(Long userId, LocalDate date);
    TimesheetEntity saveTimesheet(TimesheetEntity timesheet);
    TimesheetHistoryEntity saveTimesheetHistory(TimesheetHistoryEntity history);
    void calculateTrackedAndBreakHours(List<TimesheetHistoryEntity> savedLogs);
    TimesheetEntity save(TimesheetEntity timesheet);
    TimesheetEntity findUserIdAndDate(Long userId, LocalDate date);
    List<TimesheetEntity> findActiveTimesheetsByDate(LocalDate today);
    void saveAll(List<TimesheetEntity> openClockIns);
    List<TimesheetEntity> getLatestLogsByTimesheetIds(List<Long> memberIds, Long orgId, LocalDate date);
    List<UserDashboard> findAllByOrgIdExcludingUser(Long orgId, List<Long> userIds, LocalDate fromDate, LocalDate toDate, boolean isSuperAdmin, Long loggedInUserId, Long userId);
    List<UserAttendanceDto> findAttendanceForUserInRange(List<Long> userId, LocalDate fromDate, LocalDate toDate);
    List<UserTimesheetDto> fetchUserTimesheetsWithHistory(LocalDate startDate, LocalDate endDate, List<Long> userIds, Long orgId);
    void updateTimesheetHistory(Long id, LogType logType, LocalTime firstClockIn);
    void saveAllTimesheetHistories(List<TimesheetHistoryEntity> historyEntries);
    List<UserDashboard> getDashboard(Long orgId, List<Long> userIds, LocalDate fromDate, LocalDate toDate);
}
