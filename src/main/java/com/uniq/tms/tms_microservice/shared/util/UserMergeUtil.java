package com.uniq.tms.tms_microservice.shared.util;

import com.uniq.tms.tms_microservice.modules.userManagement.dto.SecondaryDetailsDto;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.UserPolicyDto;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.UserResponseDto;
import com.uniq.tms.tms_microservice.modules.userManagement.enums.RoleName;
import com.uniq.tms.tms_microservice.modules.userManagement.projections.UserProjection;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserMergeUtil {

    public Map<String, UserResponseDto> mergeUserRecords(List<UserProjection> rows) {

        Map<String, UserResponseDto> merged = new LinkedHashMap<>();
        for (UserProjection p : rows) {
            merged.compute(p.getUserId(), (id, existing) -> {
                if (existing == null) {
                    existing = new UserResponseDto();
                    existing.setUserId(p.getUserId());
                    existing.setUserName(p.getUserName());
                    existing.setEmail(p.getEmail());
                    existing.setMobileNumber(p.getMobileNumber());
                    existing.setRoleName(p.getRoleName());
                    existing.setDateOfJoining(p.getDateOfJoining());
                    existing.setGroupName(new ArrayList<>());
                    existing.setLocationName(new ArrayList<>());
                    existing.setPolicies(new ArrayList<>());
                    existing.setSecondaryDetails(null);
                    existing.setCalendarName(p.getCalendarName());
                    existing.setCalendarId(p.getCalendarId());
                    existing.setRequestApproverName(p.getRequestApproverName());
                    existing.setPayrollName(p.getPayrollName());
                    existing.setOrganizationName(p.getOrganizationName());
                    existing.setOrgType(p.getOrgType());
                    existing.setScheduleName(p.getScheduleName());
                }
                if (p.getGroupName() != null &&
                        !existing.getGroupName().contains(p.getGroupName())) {
                    existing.getGroupName().add(p.getGroupName());
                }
                if (p.getLocationName() != null &&
                        !existing.getLocationName().contains(p.getLocationName())) {
                    existing.getLocationName().add(p.getLocationName());
                }
                if (p.getPolicyName() != null) {

                    boolean exists = existing.getPolicies().stream()
                            .anyMatch(x -> x.getPolicyName().equals(p.getPolicyName()));

                    if (!exists) {
                        UserPolicyDto dto = new UserPolicyDto();
                        dto.setPolicyId(p.getPolicyId());
                        dto.setPolicyName(p.getPolicyName());
                        dto.setValidFrom(p.getValidFrom());
                        dto.setValidTo(p.getValidTo());
                        existing.getPolicies().add(dto);
                    }
                }

                if (RoleName.STUDENT.getRoleName().equalsIgnoreCase(p.getRoleName())) {

                    if (p.getSecName() != null || p.getSecMobile() != null || p.getSecEmail() != null) {
                        if (existing.getSecondaryDetails() == null) {
                            SecondaryDetailsDto sec = new SecondaryDetailsDto();
                            sec.setUserName(p.getSecName());
                            sec.setMobile(p.getSecMobile());
                            sec.setEmail(p.getSecEmail());
                            sec.setRelation(p.getSecRelation());
                            existing.setSecondaryDetails(sec);
                        }
                    }
                }

                return existing;
            });
        }
        return merged;
    }
}
