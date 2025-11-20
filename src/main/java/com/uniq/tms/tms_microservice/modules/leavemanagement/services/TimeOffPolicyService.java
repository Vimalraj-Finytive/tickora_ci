package com.uniq.tms.tms_microservice.modules.leavemanagement.services;

import com.uniq.tms.tms_microservice.modules.leavemanagement.model.AccrualTypeEnumModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.CompensationEnumModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeoffPoliciesModel;
import java.util.List;

public interface TimeOffPolicyService {

     List<TimeoffPoliciesModel> getAllPolicies();
     List<TimeoffPoliciesModel> getAllPolicy();
     List<AccrualTypeEnumModel>getAccrualTypeStatus();
     List<CompensationEnumModel>getCompensation();
     TimeoffPoliciesModel getPolicyById(String id);
     List<TimeoffPoliciesModel> getPolicyByUserId(String userId);
}
