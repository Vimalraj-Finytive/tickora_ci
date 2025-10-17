package com.uniq.tms.tms_microservice.modules.timesheetManagement.facade;

import com.uniq.tms.tms_microservice.modules.userManagement.dto.UserValidationDto;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.*;
import com.uniq.tms.tms_microservice.shared.security.schema.TenantContext;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.mapper.TimesheetDtoMapper;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.model.TimesheetHistory;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.services.FaceService;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.services.ReportService;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.services.TimesheetService;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.RegisterDto;
import com.uniq.tms.tms_microservice.shared.security.user.CustomUserDetails;
import com.uniq.tms.tms_microservice.shared.util.TimesheetLogParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class TimesheetFacade {

    private final AuthHelper authHelper;
    private final TimesheetService timesheetService;
    private final TimesheetDtoMapper timesheetDtoMapper;
    private final ReportService reportService;
    private final FaceService faceService;

    public TimesheetFacade(AuthHelper authHelper, TimesheetService timesheetService, TimesheetDtoMapper timesheetDtoMapper, ReportService reportService, FaceService faceService) {
        this.authHelper = authHelper;
        this.timesheetService = timesheetService;
        this.timesheetDtoMapper = timesheetDtoMapper;
        this.reportService = reportService;
        this.faceService = faceService;
    }

    private final Logger log = LoggerFactory.getLogger(TimesheetFacade.class);

    public List<UserDashboardDto> getAllUserInfo(DashboardDto request) {
        LocalDate fromDate = request.getFromDate();
        LocalDate toDate = request.getToDate();
        if (toDate.isAfter(LocalDate.now(ZoneId.of("Asia/Kolkata"))
        )) {
            toDate = LocalDate.now(ZoneId.of("Asia/Kolkata"))
            ;
        }
        String userId = request.getUserId();
        String orgId = authHelper.getOrgId();
        String userIdFromToken = authHelper.getUserId();

        if (orgId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        return timesheetService.getAllUserInfo(orgId, userIdFromToken, fromDate, toDate, userId, request.getGroupIds(), request.getType());
    }

    public List<UserTimesheetDto> getUserTimesheets(TimesheetReportDto request) {

        String orgId = authHelper.getOrgId();
        String userIdFromToken = authHelper.getUserId();
        if (orgId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        String role = authHelper.getRole();
        return timesheetService.getUserTimesheets(userIdFromToken, orgId, role, request);
    }

    public PaginationResponseDto getAllTimesheets(TimesheetReportDto request) {
        String orgId = authHelper.getOrgId();
        String userIdFromToken = authHelper.getUserId();
        if (orgId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        String role = authHelper.getRole();
        return timesheetService.getAllTimesheets(userIdFromToken, orgId, role, request);
    }

    public List<TimesheetHistoryDto> processTimesheetLogs(List<TimesheetHistoryDto> timesheetLogs) {
        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        List<TimesheetHistory> middlewareLogs = timesheetLogs.stream()
                .map(timesheetDtoMapper::toMiddleware)
                .toList();
        List<TimesheetHistory> savedLogs = timesheetService.processTimesheetLogs(middlewareLogs);
        return savedLogs.stream()
                .map(timesheetDtoMapper::toDto)
                .toList();
    }


    public TimesheetDto updateClockInOut(String userId, LocalDate date, TimesheetDto request, String orgId) {
        CustomUserDetails user = authHelper.getCurrentUser();
        String userIdFromToken = user.getUserId();
        String role = user.getRole();
        return timesheetService.updateClockInOut(userIdFromToken, role, userId, date, request, orgId);
    }

    public TimesheetDto upsertClockInOut(String userIdFromToken, String userId, LocalDate date, TimesheetDto request, String orgId, String role) {
        orgId = authHelper.getOrgId();
        if (orgId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        CustomUserDetails user = authHelper.getCurrentUser();
        role = user.getRole();
        return timesheetService.updateClockInOut(userIdFromToken, role, userId, date, request, orgId);
    }

    public ApiResponse getStatus() {

        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        List<TimesheetStatusDto> status = timesheetService.getStatus().stream()
                .map(timesheetDtoMapper::toStatusDto).
                toList();
        return new ApiResponse<>(200,"Timesheet Status fetched successfully",status);
    }

    public ApiResponse<RegisterDto> registerUserFace(RegisterDto registerDto) {
        String orgSchema = authHelper.getSchema();
        return faceService.UserFaceRegister(registerDto,orgSchema);
    }

    public ApiResponse<ClockInOutRequestDto> clockInOutUser(ClockInOutRequestDto registerDto) {
        String orgSchema = authHelper.getSchema();
        log.info("Incoming timesheetLogsJson: {}", registerDto.getTimesheetLogsJson());

        try {
            List<TimesheetHistoryDto> logs = TimesheetLogParserUtil.parseLogs(registerDto.getTimesheetLogsJson());
            registerDto.setTimesheetLogs(logs);
            return faceService.clockInOutUser(registerDto, orgSchema);
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse timesheet logs", e);
            return new ApiResponse<>(40,"Invalid timesheet data format: " + e.getMessage(),null);
        }
    }

    public ApiResponse<RegisterDto> compareMultiFace(FaceDto faceDto) {
        String orgSchema = authHelper.getSchema();
        return faceService.compareMultiFace(faceDto,orgSchema);
    }

    public CompletableFuture<FileExportResponseDto> generateTimesheetFileAsync(
            TimesheetReportDto request,
            String predefinedFileName,
            String userId,
            String orgId,
            String role,
            String schema) {

        TenantContext.setCurrentTenant(schema.toLowerCase().replace("-", "_"));
        try {
            log.info("File generation started for user: {}, org: {}", userId, orgId);

            FileExportResponseDto export = reportService.generateTimesheetFile(
                    request, userId, orgId, role, predefinedFileName);

            log.info("File Generation completed for file: {}", predefinedFileName);
            return CompletableFuture.completedFuture(export);

        } catch (Exception e) {
            log.error("Error in async file generation for file: {}", predefinedFileName, e);
            return CompletableFuture.failedFuture(e);
        } finally {
            TenantContext.clear();
        }
    }

    public ApiResponse<UserValidationDto> validateUser(String userId) {
        String orgSchema = authHelper.getSchema();
        return faceService.validateUser(userId);
    }
}
