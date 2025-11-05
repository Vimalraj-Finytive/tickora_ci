package com.uniq.tms.tms_microservice.modules.organizationManagement.mapper;

import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.MonthlyPaymentDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PlanDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.TopCustomersDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.MonthlyPaymentModel;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.Plan;
import com.uniq.tms.tms_microservice.modules.organizationManagement.model.TopCustomersModel;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentDtoMapper {
    List<MonthlyPaymentDto> toMonthlyPaymentDtoList(List<MonthlyPaymentModel> models);
    List<TopCustomersDto> toTopCustomersDtoList(List<TopCustomersModel> models);
}
