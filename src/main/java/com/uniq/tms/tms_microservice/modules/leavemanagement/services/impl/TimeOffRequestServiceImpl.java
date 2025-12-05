package com.uniq.tms.tms_microservice.modules.leavemanagement.services.impl;

import com.opencsv.CSVWriter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.LeaveBalanceAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeOffPolicyAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeOffRequestAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.UserPolicyAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffExportRequestDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffRequestEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.UsersRequestMappingEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.TimeOffPolicyDtoMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.TimeOffPolicyEntityMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.projection.TimeOffExportView;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.TimeOffRequestService;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import com.uniq.tms.tms_microservice.shared.dto.EnumModel;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import com.uniq.tms.tms_microservice.shared.util.ReportStyleUtil;
import jakarta.annotation.Nullable;
import com.uniq.tms.tms_microservice.shared.util.CacheKeyUtil;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;

@Service
public class TimeOffRequestServiceImpl implements TimeOffRequestService {

    private static final Logger log = LoggerFactory.getLogger(TimeOffRequestServiceImpl.class);

    private final ZoneId zoneId = ZoneId.of("Asia/Kolkata");

    private final TimeOffRequestAdapter timeOffRequestAdapter;
    private final TimeOffPolicyEntityMapper TimeOffPolicyEntityMapper;
    private final TimeOffPolicyDtoMapper timeOffPolicyDtoMapper;
    private final LeaveBalanceAdapter leaveBalanceAdapter;
    private final TimeOffPolicyAdapter timeOffPolicyAdapter;
    private final UserPolicyAdapter userPolicyAdapter;
    private final AuthHelper authHelper;
    private final CacheKeyUtil cacheKeyUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ReportStyleUtil reportStyleUtil;

    public TimeOffRequestServiceImpl(TimeOffRequestAdapter timeOffRequestAdapter, TimeOffPolicyEntityMapper TimeOffPolicyEntityMapper, TimeOffPolicyDtoMapper timeOffPolicyDtoMapper,
                                     LeaveBalanceAdapter leaveBalanceAdapter, TimeOffPolicyAdapter timeOffPolicyAdapter, UserPolicyAdapter userPolicyAdapter,
                                     AuthHelper authHelper, CacheKeyUtil cacheKeyUtil, @Nullable RedisTemplate<String, Object> redisTemplate, ReportStyleUtil reportStyleUtil) {
        this.timeOffRequestAdapter = timeOffRequestAdapter;
        this.TimeOffPolicyEntityMapper = TimeOffPolicyEntityMapper;
        this.timeOffPolicyDtoMapper = timeOffPolicyDtoMapper;
        this.leaveBalanceAdapter = leaveBalanceAdapter;
        this.timeOffPolicyAdapter = timeOffPolicyAdapter;
        this.userPolicyAdapter = userPolicyAdapter;
        this.authHelper = authHelper;
        this.cacheKeyUtil = cacheKeyUtil;
        this.redisTemplate = redisTemplate;
        this.reportStyleUtil = reportStyleUtil;
    }

    @Value("${csv.request.download.dir}")
    private String downloadDir;

