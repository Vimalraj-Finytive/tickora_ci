package com.uniq.tms.tms_microservice.modules.payrollManagement.services;

import com.uniq.tms.tms_microservice.modules.leavemanagement.model.ExportStatus;
import com.uniq.tms.tms_microservice.modules.payrollManagement.model.*;

import java.io.File;
import java.util.List;
import com.uniq.tms.tms_microservice.modules.payrollManagement.model.PayRollModel;
import com.uniq.tms.tms_microservice.modules.payrollManagement.model.PayRollSettingModel;
import com.uniq.tms.tms_microservice.modules.payrollManagement.model.UserPayRollAmountModel;

public interface PayRollService {
    PayRollSettingModel createOrUpdate(PayRollSettingModel model);

    PayRollModel createRecord(PayRollModel model, String orgId);

    void calculatePayrollAmount();

    UserPayRollAmountModel updatePayrollAmount(String userId,UserPayRollAmountModel model, String month);

    List<UserPayRollAmountModel> getPayrollAmount(String id, String month);

    List<PayRollSummary> getAllPayrollIdAndName();

    PayRollPaymentSummary getPayrollPayment(String month);

    void assignPayroll(PayRollUpdate model);

    PayRollResponseModel getPayrollById(String id);

    List<PayRollListModel> getAllPayrolls();

    void updatePayrollStatus(String payrollId, PayrollStatusUpdateModel model);

    List<PayRollSettingenumModel> getAllSettings();

    List<PayRollStatusEnumModel> getAllStatus();

    PayRollSettingModel getSetting();

    void updatePayroll(PayRollEditRequestModel editModel);

    String startExportPayroll(String month, String format, String schema, String orgId);

    String getExportStatus(String exportId, String schema, String orgId);

    File downloadPayroll(String exportId, String schema, String orgId);
}
