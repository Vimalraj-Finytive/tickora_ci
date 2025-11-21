package com.uniq.tms.tms_microservice.modules.leavemanagement.mapper;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffPolicyRequestDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffPolicyResponseDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeoffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffPolicyRequestModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffPolicyResponseModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeoffPoliciesModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TimeOffPolicyEntityMapper {

    TimeoffPolicyEntity toEntity(TimeOffPolicyRequestDto dto);

    TimeoffPolicyEntity toEntity(TimeOffPolicyRequestModel model);

    TimeOffPolicyResponseModel toResponseModel(TimeoffPolicyEntity entity);

    TimeOffPolicyResponseDto toResponseDto(TimeoffPolicyEntity entity);

    TimeoffPoliciesModel toModel(TimeoffPolicyEntity entity);

    TimeoffPolicyEntity toEntity(TimeoffPoliciesModel model);

    List<TimeoffPoliciesModel> toModelList(List<TimeoffPolicyEntity> entity);

//    @Mapping(source = "policy.policyName", target = "policyName")
//    @Mapping(source = "user.userId", target = "userId")
//    LeaveBalanceModel toModel(LeaveBalanceEntity entity);
//    List<LeaveBalanceModel> toBalanceModelList(List<LeaveBalanceEntity> entities);
}
