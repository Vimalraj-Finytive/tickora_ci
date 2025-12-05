package com.uniq.tms.tms_microservice.modules.leavemanagement.facade;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.AdminStatusUpdateDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.EmployeeStatusUpdateDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffRequestDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.AdminStatusUpdate;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.EmployeeStatusUpdate;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffRequest;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.LeaveBalanceService;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.TimeOffPolicyService;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.TimeOffRequestService;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.UserRole;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.shared.dto.EnumDto;
import com.uniq.tms.tms_microservice.shared.dto.EnumModel;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.TimeOffPolicyDtoMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.*;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeoffPoliciesDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeoffPolicyDto;

import java.time.LocalDate;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffPoliciesModel;
import java.util.List;
import java.util.Map;

@Component
public class TimeOffFacade {

    private final TimeOffPolicyService timeOffPolicyService;
    private final TimeOffPolicyDtoMapper timeoffPolicyDtoMapper;
    private final TimeOffRequestService timeOffRequestService;
    private final LeaveBalanceService leaveBalanceService;
    private final AuthHelper authHelper;

    public TimeOffFacade(TimeOffPolicyService timeOffPolicyService, TimeOffPolicyDtoMapper timeoffPolicyDtoMapper,
                         TimeOffRequestService timeOffRequestService, LeaveBalanceService leaveBalanceService, AuthHelper authHelper) {
        this.timeOffPolicyService = timeOffPolicyService;
        this.timeoffPolicyDtoMapper = timeoffPolicyDtoMapper;
        this.timeOffRequestService = timeOffRequestService;
        this.leaveBalanceService = leaveBalanceService;
        this.authHelper = authHelper;
    }

    public ApiResponse<TimeOffRequestDto> createRequest(TimeOffRequestDto requestDto) {
        String userId= authHelper.getUserId();
        requestDto.setUserId(userId);
        TimeOffRequest request = timeoffPolicyDtoMapper.toRequestModel(requestDto);
        timeOffRequestService.createRequest(request);
        return new ApiResponse<>(200,"Requested TimeOff Successfully",null);
    }

    public ApiResponse<EmployeeStatusUpdateDto> employeeUpdateStatus(EmployeeStatusUpdateDto dto){
        String userId= authHelper.getUserId();
        dto.setUserId(userId);
        EmployeeStatusUpdate model = timeoffPolicyDtoMapper.toStatusModel(dto);
        timeOffRequestService.employeeUpdateStatus(model);
        return new ApiResponse<>(200,"Update TimeOff Request Successfully",null);
    }

    public ApiResponse<AdminStatusUpdateDto> adminUpdateStatus(AdminStatusUpdateDto dto) {
        AdminStatusUpdate model = timeoffPolicyDtoMapper.toAdminStatusModel(dto);
        timeOffRequestService.adminUpdateStatus(model);
        return new ApiResponse<>(200, "Update TimeOff Request status Successfully", null);
    }

    public ApiResponse<Void> createPolicy(TimeOffPolicyRequestDto requestDto) {
        try {
            TimeOffPolicyRequestModel requestModel = timeoffPolicyDtoMapper.toRequestModel(requestDto);
            timeOffPolicyService.createPolicy(requestModel);
            return new ApiResponse<>(201, "Policy Created Successfully", null);

        } catch (IllegalArgumentException ex) {
            return new ApiResponse<>(400, ex.getMessage(), null);

        } catch (Exception ex) {
            return new ApiResponse<>(409, ex.getMessage(), null);
        }
    }

    public ApiResponse<List<EnumDto>> getDropDowns() {
        try {
            List<EnumModel> model = timeOffPolicyService.getDropDowns();
            List<EnumDto> dto = model.stream().map(timeoffPolicyDtoMapper::toDto).toList();;
            return new ApiResponse<>(200, "DropDowns Fetched Successfully", dto);

        } catch (IllegalArgumentException ex) {
            return new ApiResponse<>(400, ex.getMessage(), null);

        } catch (Exception ex) {
            return new ApiResponse<>(409, ex.getMessage(), null);
        }
    }

