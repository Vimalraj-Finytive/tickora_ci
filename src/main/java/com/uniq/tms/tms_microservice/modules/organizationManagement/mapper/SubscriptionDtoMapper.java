package com.uniq.tms.tms_microservice.modules.organizationManagement.mapper;

import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.SubscriptionDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.Subscription;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubscriptionDtoMapper {

    // Model → DTO
    @Mapping(target = "subscriptionId", source = "subscriptionId")
    @Mapping(target = "planName", source = "planName")
    @Mapping(target = "start", source = "startDate", dateFormat = "yyyy-MM-dd")
    @Mapping(target = "activeUntil", source = "endDate", dateFormat = "yyyy-MM-dd")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "nextInvoiceDate", source = "nextInvoiceDate", dateFormat = "yyyy-MM-dd")
    SubscriptionDto toDto(Subscription model);

    List<SubscriptionDto> toDtoList(List<Subscription> models);

    // DTO → Model
    @Mapping(target = "subscriptionId", source = "subscriptionId")
    @Mapping(target = "planName", source = "planName")
    @Mapping(target = "startDate", source = "start")
    @Mapping(target = "endDate", source = "activeUntil")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "nextInvoiceDate", source = "nextInvoiceDate")
    Subscription toModel(SubscriptionDto dto);
}