    @Override
    public void createRequest(TimeOffRequest request) {
        boolean validPolicy = timeOffPolicyAdapter.existsValidPolicy(request.getPolicyId(), request.getStartDate(), request.getEndDate());
        boolean validUserPolicy = userPolicyAdapter.isUserPolicyActive(request.getPolicyId(), request.getUserId(), request.getStartDate(), request.getEndDate());
        if (!validPolicy) {
            throw new IllegalStateException("Invalid policy for the given date.");
        }
        if (!validUserPolicy) {
            throw new IllegalStateException("Invalid user policy for the given date.");
        }
        boolean exists = timeOffRequestAdapter.existsTimeoffRequest(request.getUserId(), request.getPolicyId(), LocalDate.now(zoneId));
        if (exists) {
            throw new IllegalArgumentException("Request for today is already pending or approved");
        }
        if (LocalDate.now(zoneId).isAfter(request.getStartDate())) {
            throw new IllegalArgumentException("Invalid request format for the selected entitled type");
        }
        boolean overlap = timeOffRequestAdapter.existsOverlappingRequest(request.getUserId(), request.getPolicyId(), request.getStartDate(), request.getEndDate());

        if (overlap) {
            throw new IllegalArgumentException("Request already exists within this date range.");
        }
        TimeOffPolicyEntity policy = timeOffPolicyAdapter.findPolicyById(request.getPolicyId());
        if (policy.getCompensation() == Compensation.UNPAID) {
            createUnpaidRequest(request, policy);
            return;
        }
        TimeOffRequestEntity entity = new TimeOffRequestEntity();
        LeaveBalanceEntity leaveBalance = leaveBalanceAdapter.findForPeriod(policy.getPolicyId(), request.getUserId(), request.getStartDate(), request.getEndDate());
        double days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        Integer requested = 0;
        boolean invalidDayOrHalfDay =
                (policy.getEntitledType() == EntitledType.DAY || policy.getEntitledType() == EntitledType.HALF_DAY)
                        && (request.getStartTime() != null || request.getEndTime() != null || request.getUnitsRequested() != days);
        boolean invalidHour =
                policy.getEntitledType() == EntitledType.HOURS
                        && (days != 1 ||
                        request.getStartTime() == null || request.getEndTime() == null);
        boolean invalidHalfDay =
                policy.getEntitledType() == EntitledType.HALF_DAY
                        && days != 1;
        if (invalidDayOrHalfDay || invalidHour || invalidHalfDay) {
            throw new IllegalArgumentException("Invalid request format for the selected entitled type");
        }
        if (leaveBalance.getBalanceUnits() == 0.0) {
            throw new IllegalArgumentException("Cannot take paid leave");
        }
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        if (policy.getEntitledType() == EntitledType.DAY) {
            requested = request.getUnitsRequested();
            validateDays(days, leaveBalance.getBalanceUnits());
            entity.setUnitsRequested(request.getUnitsRequested());
        } else if (policy.getEntitledType() == EntitledType.HALF_DAY) {
            requested = request.getUnitsRequested();
            days = request.getUnitsRequested() * 0.5;
            validateDays(days, leaveBalance.getBalanceUnits());
            entity.setUnitsRequested(request.getUnitsRequested());
        } else {
            requested = request.getUnitsRequested();
            double hours = validateHours(request.getStartTime(), request.getEndTime(), request.getUnitsRequested(), leaveBalance);
            if (hours > leaveBalance.getBalanceUnits()) {
                throw new IllegalArgumentException("Insufficient leave balance.");
            }
            entity.setStartTime(request.getStartTime());
            entity.setEndTime(request.getEndTime());
            entity.setUnitsRequested(request.getUnitsRequested());
        }
        UserEntity user = new UserEntity();
        user.setUserId(request.getUserId());
        entity.setUser(user);
        entity.setPolicy(policy);
        entity.setStatus(Status.PENDING);
        entity.setReason(request.getReason());
        entity.setRequestDate(LocalDate.now(zoneId));
        Set<String> uniqueCc=new HashSet<>(request.getCc());
        if (uniqueCc.contains(request.getUserId()) || request.getTo().equals(request.getUserId())){
            throw new IllegalArgumentException("User already included");
        }

        TimeOffRequestEntity saved = timeOffRequestAdapter.saveRequest(entity);
        deductLeaveBalance(saved, requested);


        uniqueCc.remove(request.getTo());
        List<UsersRequestMappingEntity> usersMapping =
                Stream.concat(
                        Stream.of(buildMapping(ViewerType.APPROVER, request.getTo(), request.getUserId(), saved.getTimeOffRequestId())),
                        uniqueCc.stream().map(viewer -> buildMapping(ViewerType.VIEWER, viewer, request.getUserId(), saved.getTimeOffRequestId()))
                ).toList();
        List<UsersRequestMappingEntity> entities = timeOffRequestAdapter.saveUsersRequestMapping(usersMapping);
    }