    public ApiResponse<Void> editPolicy(TimeOffPolicyEditRequestDto requestDto) {
        try {
            TimeOffPolicyEditRequestModel model = timeoffPolicyDtoMapper.toEditRequestModel(requestDto);
            timeOffPolicyService.editPolicy(model);

            return new ApiResponse<>(200, "Policy Updated Successfully", null);

        } catch (IllegalArgumentException ex) {
            return new ApiResponse<>(400, ex.getMessage(), null);

        } catch (Exception ex) {
            return new ApiResponse<>(409, ex.getMessage(), null);
        }
    }

    public ApiResponse<Void> assignPoliciesToUsers(TimeOffPolicyBulkAssignRequestDto dto) {
        try {
            TimeOffPolicyBulkAssignModel model = timeoffPolicyDtoMapper.toBulkAssignModel(dto);
            timeOffPolicyService.assignPolicies(model);
            return new ApiResponse<>(200, "Policies assigned successfully", null);

        } catch (IllegalArgumentException ex) {
            return new ApiResponse<>(400, ex.getMessage(), null);

        } catch (Exception ex) {
            return new ApiResponse<>(409, ex.getMessage(), null);
        }
    }

    public ApiResponse<Void> inactivatePolicy(String policyId, TimeOffPolicyInactivateRequestDto dto) {

        try {
            TimeOffPolicyInactivateModel model = timeoffPolicyDtoMapper.toInactivateModel(dto);
            timeOffPolicyService.inactivatePolicy(policyId, model);
            String message = model.getActive()
                    ? "Policy activated successfully"
                    : "Policy deactivated successfully";
            return new ApiResponse<>(200, message, null);

        } catch (IllegalArgumentException ex) {
            return new ApiResponse<>(400, ex.getMessage(), null);

        } catch (Exception ex) {
            return new ApiResponse<>(409, ex.getMessage(), null);
        }
    }


    public ApiResponse<List<TimeoffPoliciesDto>> getAllPolicies() {
        List<TimeOffPoliciesModel> models = timeOffPolicyService.getAllPolicies();
        List<TimeoffPoliciesDto> dtos =
                models.stream().map(timeoffPolicyDtoMapper::toDto)
                        .toList();
        return new ApiResponse<>(200, "Policies fetched successfully", dtos);
    }

    public ApiResponse<List<TimeoffPolicyDto>> getAllPolicy(){
        List<TimeOffPoliciesModel> model=timeOffPolicyService.getAllPolicy();
        List<TimeoffPolicyDto> dto=model.stream()
                .map(timeoffPolicyDtoMapper::toPolicyDto)
                .toList();
        return new ApiResponse<>(200,"policies fetched successfully",dto);
    }

    public ApiResponse<List<EnumDto>> getAccrualStatus(){
        List<EnumModel> model =timeOffPolicyService.getAccrualTypeStatus();
        List<EnumDto> dto=model.stream().map(timeoffPolicyDtoMapper::toDto).toList();
        return new ApiResponse<>(200,"AccrualType fetched successfully",dto);
    }

    public ApiResponse<List<EnumDto>>getCompensation(){
        List<EnumModel> model=timeOffPolicyService.getCompensation();
             List<EnumDto> dto = model.stream().map(timeoffPolicyDtoMapper::toDto).toList();
              return new ApiResponse<>(200,"Compensation fetched successfully",dto);
    }

    public ApiResponse<List<LeaveBalanceDto>> getLeaveBalance(String userId) {
        List<LeaveBalanceModel> model = leaveBalanceService.getLeaveBalance(userId);
        List<LeaveBalanceDto> dto = timeoffPolicyDtoMapper.toDtoLeaveList(model);
        return new ApiResponse<>(200, "leave balance fetched successfully", dto);
    }

