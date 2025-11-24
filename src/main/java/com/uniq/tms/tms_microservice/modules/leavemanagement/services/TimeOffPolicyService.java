package com.uniq.tms.tms_microservice.modules.leavemanagement.services;

import com.uniq.tms.tms_microservice.modules.leavemanagement.model.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.AccrualTypeEnumModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.CompensationEnumModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffPoliciesModel;
import java.util.List;

public interface TimeOffPolicyService {

      TimeOffPolicyResponseModel createPolicy(TimeOffPolicyRequestModel model);
      EntitledTypeDropdownModel getDropDowns();
      void editPolicy(TimeOffPolicyEditRequestModel model);
      void assignPolicies(TimeOffPolicyBulkAssignModel request);
      void inactivatePolicy(String policyId, TimeOffPolicyInactivateModel model);
      List<TimeOffPoliciesModel> getAllPolicies();
      List<TimeOffPoliciesModel> getAllPolicy();
      List<AccrualTypeEnumModel>getAccrualTypeStatus();
      List<CompensationEnumModel>getCompensation();
      TimeOffPoliciesModel getPolicyById(String id);
      List<TimeOffPoliciesModel> getPolicyByUserId(String userId);


}
