package com.uniq.tms.tms_microservice.modules.leavemanagement.mapper;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TimeOffPolicyDtoMapper {
    TimeOffPolicyRequestModel toRequestModel(TimeOffPolicyRequestDto dto);


//    @Mapping(target = "validityStartDate", source = "validityStartDate")
//    @Mapping(target = "validityEndDate", source = "validityEndDate")
//    @Mapping(target = "userValidFrom", source = "userValidFrom")
//    @Mapping(target = "userValidTo", source = "userValidTo")
//    @Mapping(target = "userIds", source = "userIds")
//    @Mapping(target = "groupIds", source = "groupIds")

//    @Mapping(target = "entitledUnits", source  ="entitledUnits")
//    @Mapping(target = "entitledHours", source = "entitledHours")
//    @Mapping(target = "carryForward", source = "carryForward")
//    @Mapping(target = "maxCarryForwardUnits", source = "maxCarryForwardUnits")
//    @Mapping(target = "policyName", source = "policyName")
    TimeOffPolicyEditRequestModel toEditRequestModel(TimeOffPolicyEditRequestDto dto);

    TimeOffPolicyResponseDto toResponseDto(TimeOffPolicyResponseModel model);

    EntitledTypeDropdownDto toDto(EntitledTypeDropdownModel model);


//    @Mapping(target = "policyIds", source = "policyIds")
//    @Mapping(target = "userIds", source = "userIds")
//    @Mapping(target = "groupIds", source = "groupIds")
    TimeOffPolicyBulkAssignModel toBulkAssignModel(TimeOffPolicyBulkAssignRequestDto dto);


    TimeOffPolicyInactivateModel toInactivateModel(TimeOffPolicyInactivateRequestDto dto);

}
