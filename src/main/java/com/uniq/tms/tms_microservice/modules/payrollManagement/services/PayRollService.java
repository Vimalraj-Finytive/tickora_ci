package com.uniq.tms.tms_microservice.modules.payrollManagement.services;

import com.uniq.tms.tms_microservice.modules.payrollManagement.model.*;
import java.util.List;
import java.util.Map;

import com.uniq.tms.tms_microservice.modules.payrollManagement.model.PayRollModel;
import com.uniq.tms.tms_microservice.modules.payrollManagement.model.PayRollSettingModel;
import com.uniq.tms.tms_microservice.modules.payrollManagement.model.UserPayRollAmountModel;

public interface PayRollService {

    PayRollSettingModel createOrUpdate(PayRollSettingModel model);
    PayRollModel createRecord(PayRollModel model, String orgId);
    void calculatePayrollAmount();
    UserPayRollAmountModel updatePayrollAmount(UserPayRollAmountModel model);
    List<UserPayRollAmountModel> getPayrollAmount(String id, String month);
    List<PayRollSummary> getAllPayrollIdAndName();
    PayRollPaymentSummary getPayrollPayment(String month);
    void updatePayroll(PayRollUpdate model);
    PayRollResponseModel getPayrollById(String id);
    List<PayRollListModel> getAllPayrolls();
    void updatePayrollStatus(PayrollStatusUpdateModel model);
    List<PayRollSettingenumModel> getAllSettings();
    List<PayRollStatusEnumModel> getAllStatus();
    PayRollSettingModel getSetting();
}
