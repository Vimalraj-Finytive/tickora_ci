package com.uniq.tms.tms_microservice.modules.payrollManagement.services.impl;

import com.opencsv.CSVWriter;
import com.uniq.tms.tms_microservice.modules.identityManagement.service.IdGenerationService;
import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.LeaveBalanceAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffExportRequestDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.MonthlySummaryEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.ReportType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.ExportStatus;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffExportRequest;
import com.uniq.tms.tms_microservice.modules.payrollManagement.adapter.PayRollAdapter;
import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.PayRollEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.PayRollSettingEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.UserPayRollAmountEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.UserPayRollEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.enums.PayRollSettingEnum;
import com.uniq.tms.tms_microservice.modules.payrollManagement.mapper.PayRollEntityMapper;
import com.uniq.tms.tms_microservice.modules.payrollManagement.model.*;
import com.uniq.tms.tms_microservice.modules.payrollManagement.enums.PayRollStatusEnum;
import com.uniq.tms.tms_microservice.modules.payrollManagement.model.PayRollModel;
import com.uniq.tms.tms_microservice.modules.payrollManagement.model.PayRollSettingModel;
import com.uniq.tms.tms_microservice.modules.payrollManagement.model.UserPayRollAmountModel;
import com.uniq.tms.tms_microservice.modules.payrollManagement.projection.UserPayRollAmount;
import com.uniq.tms.tms_microservice.modules.payrollManagement.services.PayRollService;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import com.uniq.tms.tms_microservice.shared.helper.TimesheetHelper;
import com.uniq.tms.tms_microservice.shared.util.CacheKeyUtil;
import com.uniq.tms.tms_microservice.shared.util.ReportStyleUtil;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PayRollServiceImpl implements PayRollService {

    private static final Logger log = LogManager.getLogger(PayRollServiceImpl.class);

    private final PayRollAdapter payRollAdapter;
    private final PayRollEntityMapper entityMapper;
    private final UserAdapter userAdapter;
    private final IdGenerationService idGenerationService;
    private final TimesheetAdapter timesheetAdapter;
    private final PayRollEntityMapper payRollEntityMapper;
    private final LeaveBalanceAdapter leaveBalanceAdapter;
    private final RedisTemplate<String,Object>redisTemplate;
    private final CacheKeyUtil cacheKeyUtil;
    private final ReportStyleUtil reportStyleUtil;

    public PayRollServiceImpl(PayRollAdapter payRollAdapter, PayRollEntityMapper entityMapper, UserAdapter userAdapter,
                              IdGenerationService idGenerationService, TimesheetAdapter timesheetAdapter,
                              PayRollEntityMapper payRollEntityMapper, LeaveBalanceAdapter leaveBalanceAdapter,
                              RedisTemplate<String, Object> redisTemplate, CacheKeyUtil cacheKeyUtil, ReportStyleUtil reportStyleUtil) {
        this.payRollAdapter = payRollAdapter;
        this.entityMapper = entityMapper;
        this.userAdapter = userAdapter;
        this.idGenerationService = idGenerationService;
        this.timesheetAdapter = timesheetAdapter;
        this.payRollEntityMapper = payRollEntityMapper;
        this.leaveBalanceAdapter = leaveBalanceAdapter;
        this.redisTemplate = redisTemplate;
        this.cacheKeyUtil = cacheKeyUtil;
        this.reportStyleUtil = reportStyleUtil;
    }

    @Value("${csv.payroll.download.dir}")
    private String downloadDir;

    @Override
    public PayRollSettingModel createOrUpdate(PayRollSettingModel model) {
        PayRollSettingEntity entity = payRollAdapter.findFirst()
                .orElse(null);
        if (entity != null) {
            entity.setPayrollCalculation(model.getPayrollCalculation());
            entity.setOvertime(model.isOvertime());
        } else {
            entity = entityMapper.toEntity(model);
        }
        PayRollSettingEntity saved = payRollAdapter.save(entity);
        return entityMapper.toModel(saved);
    }

    @Override
    public PayRollModel createRecord(PayRollModel model, String orgId) {

        PayRollEntity payRollEntity = entityMapper.toEntity(model);
        String payrollId = idGenerationService.generatePayrollId(orgId);
        payRollEntity.setId(payrollId);
        payRollEntity.setActive(true);
        payRollEntity.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        payRollAdapter.savePayRoll(payRollEntity);
        List<UserPayRollEntity> existingMappings = payRollAdapter.findExistingUserPayrolls(model.getUserIds());

        if (!existingMappings.isEmpty()) {
            payRollAdapter.deleteAll(existingMappings);
        }
        List<UserPayRollEntity> userPayRollList = new ArrayList<>();
        for (String userId : model.getUserIds()) {
            UserEntity user = userAdapter.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found for ID: " + userId));

            UserPayRollEntity mapping = new UserPayRollEntity();
            mapping.setUser(user);
            mapping.setPayroll(payRollEntity);

            userPayRollList.add(mapping);
        }
        payRollAdapter.saveAllUserPayroll(userPayRollList);
        return entityMapper.toModel(payRollEntity);
    }

    @Override
    public void calculatePayrollAmount() {
        log.info("before finding user payroll");
        List<UserPayRollEntity> entities = payRollAdapter.getAllUserPayroll();

        // NOTE: Keeping this for future requirement changes
//        String[] userIds = entities.stream()
//                .map(e -> e.getUser().getUserId())
//                .toArray(String[]::new);
//        TimesheetHelper.WorkScheduleResult result = timesheetHelper.fetchWorkSchedulesAndDays(userIds);
//        Map<String, Set< DayOfWeek >> userWorkingDaysMap = result.getUserWorkingDaysMap();
        PayRollSettingEntity settingEntity = payRollAdapter.findFirst()
                .orElseThrow(() -> new RuntimeException("Payroll Setting not found"));
        LocalDate date = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        int year = date.getYear();
        int month = date.getMonthValue() - 1;
        if(month == 0){
            month = 12;
            year = year - 1;
        }
        List<MonthlySummaryEntity> monthlySummaryList = leaveBalanceAdapter.findByMonthAndYear(month, year);
        Map<String, Integer> unpaidMap =
                monthlySummaryList.stream()
                        .collect(Collectors.toMap(
                                MonthlySummaryEntity::getUserId,
                                MonthlySummaryEntity::getUnpaidLeavesTaken
                        ));
        List<UserPayRollAmountEntity> userPayrollAmountList = new ArrayList<>();
        for(UserPayRollEntity entity : entities){
            log.info("loop started");
            UserPayRollAmountEntity userPayrollAmount = new UserPayRollAmountEntity();
            userPayrollAmount.setUser(entity.getUser());
            userPayrollAmount.setPayroll(entity.getPayroll());
            LocalDate localDate = LocalDate.of(year, month, 1);
            String monthDate = localDate.format(DateTimeFormatter.ofPattern("MMMM,yyyy"));
            userPayrollAmount.setMonth(monthDate);
            int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
            BigDecimal daySalary = entity.getPayroll().getMonthlySalary().divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
            List<TimesheetEntity> timesheetEntities = timesheetAdapter.getTimesheetByUserIds(entity.getUser().getUserId(), year, month);
//            List<Integer> regularDaysList = calculateRegularDays(timesheetEntities, entity.getUser().getUserId(), userWorkingDaysMap, daysInMonth, year, month);
            BigDecimal unpaidLeave = BigDecimal.valueOf(unpaidMap.get(entity.getUser().getUserId()));
            Integer regularDays = calculateRegularDays(daysInMonth, unpaidLeave);
            BigDecimal regularHrs = calculateRegularHrs(timesheetEntities);
            userPayrollAmount.setUnpaidLeaveDeduction(unpaidLeave);
            userPayrollAmount.setRegularDays(regularDays);
            userPayrollAmount.setRegularHrs(regularHrs);
            BigDecimal regularPayrollAmount = calculateRegularPayrollAmount(regularDays, daySalary);
            userPayrollAmount.setRegularPayrollAmount(regularPayrollAmount);
            BigDecimal overtimeHrs = BigDecimal.ZERO;
            BigDecimal overtimePayrollAmount = BigDecimal.ZERO;
            if (settingEntity.isOvertime()) {
                overtimeHrs = calculateOvertimeHrs(timesheetEntities);
                overtimePayrollAmount = overtimeHrs
                        .multiply(entity.getPayroll().getOvertimeAmount())
                        .setScale(2, RoundingMode.HALF_UP);
            }
            userPayrollAmount.setOvertimeHrs(overtimeHrs);
            userPayrollAmount.setTotalHrs(regularHrs.add(overtimeHrs));
            userPayrollAmount.setOvertimePayrollAmount(overtimePayrollAmount);
            userPayrollAmount.setTotalPayrollAmount(regularPayrollAmount.add(overtimePayrollAmount));
            userPayrollAmount.setPayrollStatus(PayRollStatusEnum.PROCESSING);
            userPayrollAmount.setNotes("monthly salary updated");
            userPayrollAmountList.add(userPayrollAmount);
        }
        payRollAdapter.saveAllUserPayrollAmount(userPayrollAmountList);
    }


    public Integer calculateRegularDays(int days, BigDecimal unpaidLeave){
            return BigDecimal.valueOf(days).subtract(unpaidLeave).intValue();
    }

    // NOTE: Keeping this for future requirement changes

//    public List<Integer> calculateRegularDays(List<TimesheetEntity> timesheetEntities, String userId,
//                                              Map<String, Set< DayOfWeek >> userWorkingDaysMap, int daysInMonth, int year, int month) {
//        int count = 0, restDaysCount =0;
//        Set<LocalDate> timesheetDates = timesheetEntities.stream()
//                .map(TimesheetEntity::getDate)
//                .collect(Collectors.toSet());
//
//        for (int day = 1; day <= daysInMonth; day++) {
//            LocalDate currentDate = LocalDate.of(year, month, day);
//            if (timesheetDates.contains(currentDate) ) {
//                count++;
//            }
//            if(!userWorkingDaysMap.get(userId).contains(currentDate.getDayOfWeek())) {
//                restDaysCount++;
//            }
//        }
//        return List.of(count,restDaysCount+count);
//    }

    public BigDecimal calculateOvertimeHrs(List<TimesheetEntity> timesheetEntities) {
        return timesheetEntities.stream()
                .filter(t -> t.getTotalOverTime() != null)
                .map(t -> BigDecimal.valueOf(t.getTotalOverTime().getHour())
                        .add(BigDecimal.valueOf(t.getTotalOverTime().getMinute()).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateRegularHrs(List<TimesheetEntity> timesheetEntities) {
        return timesheetEntities.stream()
                .filter(t -> t.getRegularHours() != null)
                .map(t -> BigDecimal.valueOf(t.getRegularHours().getHour())
                        .add(BigDecimal.valueOf(t.getRegularHours().getMinute()).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateRegularPayrollAmount(Integer regularDays, BigDecimal daySalary) {
        if (regularDays == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(regularDays).multiply(daySalary);
    }

    @Override
    public List<UserPayRollAmountModel> getPayrollAmount(String id, String month) {
        return entityMapper.toModel(payRollAdapter.getPayrollAmount(id, month));
    }

    @Override
    @Transactional
    public UserPayRollAmountModel updatePayrollAmount(String userId, UserPayRollAmountModel model, String month) {

        Optional<UserPayRollAmountEntity> optExisting =
                payRollAdapter.findUserPayrollAmountByUserIdAndMonth(userId, month);
        UserPayRollAmountEntity existing = getUserPayrollAmountEntity(model, optExisting);

        BigDecimal incomingTotalAmount = model.getTotalAmount();
        BigDecimal existingTotalAmount = existing.getTotalAmount();

        if (existingTotalAmount == null && incomingTotalAmount != null) {
            existing.setTotalAmount(incomingTotalAmount);
        } else if (incomingTotalAmount != null && incomingTotalAmount.compareTo(existingTotalAmount) != 0) {
            existing.setTotalAmount(incomingTotalAmount);
        }

        if (model.getPayrollStatus() != null) {
            existing.setPayrollStatus(model.getPayrollStatus());
        }

        if (model.getNotes() != null) {
            existing.setNotes(model.getNotes());
        }

        if (model.getTotalPayrollAmount() != null) {
            existing.setTotalPayrollAmount(model.getTotalPayrollAmount());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        UserPayRollAmountEntity saved = payRollAdapter.saveUserPayRollAmount(existing);
        return entityMapper.toModel(saved);
    }

    @Override
    public PayRollResponseModel getPayrollById(String id) {
        PayRollEntity entity = payRollAdapter.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payroll not found for id: " + id));
        return payRollEntityMapper.toResponseModel(entity);
    }

    @Override
    public List<PayRollListModel> getAllPayrolls() {
        List<PayRollEntity> entities = payRollAdapter.findAll();
        return entities.stream()
                .map(payRollEntityMapper::toListModel)
                .toList();
    }

    @Override
    @Transactional
    public void updatePayrollStatus(String payrollId, PayrollStatusUpdateModel model) {

        PayRollEntity payroll = payRollAdapter.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Payroll not found"));

        payroll.setActive(model.isActive());
        payroll.setUpdatedAt(LocalDateTime.now());
        payRollAdapter.save(payroll);
        if (!model.isActive()) {
            try {
                payRollAdapter.deleteUserPayrollById(payrollId);
            } catch (Exception ex) {
                log.error("Failed to delete user-payroll mappings for payrollId {}: {}", payrollId, ex.getMessage(), ex);
            }
        }
    }

    private static UserPayRollAmountEntity getUserPayrollAmountEntity(UserPayRollAmountModel model, Optional<UserPayRollAmountEntity> optExisting) {
        UserPayRollAmountEntity existing = optExisting.orElseThrow(() ->
                new RuntimeException("User payroll amount record not found for userId: " + model.getUserId()));

        if (existing.getPayrollStatus() == PayRollStatusEnum.PAID) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot edit payroll. Status is PAID.");
        }

        if (existing.getPayrollStatus() == PayRollStatusEnum.APPROVED &&
                model.getTotalAmount() != null &&
                model.getTotalAmount().compareTo(existing.getTotalAmount()) != 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot modify total amount. Status is APPROVED.");
        }
        return existing;
    }


    @Override
    public List<PayRollSummary> getAllPayrollIdAndName() {
        return payRollAdapter.getAllPayrollNameAndId()
                .stream()
                .map(p -> new PayRollSummary(p.getId(), p.getPayrollName()))
                .collect(Collectors.toList());
    }

    @Override
    public PayRollPaymentSummary getPayrollPayment(String month) {
        List<UserPayRollAmountEntity> userPayRollAmountEntityList = payRollAdapter.getAllByMonthAndYear(month);
        BigDecimal totalPayment = BigDecimal.ZERO;
        int paidCount = 0, unpaidCount = 0, failedCount = 0;
        for (UserPayRollAmountEntity entity : userPayRollAmountEntityList) {
            totalPayment = totalPayment.add(entity.getTotalPayrollAmount());
            if (entity.getPayrollStatus() == PayRollStatusEnum.PAID) {
                paidCount++;
            } else {
                if (entity.getPayrollStatus() == PayRollStatusEnum.FAILED) {
                    failedCount++;
                }
                unpaidCount++;
            }
        }
        return new PayRollPaymentSummary(totalPayment, paidCount, unpaidCount, failedCount);
    }

    @Override
    public List<PayRollSettingenumModel> getAllSettings() {
        List<PayRollSettingenumModel> list = new ArrayList<>();
        for (PayRollSettingEnum e : PayRollSettingEnum.values()) {
            PayRollSettingenumModel model =
                    new PayRollSettingenumModel(e.name(), e.getValue());
            list.add(model);
        }
        return list;
    }

    @Override
    public List<PayRollStatusEnumModel> getAllStatus() {
        List<PayRollStatusEnumModel> list = new ArrayList<>();
        for (PayRollStatusEnum e : PayRollStatusEnum.values()) {
            PayRollStatusEnumModel model = new PayRollStatusEnumModel(e.name(), e.getValue());
            list.add(model);
        }
        return list;
    }

    @Override
    public PayRollSettingModel getSetting() {
        PayRollSettingEntity entity = payRollAdapter.findFirst().orElse(null);
        if (entity == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "No Data Found");
        return entityMapper.toModel(entity);
    }

    public void assignPayroll(PayRollUpdate model) {
        List<UserEntity> users = userAdapter.getUsersByIds(model.getUserId());
        PayRollEntity payroll = payRollAdapter.getPayRoll(model.getPayRollId());
        List<UserPayRollEntity> existingMappings = payRollAdapter.
                findExistingUserPayrolls(model.getUserId());
        Map<String, UserPayRollEntity> existingMap = existingMappings.stream()
                .collect(Collectors.toMap(e -> e.getUser().getUserId(), e -> e));
        List<UserPayRollEntity> toUpdate = new ArrayList<>();
        List<UserPayRollEntity> toInsert = new ArrayList<>();
        for (UserEntity user : users) {
            UserPayRollEntity mapping = existingMap.get(user.getUserId());
            if (mapping != null) {
                mapping.setPayroll(payroll);
                toUpdate.add(mapping);
            } else {
                UserPayRollEntity newEntity = new UserPayRollEntity();
                newEntity.setUser(user);
                newEntity.setPayroll(payroll);
                toInsert.add(newEntity);
            }
        }
        if (!toUpdate.isEmpty()) {
            payRollAdapter.saveAllUserPayroll(toUpdate);
        }
        if (!toInsert.isEmpty()) {
            payRollAdapter.saveAllUserPayroll(toInsert);
        }
    }

    @Override
    public void updatePayroll(PayRollEditRequestModel editModel) {
        PayRollEntity payroll = payRollAdapter.findById(editModel.getPayrollId())
                .orElseThrow(() -> new IllegalArgumentException("Payroll not found"));
        if (editModel.getPayrollName() != null)
            payroll.setPayrollName(editModel.getPayrollName());
        if (editModel.getYearlySalary() != null)
            payroll.setYearlySalary(editModel.getYearlySalary());
        if (editModel.getMonthlySalary() != null)
            payroll.setMonthlySalary(editModel.getMonthlySalary());
        if (editModel.getPf() != null)
            payroll.setPf(editModel.getPf());
        if (editModel.getOthers() != null)
            payroll.setOthers(editModel.getOthers());
        if (editModel.getOvertimeAmount() != null)
            payroll.setOvertimeAmount(editModel.getOvertimeAmount());
        payroll.setUpdatedAt(LocalDateTime.now());
        payRollAdapter.savePayRoll(payroll);
    }

    @Override
    @Transactional
    public String startExportPayroll(String month, String format, String schema, String orgId) {
        File folder = new File(downloadDir);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String fileFormat = (format == null ? "xlsx" : format.toLowerCase());
        String baseName = "Payroll_" + month;
        String extension = "." + fileFormat;
        String finalName = baseName + extension;
        File file = new File(downloadDir + finalName);
        int count = 1;
        while (file.exists()) {
            finalName = baseName + "(" + count + ")" + extension;
            file = new File(downloadDir + finalName);
            count++;
        }
        String exportKey = cacheKeyUtil.getPayRollExport(schema, orgId, finalName);
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(exportKey, ReportType.PENDING.getValues(), Duration.ofHours(12));
        }
        generateAsync(file, exportKey, month, fileFormat);
        return finalName;
    }

    @Async
    public void generateAsync(File file, String redisKey, String month, String format) {
        try {
            redisTemplate.opsForValue().set(redisKey, ReportType.PROCESSING.getValues());
            List<UserPayRollAmount> data = payRollAdapter.findAllByMonth(month);
            if ("csv".equalsIgnoreCase(format)) {
                generateCsv(data, file);
            } else {
                generateXlsx(data, file);
            }
            redisTemplate.opsForValue().set(redisKey, ReportType.COMPLETED.getValues());
        } catch (Exception ex) {
            redisTemplate.opsForValue().set(redisKey, ReportType.FAILED.getValues());
            log.error("Payroll export failed", ex);
        }
    }

    private void generateCsv(List<UserPayRollAmount> data, File file) {
        try (FileWriter fw = new FileWriter(file);
             CSVWriter csv = new CSVWriter(fw)) {
            ClassPathResource resource =
                    new ClassPathResource("templates/text/payroll_amount_csv_header.txt");
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

                String headerLine = br.readLine();
                if (headerLine != null) {
                    csv.writeNext(headerLine.split(","));
                }
            }
            for (UserPayRollAmount p : data) {
                csv.writeNext(new String[]{
                        p.getUserId(),
                        p.getUserName(),
                        String.valueOf(p.getUnpaidLeaveDeduction()),
                        String.valueOf(p.getRegularDays()),
                        String.valueOf(p.getRegularHrs()),
                        String.valueOf(p.getOvertimeHrs()),
                        String.valueOf(p.getTotalHrs()),
                        String.valueOf(p.getRegularPayrollAmount()),
                        String.valueOf(p.getOvertimePayrollAmount()),
                        String.valueOf(p.getTotalPayrollAmount()),
                        String.valueOf(p.getMonthlyNetSalary()),
                        p.getPayrollName(),
                        String.valueOf(p.getPayrollStatus()),
                        p.getNotes(),
                        String.valueOf(p.getTotalAmount()),
                        p.getMonth()
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("CSV export failed", e);
        }
    }

    private void generateXlsx(List<UserPayRollAmount> data, File file) {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(file)) {
            Sheet sheet = workbook.createSheet("Payroll Report");
            CellStyle headerStyle = reportStyleUtil.createHeaderCellStyle(workbook);
            CellStyle dataStyle = reportStyleUtil.createDataCellStyle(workbook);
            ClassPathResource resource =
                    new ClassPathResource("templates/text/payroll_amount_excel_header.txt");
            List<String> headerLines;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                headerLines = reader.lines().toList();
            }
            String[] headers = headerLines.getFirst().split("\\|");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                reportStyleUtil.createStyledCell(headerRow, i, headers[i], headerStyle);
            }
            int rowIdx = 1;
            for (UserPayRollAmount p : data) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;
                reportStyleUtil.createStyledCell(row, col++, p.getUserId(), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, p.getUserName(), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, String.valueOf(p.getUnpaidLeaveDeduction()), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, p.getRegularDays(), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, p.getRegularHrs(), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, p.getOvertimeHrs(), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, p.getTotalHrs(), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, String.valueOf(p.getRegularPayrollAmount()), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, String.valueOf(p.getOvertimePayrollAmount()), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, String.valueOf(p.getTotalPayrollAmount()), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, String.valueOf(p.getMonthlyNetSalary()), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, p.getPayrollName(), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, p.getPayrollStatus(), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, p.getNotes(), dataStyle);
                reportStyleUtil.createStyledCell(row, col++, String.valueOf(p.getTotalAmount()), dataStyle);
                reportStyleUtil.createStyledCell(row, col, p.getMonth(), dataStyle);
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(fos);
        } catch (Exception e) {
            throw new RuntimeException("XLSX export failed", e);
        }
    }

    @Override
    public String getExportStatus(String exportId, String schema, String orgId) {
        String exportKey = cacheKeyUtil.getPayRollExport(schema, orgId, exportId);
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
    public File downloadPayroll(String exportId, String schema, String orgId) {
        String redisKey = cacheKeyUtil.getPayRollExport(schema, orgId, exportId);
        String status = (String) redisTemplate.opsForValue().get(redisKey);
        if (status == null || !ReportType.COMPLETED.getValues().equalsIgnoreCase(status)) {
            throw new RuntimeException("Payroll export not ready. Current status = " + status);
        }
        File file = new File(downloadDir + exportId);
        if (!file.exists()) {
            throw new RuntimeException("Report file missing");
        }
        return file;
    }

}
