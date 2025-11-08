package com.uniq.tms.tms_microservice.modules.workScheduleManagement.facade;

import com.uniq.tms.tms_microservice.modules.workScheduleManagement.dto.WorkScheduleDto;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.dto.WorkScheduleTypeDto;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.mapper.WorkScheduleDtoMapper;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.model.WorkSchedule;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.services.WorkScheduleService;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class WorkScheduleFacade {

    private final Logger log = LoggerFactory.getLogger(WorkScheduleFacade.class);

    private final WorkScheduleService workScheduleService;
    private final WorkScheduleDtoMapper workScheduleDtoMapper;
    private final AuthHelper authHelper;

    public WorkScheduleFacade(WorkScheduleService workScheduleService, WorkScheduleDtoMapper workScheduleDtoMapper, AuthHelper authHelper) {
        this.workScheduleService = workScheduleService;
        this.workScheduleDtoMapper = workScheduleDtoMapper;
        this.authHelper = authHelper;
    }

    public ApiResponse getWorkSchedule(String orgId) {
        List<WorkScheduleDto> workScheduleDtos = workScheduleService.getAllWorkSchedules(orgId);
        return new ApiResponse(200, "Work Schedule fetched successfully", workScheduleDtos);
    }

    public ApiResponse createWorkSchedule(String orgId, WorkScheduleDto dto) {
        WorkSchedule model = workScheduleDtoMapper.toModel(dto);
        ApiResponse response = workScheduleService.createWorkSchedule(model, orgId);
        return ResponseEntity.ok(response).getBody();
    }

    public ApiResponse addType(WorkScheduleTypeDto type) {
        return workScheduleService.addType(type);
    }

    public ApiResponse updateWorkSchedule(String orgId, WorkScheduleDto dto) {
        WorkSchedule model = workScheduleDtoMapper.toModel(dto);
        workScheduleService.updateWorkSchedule( model, orgId);
        return new ApiResponse(200, "WorkSchedule Updated Successfully", true);
    }

    public void deleteSchedule(String scheduleId) {
        try {
            String orgId = authHelper.getOrgId();
            if (orgId == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
            }
            workScheduleService.deleteWorkSchedule(orgId, scheduleId);
        } catch (IllegalArgumentException | IllegalStateException | ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while deleting work schedule: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error while deleting work schedule", e);
        }
    }

    public ApiResponse getType() {
        String orgId = authHelper.getOrgId();
        if (orgId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        List<WorkScheduleTypeDto> scheduleTypeEntities = workScheduleService.getAllTypes().stream()
                .map(workScheduleDtoMapper::toTypeDto)
                .toList();
        return new ApiResponse(200, "WorkSchedule Types fetched successfully", scheduleTypeEntities);
    }

}
