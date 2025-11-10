package com.uniq.tms.tms_microservice.modules.organizationManagement.mapper;

import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.SubscriptionEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionEntityMapper {
    // Entity → Model
    @Mapping(target = "subscriptionId", source = "subId")
    @Mapping(target = "planName", source = "planId")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "nextInvoiceDate", source = "endDate")
    Subscription toModel(SubscriptionEntity entity);

    List<Subscription> toModelList(List<SubscriptionEntity> entities);

    // Model → Entity
    @Mapping(target = "subId", source = "subscriptionId")
    @Mapping(target = "planId", source = "planName")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    @Mapping(target = "status", source = "status")
    SubscriptionEntity toEntity(Subscription model);

}
