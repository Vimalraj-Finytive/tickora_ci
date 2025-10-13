package com.uniq.tms.tms_microservice.modules.organizationManagement.mapper;


import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.UpgradePlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.UpgradePlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UpgradeDtoMapper {
       // ---------- DTO → Model ----------
    @Mapping(target = "planId", source = "planId")
    @Mapping(target = "subscribedUserCount", source = "subscribedUserCount")
    @Mapping(target = "amount", source = "totalSubscriptionAmount")
    @Mapping(target = "orderId", source = "orderID")
    @Mapping(target = "success", source = "status")
    UpgradePlan toModel(UpgradePlanDto dto);

    List<UpgradePlan> toModelList(List<UpgradePlanDto> dtos);

}
