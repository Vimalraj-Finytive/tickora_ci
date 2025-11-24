package com.uniq.tms.tms_microservice.modules.leavemanagement.mapper;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffPolicyRequestDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeoffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeoffRequestEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffPolicyRequestModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffPolicyResponseModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeoffPoliciesModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeoffRequestResponseModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TimeOffPolicyEntityMapper {

    TimeoffPolicyEntity toEntity(TimeOffPolicyRequestDto dto);

    TimeoffPolicyEntity toEntity(TimeOffPolicyRequestModel model);

    TimeOffPolicyResponseModel toResponseModel(TimeoffPolicyEntity entity);

    TimeoffPoliciesModel toModel(TimeoffPolicyEntity entity);

    TimeoffPolicyEntity toEntity(TimeoffPoliciesModel model);

    List<TimeoffPoliciesModel> toModelList(List<TimeoffPolicyEntity> entity);

    List<TimeoffRequestResponseModel> toModel(List<TimeoffRequestEntity> entityList);



    @Mapping(source = "policy.policyName", target = "policyName")
    TimeoffRequestResponseModel toModel(TimeoffRequestEntity entity);

    List<TimeoffRequestResponseModel> toResponseModelList(List<TimeoffRequestEntity> entities);

//    @Mapping(source = "policy.policyName", target = "policyName")
//    @Mapping(source = "user.userId", target = "userId")
//    LeaveBalanceModel toModel(LeaveBalanceEntity entity);
//    List<LeaveBalanceModel> toBalanceModelList(List<LeaveBalanceEntity> entities);
}
