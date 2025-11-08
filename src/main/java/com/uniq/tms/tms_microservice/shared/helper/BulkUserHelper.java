package com.uniq.tms.tms_microservice.shared.helper;

import com.uniq.tms.tms_microservice.modules.identityManagement.service.IdGenerationService;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.UserDto;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BulkUserHelper {

    private int rowNumber;
    private String[] row;
    private boolean valid;
    private String reason;
    private Long roleId;
    private List<Long> locationIds;
    private String email;
    private String scheduleId;
    private String groupName;

    private String secondaryName;
    private String secondaryMobile;
    private String secondaryEmail;
    private String relation;
    private boolean hasSecondary;

    public BulkUserHelper(int rowNumber, String[] row) {
        this.rowNumber = rowNumber;
        this.row = row;
    }

    public void invalidate(String reason) {
        this.valid = false;
        this.reason = reason;
    }

    public void markValid(String email, Long roleId, List<Long> locationIds, String scheduleId,
                          String groupName, String secondaryName, String secondaryMobile,
                          String secondaryEmail, String relation) {
        this.email = email;
        this.valid = true;
        this.roleId = roleId;
        this.locationIds = locationIds;
        this.scheduleId = scheduleId;
        this.groupName = groupName;
        this.secondaryName = secondaryName;
        this.secondaryMobile = secondaryMobile;
        this.secondaryEmail = secondaryEmail;
        this.relation = relation;
    }

    public void markHasSecondary(String name, String mobile, String email, String relation) {
        this.hasSecondary = true;
        this.secondaryName = name;
        this.secondaryMobile = mobile;
        this.secondaryEmail = email;
        this.relation = relation;
    }

    public boolean hasSecondary() {
        return hasSecondary;
    }

    public String getSecondaryName() {
        return secondaryName;
    }

    public String getSecondaryMobile() {
        return secondaryMobile;
    }

    public String getSecondaryEmail() {
        return secondaryEmail;
    }

    public String getRelation() {
        return relation;
    }


    public UserDto toUserDto(String orgId, IdGenerationService idService) {
        String[] row = this.row;
        UserDto dto = new UserDto();
        dto.setUserName(row[0].trim());
        dto.setEmail(row[1].trim());
        dto.setMobileNumber(row[2].trim());
        dto.setRoleId(roleId);
        dto.setWorkSchedule(scheduleId);
        dto.setIsRegisterUser(false);
        dto.setDateOfJoining(parseDate(row[5].trim()));
        dto.setUserId(idService.generateNextUserId(orgId));
        return dto;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public String[] getRow() {
        return row;
    }

    public boolean isValid() {
        return valid;
    }

    public String getReason() {
        return reason;
    }

    public Long getRoleId() {
        return roleId;
    }

    public List<Long> getLocationIds() {
        return locationIds;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public String getGroupName() {
        return groupName;
    }

    private LocalDate parseDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return LocalDate.parse(dateStr, formatter);
    }
}
