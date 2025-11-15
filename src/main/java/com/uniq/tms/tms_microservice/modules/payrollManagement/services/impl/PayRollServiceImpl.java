package com.uniq.tms.tms_microservice.modules.payrollManagement.services.impl;

import com.uniq.tms.tms_microservice.modules.identityManagement.service.IdGenerationService;
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
import com.uniq.tms.tms_microservice.modules.payrollManagement.services.PayRollService;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import com.uniq.tms.tms_microservice.shared.helper.TimesheetHelper;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PayRollServiceImpl implements PayRollService {

    private static final Logger log = LogManager.getLogger(PayRollServiceImpl.class);

    private  final PayRollAdapter payRollAdapter;
    private final PayRollEntityMapper entityMapper;
    private final UserAdapter userAdapter;
    private final IdGenerationService idGenerationService;
    private final TimesheetAdapter timesheetAdapter;
    private final TimesheetHelper timesheetHelper;
    private final PayRollEntityMapper payRollEntityMapper;

    public PayRollServiceImpl(PayRollAdapter payRollAdapter, PayRollEntityMapper entityMapper, UserAdapter userAdapter,
                              IdGenerationService idGenerationService, TimesheetAdapter timesheetAdapter, TimesheetHelper timesheetHelper, PayRollEntityMapper payRollEntityMapper) {
        this.payRollAdapter = payRollAdapter;
        this.entityMapper = entityMapper;
        this.userAdapter = userAdapter;
        this.idGenerationService = idGenerationService;
        this.timesheetAdapter = timesheetAdapter;
        this.timesheetHelper = timesheetHelper;
        this.payRollEntityMapper = payRollEntityMapper;
    }

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
        String[] userIds = entities.stream()
                .map(e -> e.getUser().getUserId())
                .toArray(String[]::new);
        TimesheetHelper.WorkScheduleResult result = timesheetHelper.fetchWorkSchedulesAndDays(userIds);
        Map<String, Set< DayOfWeek >> userWorkingDaysMap = result.getUserWorkingDaysMap();
        PayRollSettingEntity settingEntity = payRollAdapter.findFirst()
                .orElseThrow(() -> new RuntimeException("Payroll Setting not found"));
        List<UserPayRollAmountEntity> userPayrollAmountList = new ArrayList<>();
        for(UserPayRollEntity entity : entities){
            log.info("loop started");
            UserPayRollAmountEntity userPayrollAmount = new UserPayRollAmountEntity();
            userPayrollAmount.setUser(entity.getUser());
            userPayrollAmount.setPayroll(entity.getPayroll());
            LocalDate date = LocalDate.now(ZoneId.of("Asia/Kolkata"));
            int year = date.getYear();
            int month = date.getMonthValue() - 1;
            if(month == 0){
                month = 12;
                year = year - 1;
            }
            LocalDate localDate = LocalDate.of(year, month, 1);
            String monthDate = localDate.format(DateTimeFormatter.ofPattern("MMMM,yyyy"));
            userPayrollAmount.setMonth(monthDate);
            int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
            BigDecimal daySalary = entity.getPayroll().getMonthlySalary().divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
            List<TimesheetEntity> timesheetEntities = timesheetAdapter.getTimesheetByUserIds(entity.getUser().getUserId(), year, month);
            List<Integer> regularDaysList = calculateRegularDays(timesheetEntities, entity.getUser().getUserId(), userWorkingDaysMap, daysInMonth, year, month);
            BigDecimal regularHrs = calculateRegularHrs(timesheetEntities);
            userPayrollAmount.setUnpaidLeaveDeduction(BigDecimal.valueOf(daysInMonth).subtract(BigDecimal.valueOf(regularDaysList.get(1))).multiply(daySalary));
            userPayrollAmount.setRegularDays(regularDaysList.get(0));
            userPayrollAmount.setRegularHrs(regularHrs);
            BigDecimal regularPayrollAmount = calculateRegularPayrollAmount(regularDaysList.get(1), daySalary);
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
            userPayrollAmount.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            userPayrollAmount.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            userPayrollAmount.setNotes("monthly salary updated");
            userPayrollAmountList.add(userPayrollAmount);
        }
          payRollAdapter.saveAllUserPayrollAmount(userPayrollAmountList);
    }


//    public BigDecimal calculateRegularHrsDays(List<TimesheetEntity> timesheetEntities, PayrollSettingEnum payrollSettingEnum,
//                                              BigDecimal unpaidLeave, int days){
//            return BigDecimal.valueOf(days).subtract(unpaidLeave);
//
//    }

    public List<Integer> calculateRegularDays(List<TimesheetEntity> timesheetEntities, String userId,
                                              Map<String, Set< DayOfWeek >> userWorkingDaysMap, int daysInMonth, int year, int month) {
        int count = 0, restDaysCount =0;
        Set<LocalDate> timesheetDates = timesheetEntities.stream()
                .map(TimesheetEntity::getDate)
                .collect(Collectors.toSet());

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate currentDate = LocalDate.of(year, month, day);
            if (timesheetDates.contains(currentDate) ) {
                count++;
            }
            if(!userWorkingDaysMap.get(userId).contains(currentDate.getDayOfWeek())) {
                restDaysCount++;
            }
        }
        return List.of(count,restDaysCount+count);
    }

    public BigDecimal calculateOvertimeHrs(List<TimesheetEntity> timesheetEntities){
        return timesheetEntities.stream()
                .filter(t -> t.getTotalOverTime() != null)
                .map(t -> BigDecimal.valueOf(t.getTotalOverTime().getHour())
                        .add(BigDecimal.valueOf(t.getTotalOverTime().getMinute()).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateRegularHrs(List<TimesheetEntity> timesheetEntities){
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
    public UserPayRollAmountModel updatePayrollAmount(UserPayRollAmountModel model) {

        Optional<UserPayRollAmountEntity> optExisting =
                payRollAdapter.findUserPayrollAmountByUserId(model.getUserId());
        UserPayRollAmountEntity existing = getUserPayrollAmountEntity(model, optExisting);

        BigDecimal incomingTotalAmount = model.getTotalAmount();
        BigDecimal existingTotalAmount = existing.getTotalAmount();


        if (existingTotalAmount == null && incomingTotalAmount != null) {
            existing.setTotalAmount(incomingTotalAmount);
        }
        else if (incomingTotalAmount != null
                && existingTotalAmount != null
                && incomingTotalAmount.compareTo(existingTotalAmount) != 0) {
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
    public void updatePayrollStatus(PayrollStatusUpdateModel model) {

        PayRollEntity payroll = payRollAdapter.findById(model.getPayrollId())
                .orElseThrow(() -> new RuntimeException("Payroll not found"));
        payroll.setActive(model.isActive());

        payroll.setUpdatedAt(LocalDateTime.now());

        payRollAdapter.save(payroll);

        if (!model.isActive()) {
            payRollAdapter.deleteUserPayrollById(model.getPayrollId());
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
        for (UserPayRollAmountEntity entity : userPayRollAmountEntityList){
            totalPayment =  totalPayment.add(entity.getTotalPayrollAmount());
            if (entity.getPayrollStatus() == PayRollStatusEnum.PAID){
                paidCount++;
            }
            else {
                if(entity.getPayrollStatus() == PayRollStatusEnum.FAILED){
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

    public void updatePayroll(PayRollUpdate model) {
        List<UserEntity> users = userAdapter.getUsersByIds(model.getUserId());
        PayRollEntity payroll = payRollAdapter.getPayRoll(model.getPayRollId());

        List<UserPayRollEntity> existingMappings =payRollAdapter.
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
}