    public ApiResponse<TimeoffPoliciesDto> getPolicyById(String id) {
        TimeoffPoliciesDto dto = timeoffPolicyDtoMapper.toDto(timeOffPolicyService.getPolicyById(id));
        return new ApiResponse<>(200, "policy fetched successfully", dto);
    }

    public ApiResponse<List<TimeoffPolicyDto>> getPolicyByUserId(String userId){
        List<TimeOffPoliciesModel> model=timeOffPolicyService.getPolicyByUserId(userId);
        List<TimeoffPolicyDto> dto = model.stream()
                .map(timeoffPolicyDtoMapper::toPolicyDto)
                .toList();
        return new ApiResponse<>(200, "Policies fetched successfully", dto);
    }

    public ApiResponse<Map<String, List<TimeOffRequestGroupDto>>> filterRequests(TimeOffExportRequest dto,String loggedUserId) {
        Map<String, List<TimeOffRequestGroupModel>> model = timeOffRequestService.filterRequests(dto,loggedUserId);
        Map<String, List<TimeOffRequestGroupDto>> dtoMap = timeoffPolicyDtoMapper.toDtoList(model);
        return new ApiResponse<>(200, "Requests fetched successfully", dtoMap);
    }

    public ApiResponse<List<TimeoffRequestResponseDto>> filterRequestsBasedOnRole(LocalDate fromDate, LocalDate toDate) {
        try {
            String roleName = authHelper.getRole();
            int minLevel = UserRole.getLevel(roleName);
            List<TimeOffRequestResponseModel> list = timeOffRequestService.filterRequestsByRole(fromDate, toDate, minLevel);
            List<TimeoffRequestResponseDto> dtoList = timeoffPolicyDtoMapper.toDtoList(list);

            return new ApiResponse<>(200, "Requests fetched successfully", dtoList);

        } catch (IllegalArgumentException ex) {
            return new ApiResponse<>(400, ex.getMessage(), null);
        } catch (Exception ex) {
            return new ApiResponse<>(409, ex.getMessage(), null);
        }
    }

    public ApiResponse<List<EnumDto>> getStatus() {
        List<EnumModel> model = timeOffRequestService.getStatus();
        List<EnumDto> dto = model.stream().map(timeoffPolicyDtoMapper::toDto).toList();
        return new ApiResponse<>(200, "Status fetched", dto);
    }


    public ApiResponse<Map<String,String>> startExportJob(TimeOffExportRequestDto request, String schema, String orgId) {
        String exportId = timeOffRequestService.startExporting(request, schema, orgId);
        return new ApiResponse<>(202,"Report generation Started",exportId);
    }

    public ApiResponse<Map<String, String>> getExportStatus(String schema, String orgId, String exportId) {
        String exportStatus = timeOffRequestService.exportStatus(exportId, schema, orgId);
        return new ApiResponse<>(200,"Fetched Report Status Successfully",exportStatus);
    }

    public ApiResponse<Resource> downloadExport(String schema, String orgId, String exportId, String type) {
        Resource downloadStatus = timeOffRequestService.downloadReport(exportId, schema, orgId, type);
        return new ApiResponse<>(200,"Report Downloaded Successfully",downloadStatus);
    }

    public ApiResponse<List<EnumDto>> getResetFrequencyStatus() {
        List<EnumModel> model = timeOffPolicyService.getResetFrequencyStatus();
        List<EnumDto> dto = model.stream()
                .map(timeoffPolicyDtoMapper::toDto)
                .toList();

        return new ApiResponse<>(200, "ResetFrequency fetched successfully", dto);
    }

    public ApiResponse<List<EnumDto>> getHourType() {
        List<EnumModel> model = timeOffRequestService.getHourType();
        List<EnumDto> dto = model.stream().map(timeoffPolicyDtoMapper::toDto).toList();
        return new ApiResponse<>(200, "Status fetched", dto);
    }

}