    private UsersRequestMappingEntity buildMapping(ViewerType type, String viewerId, String requesterId, Long requestId) {
        UsersRequestMappingEntity m = new UsersRequestMappingEntity();
        m.setType(type);
        m.setViewerId(viewerId);
        m.setRequesterId(requesterId);
        m.setTimeOffRequestId(requestId);
        return m;
    }

    private double validateHours(LocalTime startTime, LocalTime endTime, Integer hoursRequested, LeaveBalanceEntity leaveBalance) {
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Invalid duration");
        }
        long minutes = Duration.between(startTime, endTime).toMinutes();
        if (minutes % 60 != 0 || (minutes / 60) != hoursRequested) {
            throw new IllegalArgumentException("Invalid duration");
        }
        return minutes / 60.0;

    }

    private void validateDays(double days, Double balanceUnits) {
        if (balanceUnits - days < 0) {
            throw new IllegalArgumentException("Insufficient leave balance.");
        }
    }

    private void createUnpaidRequest(TimeOffRequest request, TimeOffPolicyEntity policy) {
        TimeOffRequestEntity entity = new TimeOffRequestEntity();
        double days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        if (days != request.getUnitsRequested()) {
            throw new IllegalArgumentException("Invalid request format for the selected entitled type");
        }
        UserEntity user = new UserEntity();
        user.setUserId(request.getUserId());
        entity.setUser(user);
        entity.setPolicy(policy);
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setReason(request.getReason());
        entity.setUnitsRequested(request.getUnitsRequested());
        entity.setRequestDate(LocalDate.now(zoneId));
        entity.setStatus(Status.PENDING);
        timeOffRequestAdapter.saveRequest(entity);
    }

    @Override
    public void employeeUpdateStatus(EmployeeStatusUpdate model) {
        if (model.getStartDate() != null) {
            boolean validUserPolicy = userPolicyAdapter.isUserPolicyActive(model.getPolicyId(), model.getUserId(), model.getStartDate(), model.getEndDate());
            if (!validUserPolicy) {
                throw new IllegalStateException("Invalid policy for the given date.");
            }
        }
        TimeOffRequestEntity entity = timeOffRequestAdapter.getTimeoffRequest(model.getPolicyId(), model.getUserId(), model.getRequestDate());
        if (LocalDate.now(zoneId).isAfter(entity.getStartDate()) || (model.getStatus() != null && !handleEmployeeRules(entity.getStatus(), model.getStatus()))) {
            throw new IllegalArgumentException("Update not allowed");
        }
        if (model.getStatus() != null && model.getStatus() == Status.CANCELLED) {
            if (entity.getPolicy().getCompensation() == Compensation.PAID) {
                Integer requested = entity.getUnitsRequested();
                addLeaveBalance(entity, requested);
            }
            entity.setStatus(model.getStatus());
            timeOffRequestAdapter.saveRequest(entity);
            return;
        }

        double days = ChronoUnit.DAYS.between(entity.getStartDate(), entity.getEndDate()) + 1;

        if (model.getReason() != null) {
            entity.setReason(model.getReason());
        }
        if (model.getStartDate() != null && model.getEndDate() != null) {
            days = ChronoUnit.DAYS.between(model.getStartDate(), model.getEndDate()) + 1;
            entity.setStartDate(model.getStartDate());
            entity.setEndDate(model.getEndDate());
        }
        if (entity.getPolicy().getCompensation() == Compensation.UNPAID) {
            if (model.getUnitsRequested() != null && days != model.getUnitsRequested()) {
                throw new IllegalArgumentException("Invalid paid leave request.");
            }
            timeOffRequestAdapter.saveRequest(entity);
            return;
        }
        LeaveBalanceEntity leaveBalance = leaveBalanceAdapter.findForPeriod(entity.getPolicy().getPolicyId(), entity.getUser().getUserId(), entity.getStartDate(), entity.getEndDate());
        if ((entity.getPolicy().getEntitledType() == EntitledType.DAY || entity.getPolicy().getEntitledType() == EntitledType.HALF_DAY) && (model.getUnitsRequested() != null && model.getStartDate() != null && model.getEndDate() != null)) {

            if (model.getUnitsRequested() != days) {
                throw new IllegalArgumentException("Invalid paid leave request.");
            }
            if (entity.getPolicy().getEntitledType() == EntitledType.DAY) {
                if (model.getUnitsRequested() > entity.getUnitsRequested()) {
                    days = model.getUnitsRequested() - entity.getUnitsRequested();
                    validateDays(days, leaveBalance.getBalanceUnits());
                }

            }
            if (entity.getPolicy().getEntitledType() == EntitledType.HALF_DAY) {
                if (days != 1) {
                    throw new IllegalArgumentException("Invalid paid leave request.");
                }
            }
        }

        if (entity.getPolicy().getEntitledType() == EntitledType.HOURS && (model.getStartTime() != null && model.getEndTime() != null &&
                model.getUnitsRequested() != null)) {
            if (days != 1) {
                throw new IllegalArgumentException("Invalid paid leave request.");
            }
            double modelUnitsRequested = validateHours(model.getStartTime(), model.getEndTime(), model.getUnitsRequested(), leaveBalance);
            if (model.getUnitsRequested() > entity.getUnitsRequested()) {
                double hour = modelUnitsRequested - entity.getUnitsRequested();
                if (leaveBalance.getBalanceUnits() < hour) {
                    throw new IllegalArgumentException("Insufficient leave balance.");
                }
            }
            entity.setStartTime(model.getStartTime());
            entity.setEndTime(model.getEndTime());
        }

        if (entity.getPolicy().getEntitledType() == EntitledType.HOURS) {
            if (model.getUnitsRequested() > entity.getUnitsRequested()) {
                deductLeaveBalance(entity, model.getUnitsRequested() - entity.getUnitsRequested());
            } else if (model.getUnitsRequested() < entity.getUnitsRequested()) {
                addLeaveBalance(entity, entity.getUnitsRequested() - model.getUnitsRequested());
            }
        } else {
            if (model.getUnitsRequested() > entity.getUnitsRequested()) {
                deductLeaveBalance(entity, model.getUnitsRequested() - entity.getUnitsRequested());
            } else if (model.getUnitsRequested() < entity.getUnitsRequested()) {
                addLeaveBalance(entity, entity.getUnitsRequested() - model.getUnitsRequested());
            }
        }
        entity.setUnitsRequested(model.getUnitsRequested());
        TimeOffRequestEntity saved = timeOffRequestAdapter.saveRequest(entity);
    }

    private boolean handleEmployeeRules(Status current, Status next) {
        return ((current == Status.PENDING || current == Status.APPROVED) && (next == Status.CANCELLED ) ||
        (current == Status.PENDING && next == Status.PENDING));
    }

    @Override
    public void adminUpdateStatus(AdminStatusUpdate model) {
        TimeOffRequestEntity entity = timeOffRequestAdapter.getTimeoffRequest(model.getPolicyId(), model.getUserId(), model.getRequestDate());
        if (entity == null) {
            throw new IllegalArgumentException("No time-off request exists ");
        }
        if (LocalDate.now(zoneId).isAfter(entity.getStartDate()) || model.getStatus() != null && !handleAdminRules(entity.getStatus(), model.getStatus())) {
            throw new IllegalArgumentException("Update not allowed. Invalid date or status");
        }
        if (model.getStatus() != null) {
            entity.setStatus(model.getStatus());
            TimeOffRequestEntity saved = timeOffRequestAdapter.saveRequest(entity);
            if (saved.getStatus() == Status.REJECTED) {
                Integer requested = saved.getUnitsRequested();
                addLeaveBalance(saved, requested);
            }
        }
    }

    private boolean handleAdminRules(Status current, Status next) {
        return switch (current) {
            case PENDING -> next == Status.APPROVED || next == Status.REJECTED || next == Status.PENDING;
            case APPROVED -> next == Status.REJECTED ;
            default -> false;
        };
    }

    public void deductLeaveBalance(TimeOffRequestEntity entity, Integer requested) {

        LeaveBalanceEntity leaveBalance = leaveBalanceAdapter.findForPeriod(entity.getPolicy().getPolicyId(), entity.getUser().getUserId(), entity.getStartDate(), entity.getEndDate());
        if (leaveBalance != null && entity.getPolicy().getEntitledType() == EntitledType.HALF_DAY) {
            log.info("leave balance find");
            leaveBalance.setLeaveTakenUnits(leaveBalance.getLeaveTakenUnits() + requested * 0.5);
            double balanceUnits = leaveBalance.getBalanceUnits() - requested * 0.5;
            leaveBalance.setBalanceUnits(balanceUnits);
        } else if (leaveBalance != null && (entity.getPolicy().getEntitledType() == EntitledType.HOURS ||
                entity.getPolicy().getEntitledType() == EntitledType.DAY)) {
            leaveBalance.setLeaveTakenUnits(leaveBalance.getLeaveTakenUnits() + requested);
            double balanceUnits = leaveBalance.getBalanceUnits() - requested;
            leaveBalance.setBalanceUnits(balanceUnits);
        }
        leaveBalanceAdapter.saveLeaveBalance(leaveBalance);
    }

    public void addLeaveBalance(TimeOffRequestEntity entity, Integer requested) {
        LeaveBalanceEntity leaveBalance = leaveBalanceAdapter.findForPeriod(entity.getPolicy().getPolicyId(), entity.getUser().getUserId(), entity.getStartDate(), entity.getEndDate());
        if (leaveBalance != null && entity.getPolicy().getEntitledType() == EntitledType.HALF_DAY) {
            log.info("leave balance finds");
            leaveBalance.setLeaveTakenUnits(leaveBalance.getLeaveTakenUnits() - requested * 0.5);
            double balanceUnits = leaveBalance.getBalanceUnits() + requested * 0.5;
            leaveBalance.setBalanceUnits(balanceUnits);
        } else if (leaveBalance != null && (entity.getPolicy().getEntitledType() == EntitledType.HOURS ||
                entity.getPolicy().getEntitledType() == EntitledType.DAY)) {
            leaveBalance.setLeaveTakenUnits(leaveBalance.getLeaveTakenUnits() - requested);
            double balanceUnits = leaveBalance.getBalanceUnits() + requested;
            leaveBalance.setBalanceUnits(balanceUnits);
        }
        leaveBalanceAdapter.saveLeaveBalance(leaveBalance);
    }

    @Override
    public List<EnumModel> getStatus() {
        List<EnumModel> list = new ArrayList<>();
        for (Status e : Status.values()) {
            EnumModel model = new EnumModel(e.name(), e.getValue());
            list.add(model);
        }
        return list;
    }

    @Override
    public List<TimeOffRequestResponseModel> filterRequestsByRole(
            LocalDate fromDate,
            LocalDate toDate,
            int minRoleLevel) {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("fromDate and toDate are required");
        }
        List<TimeOffRequestUserModel> list =
                timeOffRequestAdapter.filterWithUserAndRole(fromDate, toDate, minRoleLevel);
        List<TimeOffRequestResponseModel> result = new ArrayList<>();
        for (TimeOffRequestUserModel item : list) {
            TimeOffRequestResponseModel rm =
                    TimeOffPolicyEntityMapper.toModel(item.getRequest());
            rm.setUserName(item.getUserName());
            result.add(rm);
        }
        return result;
    }

    @Override
    public Map<String, List<TimeOffRequestGroupModel>> filterRequests(
            TimeOffExportRequest request,
            String loggedUserId) {
        boolean creatorMode =
                request.getUserId() != null &&
                        !request.getUserId().trim().isEmpty() &&
                        request.getUserId().equals(loggedUserId);

        if (request.getFromDate() == null || request.getToDate() == null) {
            throw new IllegalArgumentException("fromDate and toDate are required");
        }
        List<TimeOffExportView> rows = fetchExportRows(request, loggedUserId);
        List<TimeOffRequestGroupModel> toList = new ArrayList<>();
        List<TimeOffRequestGroupModel> ccList = new ArrayList<>();
        for (TimeOffExportView row : rows) {
            TimeOffRequestGroupModel model = new TimeOffRequestGroupModel();
            model.setPolicyName(row.getPolicyName());
            model.setPolicyId(row.getPolicyId());
            model.setRequestDate(LocalDate.parse(row.getRequestedDate()));
            model.setStartDate(row.getLeaveStartDate());
            model.setEndDate(row.getLeaveEndDate());
            model.setStartTime(row.getLeaveStartTime());
            model.setEndTime(row.getLeaveEndTime());
            model.setStatus(row.getStatus());
            model.setReason(row.getReason());
            model.setUnitsRequested(row.getUnitsRequested());
            model.setLeaveType(row.getLeaveType());
            if (creatorMode) {
                model.setUserId(row.getViewerId());
                model.setUserName(row.getViewerName());
                model.setViewerType(null);
                if ("APPROVER".equals(row.getViewerType()))
                    toList.add(model);
                else if ("VIEWER".equals(row.getViewerType()))
                    ccList.add(model);
            } else {
                model.setUserId(row.getCreatorId());
                model.setUserName(row.getCreatorName());
                model.setViewerType(row.getViewerType());
                toList.add(model);
            }
        }
        Map<String, List<TimeOffRequestGroupModel>> map = new HashMap<>();
        map.put("TO", toList);
        map.put("CC", ccList);
        return map;
    }

    public List<TimeOffExportView> fetchExportRows(TimeOffExportRequest request, String loggedUserId) {

        boolean creatorMode =
                request.getUserId() != null &&
                        !request.getUserId().trim().isEmpty() &&
                        request.getUserId().equals(loggedUserId);

        String[] statusArr = (request.getStatus() == null || request.getStatus().isEmpty())
                ? new String[0]
                : request.getStatus().toArray(new String[0]);

        String[] policyArr = (request.getPolicyIds() == null || request.getPolicyIds().isEmpty())
                ? new String[0]
                : request.getPolicyIds().toArray(new String[0]);

        return creatorMode
                ? timeOffRequestAdapter.fetchCreatorRequests(
                request.getFromDate(), request.getToDate(), statusArr, policyArr, loggedUserId
        )
                : timeOffRequestAdapter.fetchReceiverRequests(
                request.getFromDate(), request.getToDate(), statusArr, policyArr, loggedUserId
        );
    }

    @Override
    @Transactional
    public String startExporting(TimeOffExportRequestDto request, String schema, String orgId) {
        TimeOffExportRequest model = timeOffPolicyDtoMapper.toModel(request);
        String format = request.getFormat() == null ? "xlsx" : request.getFormat().toLowerCase();
        String baseName = "TimeOffRequest_" + request.getFromDate();
        String extension = "." + format;
        String finalName = baseName + extension;
        File file = new File(downloadDir + finalName);
        int count = 1;
        while (file.exists()) {
            finalName = baseName + "(" + count + ")" + extension;
            file = new File(downloadDir + finalName);
            count++;
        }
        String exportKey = cacheKeyUtil.getExport(schema, orgId, finalName);
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(exportKey, ReportType.PENDING, Duration.ofHours(12));
        } else {
            log.warn("Redis not available. Skipping export status tracking.");
        }
        generateReportAsync(file, exportKey, model);
        return finalName;
    }

    @Async
    public void generateReportAsync(File file, String exportKey, TimeOffExportRequest request) {
        try {
            boolean redisAvailable = redisTemplate != null;
            if (redisAvailable) {
                redisTemplate.opsForValue().set(exportKey, ReportType.PROCESSING);
            } else {
                log.warn("Redis unavailable — proceeding without status tracking.");
            }
            String loggedInUser = authHelper.getUserId();
            List<TimeOffExportView> exportData = fetchExportRows(request, loggedInUser);
            if ("csv".equalsIgnoreCase(request.getFormat())) {
                generateCsv(exportData, file);
            } else {
                generateXlsx(exportData, file);
            }
            if (redisAvailable) {
                redisTemplate.opsForValue().set(exportKey, ReportType.COMPLETED);
            }
        } catch (Exception e) {
            log.error("Export failed: {}", e.getMessage(), e);
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(exportKey, ReportType.FAILED);
            }
        }
    }

    private void generateCsv(List<TimeOffExportView> data, File file) throws Exception {
        try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8); CSVWriter csv = new CSVWriter(fw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
            ClassPathResource resource = new ClassPathResource("templates/text/timeoff_request_csv_header.txt");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String headerLine = br.readLine();
                if (headerLine != null) {
                    csv.writeNext(headerLine.split(","));
                }
            }
            for (TimeOffExportView v : data) {
                csv.writeNext(new String[]{
                        v.getCreatorId(),
                        v.getCreatorName(),
                        v.getPolicyName(),
                        String.valueOf(v.getLeaveStartDate()),
                        String.valueOf(v.getLeaveEndDate()),
                        v.getLeaveType(),
                        v.getStatus(),
                        v.getViewerType()});
            }
            csv.flush();
        }
    }

    private void generateXlsx(List<TimeOffExportView> data, File file) throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(file)) {
            Sheet sheet = workbook.createSheet("TimeOff Report");
            Resource resource =
                    new ClassPathResource("templates/text/timeoff_request_excel_header.txt");
            List<String> headerLines;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                headerLines = reader.lines().toList();
            }
            String[] headers = headerLines.getFirst().split("\\|");
            CellStyle headerStyle = reportStyleUtil.createHeaderCellStyle(workbook);
            CellStyle dataStyle = reportStyleUtil.createDataCellStyle(workbook);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                reportStyleUtil.createStyledCell(headerRow, i, " " + headers[i] + " ", headerStyle);
            }
            int rowIdx = 1;
            for (TimeOffExportView v : data) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;
                reportStyleUtil.createStyledCell(row, col++, v.getCreatorId(), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, v.getCreatorName(), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, v.getPolicyName(), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, String.valueOf(v.getLeaveStartDate()), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, String.valueOf(v.getLeaveEndDate()), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, v.getLeaveType(), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, v.getStatus(), dataStyle);
                reportStyleUtil.createStyledCell(row, col, v.getViewerType(), dataStyle);
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(fos);
        }
    }

    @Override
    public String exportStatus(String exportId, String schema, String orgId) {
        String exportKey = cacheKeyUtil.getExport(schema, orgId, exportId);
        if (redisTemplate == null) {
            return "REDIS_UNAVAILABLE";
        }
        Object val = redisTemplate.opsForValue().get(exportKey);
        if (val == null) {
            return "NOT_FOUND";
        }
        return val.toString();
    }

    @Override
    public Resource downloadReport(
            String exportId,
            String schema,
            String orgId,
            String type) {
        String exportKey = cacheKeyUtil.getExport(schema, orgId, exportId);
        if (redisTemplate == null) {
            throw new IllegalStateException("Redis unavailable. Cannot verify export status.");
        }
        Object redisValue = redisTemplate.opsForValue().get(exportKey);
        if (redisValue == null) {
            throw new IllegalStateException("Export ID not found.");
        }
        String[] parts = redisValue.toString().split("\\|");
        if (!parts[0].equalsIgnoreCase(ReportType.COMPLETED.getValues())) {
            throw new IllegalStateException("Report is not ready. Current status = " + parts[0]);
        }
        String fileName = parts[1];
        File file = new File(downloadDir + fileName);
        if (!file.exists()) {
            throw new RuntimeException("Report file not found: " + fileName);
        }
        return new FileSystemResource(file);
    }

    @Override
    public List<EnumModel> getHourType() {
        List<EnumModel> list = new ArrayList<>();
        for (HourType e : HourType.values()) {
            EnumModel model = new EnumModel(e.name(), e.getValue());
            list.add(model);
        }
        return list;
    }
}
