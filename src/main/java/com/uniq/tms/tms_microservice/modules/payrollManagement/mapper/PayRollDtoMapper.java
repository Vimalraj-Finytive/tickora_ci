package com.uniq.tms.tms_microservice.modules.payrollManagement.mapper;

import com.uniq.tms.tms_microservice.modules.payrollManagement.dto.*;
import com.uniq.tms.tms_microservice.modules.payrollManagement.model.*;
import com.uniq.tms.tms_microservice.modules.payrollManagement.dto.PayRollDto;
import com.uniq.tms.tms_microservice.modules.payrollManagement.dto.PayRollSettingDto;
import com.uniq.tms.tms_microservice.modules.payrollManagement.dto.UserPayRollUpdateDto;
import com.uniq.tms.tms_microservice.modules.payrollManagement.dto.UserPayRollAmountDto;
import com.uniq.tms.tms_microservice.modules.payrollManagement.model.PayRollModel;
import com.uniq.tms.tms_microservice.modules.payrollManagement.model.PayRollSettingModel;
import com.uniq.tms.tms_microservice.modules.payrollManagement.model.UserPayRollAmountModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PayRollDtoMapper {
        PayRollSettingModel fromDto(PayRollSettingDto dto);
        PayRollModel fromDto(PayRollDto dto);
        PayRollDto toDto(PayRollModel model);
        List<PayRollSummaryDto> toSummaryDto(List<PayRollSummary> model);
        PayRollPaymentSummaryDto toPayrollPaymentDto(PayRollPaymentSummary model);
        @Mapping(source = "regularHrs", target = "regularHrs", qualifiedByName = "hrsToString")
        @Mapping(source = "overtimeHrs", target = "overtimeHrs", qualifiedByName = "hrsToString")
        @Mapping(source = "totalHrs", target = "totalHrs", qualifiedByName = "hrsToString")
        @Mapping(source = "regularDays", target = "regularDays", qualifiedByName = "daysToString")
        UserPayRollAmountDto toUserPayrollDto(UserPayRollAmountModel model);
        List<UserPayRollAmountDto> toDto(List<UserPayRollAmountModel> model);
        UserPayRollAmountModel toModel(UserPayRollUpdateDto dto);
        UserPayRollUpdateDto toDto(UserPayRollAmountModel model);
        PayRollUpdate toPayRollUpdateModel(PayRollUpdateDto dto);
        PayrollListResponseDto toListDto(PayRollListModel model);
        @Mapping(source = "payrollId", target = "payrollId")
        PayrollStatusUpdateModel toStatusUpdateModel(PayrollStatusUpdateDto dto);
        PayrollResponseDto toDto(PayRollResponseModel model);
        PayRollSettingenumDto toDto(PayRollSettingenumModel model);
        PayRollSettingenumModel toModel(PayRollSettingenumDto dto);
        PayRollStatusEnumDto toDto(PayRollStatusEnumModel model);
        PayRollStatusEnumModel toModel(PayRollStatusEnumDto dto);
        @Mapping(target = "payrollCalculation", expression = "java(model.getPayrollCalculation().getValue())")
        PayRollSettingDto toDto(PayRollSettingModel model);

        @Named("hrsToString")
        default String hrsToString(BigDecimal hours) {
        if (hours == null) return "0h 0m";

        double decimal = hours.doubleValue();
        int h = (int) decimal;
        int m = (int) Math.round((decimal - h) * 60);

        return h + "h " + m + "m";
       }

       @Named("daysToString")
        default String daysToString(Integer days) {
        if (days == null) return "0 days";
        return days + " days";
        }
}
