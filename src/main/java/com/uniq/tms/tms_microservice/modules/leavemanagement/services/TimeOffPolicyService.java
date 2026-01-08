package com.uniq.tms.tms_microservice.modules.leavemanagement.services;

import com.uniq.tms.tms_microservice.modules.leavemanagement.model.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffPoliciesModel;
import com.uniq.tms.tms_microservice.shared.dto.EnumModel;
import java.util.List;

public interface TimeOffPolicyService {

      TimeOffPolicyResponseModel createPolicy(TimeOffPolicyRequestModel model);
      List<EnumModel> getDropDowns();
      void editPolicy(TimeOffPolicyEditRequestModel model);
      void assignPolicies(TimeOffPolicyBulkAssignModel request,String orgId);
      void inactivatePolicy(String policyId, TimeOffPolicyInactivateModel model);
      List<TimeOffPoliciesModel> getAllPolicies();
      List<TimeOffPoliciesModel> getAllPolicy(String type);
      List<EnumModel>getAccrualTypeStatus();
      List<EnumModel>getCompensation();
      TimeOffPoliciesModel getPolicyById(String id);
      List<TimeOffPoliciesModel> getPolicyByUserId(String userId);
      List<EnumModel> getResetFrequencyStatus();
      void editUserPolicy(List<EditUserPolicyModel> editUserPolicyModel);
      void updateMonthlyPolicy(String orgId);
      void updateYearlyPolicy(String orgId);

}
