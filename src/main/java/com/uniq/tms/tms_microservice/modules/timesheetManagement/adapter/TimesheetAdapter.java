package com.uniq.tms.tms_microservice.modules.timesheetManagement.adapter;

import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.LogType;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.PaginationResponseDto;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.UserAttendanceDto;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.UserTimesheetDto;
import com.uniq.tms.tms_microservice.modules.userManagement.projections.UserDashboard;
import com.uniq.tms.tms_microservice.modules.locationManagement.entity.LocationEntity;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.TimesheetHistoryEntity;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.TimesheetStatusEntity;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TimesheetAdapter {

    PaginationResponseDto filterTimesheetsForAllUsers(LocalDate startDate, LocalDate endDate, List<String> userIds, String orgId, Integer pageIndex, Integer pageSize);
    Optional<TimesheetEntity> findByUserIdAndDate(String userId, LocalDate date);
    TimesheetEntity saveTimesheet(TimesheetEntity timesheet);
    TimesheetHistoryEntity saveTimesheetHistory(TimesheetHistoryEntity history);
    void calculateTrackedAndBreakHours(List<TimesheetHistoryEntity> savedLogs);
    TimesheetEntity save(TimesheetEntity timesheet);
    TimesheetEntity findUserIdAndDate(String userId, LocalDate date);
    List<TimesheetEntity> findActiveTimesheetsByDate(LocalDate today);
    void saveAll(List<TimesheetEntity> openClockIns);
    List<TimesheetEntity> getLatestLogsByTimesheetIds(List<String> memberIds, String orgId, LocalDate date);
    List<UserAttendanceDto> findAttendanceForUserInRange(List<String> userId, LocalDate fromDate, LocalDate toDate);
    List<UserTimesheetDto> fetchUserTimesheetsWithHistory(LocalDate startDate, LocalDate endDate, List<String> userIds, String orgId);
    void updateTimesheetHistory(Long id, LogType logType, LocalTime firstClockIn);
    void saveAllTimesheetHistories(List<TimesheetHistoryEntity> historyEntries);
    List<UserDashboard> getDashboard(String orgId, List<String> userIds, LocalDate fromDate, LocalDate toDate);
    List<TimesheetStatusEntity> getStatus();
    Optional<TimesheetStatusEntity> findById(String status);
    Optional<TimesheetStatusEntity> findByStatusName(String label);
    List<TimesheetEntity> findUserByStatusId(List<String> statusId, LocalDate startDate, LocalDate endDate);
    LocationEntity getDefaultLocation(String orgId);
    List<String> findUserByStatusIdNotIn(LocalDate startDate, LocalDate endDate);
    long countByUserIdsAndDateAndStatus(List<String> userIds, LocalDate date, String status);
    long countActiveUsers(String organizationId, LocalDate fromDate, LocalDate toDate);
}
