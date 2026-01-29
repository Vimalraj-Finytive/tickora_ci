package com.uniq.tms.tms_microservice.modules.payrollManagement.services;

import com.uniq.tms.tms_microservice.modules.payrollManagement.model.*;

import java.util.List;

public interface PayRollService {
    PayRollSettingModel createOrUpdate(PayRollSettingModel model);

    PayRollModel createRecord(PayRollModel model, String orgId);

    void calculateMonthlyPayrollAmount();

    UserPayRollAmountModel updatePayrollAmount(String userId,UserPayRollAmountModel model, String month);

    List<UserPayRollAmountModel> getPayrollAmount(String id, String month);

    List<PayRollSummary> getAllPayrollIdAndName();

    PayRollPaymentSummary getPayrollPayment(String month);

    void assignPayroll(PayRollUpdate model);

    PayRollResponseModel getPayrollById(String id);

    List<PayRollListModel> getAllPayrolls(String month);

    void updatePayrollStatus(String payrollId, PayrollStatusUpdateModel model);

    List<PayRollSettingenumModel> getAllSettings();

    List<PayRollStatusEnumModel> getAllStatus();

    PayRollSettingModel getSetting();

    void updatePayroll(PayRollEditRequestModel editModel);

    void calculateDailyPayrollAmount();

}
