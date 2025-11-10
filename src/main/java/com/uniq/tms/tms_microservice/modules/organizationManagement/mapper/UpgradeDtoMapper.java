package com.uniq.tms.tms_microservice.modules.organizationManagement.mapper;


import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.UpdatePlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.UpgradePlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.UpdatePlan;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.UpgradePlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UpgradeDtoMapper {
    @Mapping(target = "planId", source = "planId")
    @Mapping(target = "subscribedUserCount", source = "subscribedUserCount")
    @Mapping(target = "amount", source = "totalSubscriptionAmount")
    @Mapping(target = "orderId", source = "orderID")
    @Mapping(target = "success", source = "status")
    UpgradePlan toModel(UpgradePlanDto dto);

    @Mapping(target = "amount", source = "totalSubscriptionAmount")
    @Mapping(target = "orderId", source = "orderID")
    @Mapping(target = "success", source = "status")
    UpdatePlan toModel(UpdatePlanDto dto);

    List<UpgradePlan> toModelList(List<UpgradePlanDto> dtos);

}
