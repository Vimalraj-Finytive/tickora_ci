package com.uniq.tms.tms_microservice.modules.leavemanagement.mapper;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeoffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeoffPoliciesModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import java.util.List;



@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TimeoffPolicyEntityMapper {
    TimeoffPoliciesModel toModel(TimeoffPolicyEntity entity);
    TimeoffPolicyEntity toEntity(TimeoffPoliciesModel model);
    List<TimeoffPoliciesModel> toModelList(List<TimeoffPolicyEntity> entity);
}
