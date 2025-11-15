package com.uniq.tms.tms_microservice.modules.payrollManagement.mapper;

import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.PayRollEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.PayRollSettingEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.UserPayRollAmountEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PayRollEntityMapper {
    PayRollSettingEntity toEntity(PayRollSettingModel model);
    PayRollModel toModel(PayRollEntity entity);
    PayRollEntity toEntity(PayRollModel model);
    @Mapping(target = "userName", source = "user.userName")
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(target = "monthlyNetSalary", source = "payroll.monthlySalary")
    UserPayRollAmountModel toModel(UserPayRollAmountEntity entity);
    List<UserPayRollAmountModel> toModel(List<UserPayRollAmountEntity> entity);
    PayRollResponseModel toResponseModel(PayRollEntity entity);
    PayRollListModel toListModel(PayRollEntity entity);
    PayRollSettingModel toModel(PayRollSettingEntity entity);
}
