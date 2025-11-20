package com.uniq.tms.tms_microservice.modules.leavemanagement.services;

import com.uniq.tms.tms_microservice.modules.leavemanagement.model.AdminStatusUpdate;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.EmployeeStatusUpdate;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffRequest;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.EntitledTypeDropdownDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffPolicyRequestDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeoffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.*;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.AccrualTypeEnumModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.CompensationEnumModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeoffPoliciesModel;
import java.util.List;

public interface TimeOffPolicyService {
      void createRequest(TimeOffRequest request);
      void employeeUpdateStatus(EmployeeStatusUpdate model);
      void adminUpdateStatus(AdminStatusUpdate model);
      void updateLeaveBalance();
      TimeOffPolicyResponseModel createPolicy(TimeOffPolicyRequestModel model);
      EntitledTypeDropdownModel getDropDowns();
      void editPolicy(TimeOffPolicyEditRequestModel model);
      void assignPolicies(TimeOffPolicyBulkAssignModel request);
      void inactivatePolicy(String policyId, TimeOffPolicyInactivateModel model);
      List<TimeoffPoliciesModel> getAllPolicies();
      List<TimeoffPoliciesModel> getAllPolicy();
      List<AccrualTypeEnumModel>getAccrualTypeStatus();
      List<CompensationEnumModel>getCompensation();
      TimeoffPoliciesModel getPolicyById(String id);
      List<TimeoffPoliciesModel> getPolicyByUserId(String userId);
}
