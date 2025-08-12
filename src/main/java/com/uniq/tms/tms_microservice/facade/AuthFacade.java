package com.uniq.tms.tms_microservice.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.mapper.TimesheetDtoMapper;
import com.uniq.tms.tms_microservice.mapper.UserDtoMapper;
import com.uniq.tms.tms_microservice.mapper.WorkScheduleDtoMapper;
import com.uniq.tms.tms_microservice.model.*;
import com.uniq.tms.tms_microservice.model.Privilege;
import com.uniq.tms.tms_microservice.service.*;
import com.uniq.tms.tms_microservice.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class AuthFacade {

    private final AuthService authService;
    private final UserService userService;
    private final UserDtoMapper userDtoMapper;
    private final TimesheetService timesheetService;
    private final TimesheetDtoMapper timesheetDtoMapper;
    private final WorkScheduleService workScheduleService;
    private final WorkScheduleDtoMapper workScheduleDtoMapper;
    private final ReportService reportService;
    private final OrganizationService organizationService;
    private final AuthUtil authUtil;

    public AuthFacade(AuthService authService, UserService userService, UserDtoMapper userDtoMapper, TimesheetService timesheetService, TimesheetDtoMapper timesheetDtoMapper, WorkScheduleService workScheduleService, WorkScheduleDtoMapper workScheduleDtoMapper, ReportService reportService, OrganizationService organizationService, AuthUtil authUtil) {

        this.authService = authService;
        this.userService = userService;
        this.userDtoMapper = userDtoMapper;
        this.timesheetService = timesheetService;
        this.timesheetDtoMapper = timesheetDtoMapper;
        this.workScheduleService = workScheduleService;
        this.workScheduleDtoMapper = workScheduleDtoMapper;
        this.reportService = reportService;
        this.organizationService = organizationService;
        this.authUtil = authUtil;
    }

    @Value("${csv.download.dir}")
    private String downloadDir;

    private final Logger log = LoggerFactory.getLogger(AuthFacade.class);

    public ResponseEntity<ApiResponse> handleLoginByEmail(String email, String password, HttpServletResponse
            response, HttpServletRequest request) {
        return authService.authenticateUserByEmail(email, password, response, request);
    }

    public ResponseEntity<ApiResponse> handleLogout(HttpServletRequest request, HttpServletResponse response) {
        return authService.logoutUser(request, response);
    }

    public ApiResponse getAllRole(String orgId, String role) {
        List<RoleDto> roles = userService.getAllRole(orgId, role).stream().map(userDtoMapper::toDto).toList();
        return new ApiResponse(
                200,
                "Roles fetched successfully",
                roles
        );
    }

    public ApiResponse getAllGroup(String orgId) {
        List<GroupDto> groups = userService.getAllGroup(orgId).stream().map(userDtoMapper::toGroupDto).toList();
        return new ApiResponse(
                200, "Groups fetched successfully", groups
        );
    }

    public ApiResponse getAllLocation(String orgId) {
        try {
            List<LocationDto> locations = userService.getAllLocation(orgId)
                    .stream()
                    .map(userDtoMapper::toDto)
                    .toList();
            return new ApiResponse(200, "Locations fetched successfully", locations);
        } catch (Exception e) {
            return new ApiResponse(500, "Internal Server Error: " + e.getMessage(), null);
        }
    }

    public ApiResponse createBulkUser(MultipartFile file) {
        String orgId = authUtil.getOrgId();
        String userId = authUtil.getUserId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        return userService.bulkCreateUsers(file, orgId, userId);
    }

    public ApiResponse createUser(UserDto userDto, SecondaryDetailsDto secondaryDetailsDto) {
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        ApiResponse user = userService.createUser(userDto, secondaryDetailsDto,orgId);
        return new ApiResponse(HttpStatus.CREATED.value(), "User Created successfully and Reset password link sent to email.", user);
    }

    public ApiResponse getUserProfile(String userId) {
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        if (userId == null) {
            userId = authUtil.getUserId();
        }
        UserProfileResponse response = userService.getUserProfile(orgId, userId);
        return new ApiResponse(HttpStatus.OK.value(), "User Profile fetched successfully", response);
    }

    public ResponseEntity<ApiResponse> validateEmail(EmailDto email) {
            ResponseEntity<ApiResponse> response = authService.forgotPassword(email.getEmail());
            return ResponseEntity.ok(response.getBody());
    }

    public ResponseEntity<ApiResponse> resetPassword(String email, ChangePasswordDto request) {
        return authService.resetPassword(email, request);
    }

    public ApiResponse updateUser( CreateUserDto updates, String userId) {
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        User user = userService.updateUser(updates, orgId, userId);
        return new ApiResponse(200, "User Updated successfully", user);
    }

    public ApiResponse getUsers() {
        
        String orgId = authUtil.getOrgId();
        String role = authUtil.getRole();
        role = role.replace("ROLE_", "").toUpperCase();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        List<UserResponseDto> users = userService.getUsers(orgId, role);
        return new ApiResponse(200, "Users fetched successfully", users);
    }

    public ApiResponse deleteUser( String userId) {
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        User user = userService.deleteUser(orgId, userId);
        return new ApiResponse(204, "User Deleted successfully", "No Content");
    }

    public ApiResponse createGroup( AddGroupDto addGroupDto) {
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        AddGroup groupMiddleware = userDtoMapper.toMiddleware(addGroupDto);
        try {
            AddGroup createdGroup = userService.createGroup(groupMiddleware, orgId);
            return new ApiResponse(201, "Group created successfully", true);
        } catch (DataIntegrityViolationException e) {
            return new ApiResponse(409, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse(500, "Internal Server Error: " + e.getMessage(), null);
        }
    }

    public ApiResponse addUserToGroup( AddMemberDto addMemberDto) {
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        AddMember addMemberMiddleware = userDtoMapper.toMiddleware(addMemberDto);
        try {
            return userService.addUserToGroup(addMemberMiddleware, orgId);

        } catch (DataIntegrityViolationException e) {
            return new ApiResponse(409, e.getMessage(), null);
        } catch (ResponseStatusException e) {
            return new ApiResponse(e.getStatusCode().value(), e.getReason(), null);
        } catch (Exception e) {
            return new ApiResponse(500, "Internal Server Error: " + e.getMessage(), null);
        }
    }

    public ApiResponse updateGroupDetails( AddGroupDto addGroupDto, Long groupId) {
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }

        log.info("Updating group: groupId={}, groupName={}, locationId={}",
                groupId, addGroupDto.getGroupName(), addGroupDto.getLocationId());
        return userService.updateGroupDetails(addGroupDto, groupId, orgId);

    }

    public ApiResponse getAllGroups() throws JsonProcessingException {
        String orgId = authUtil.getOrgId();
        String userId = authUtil.getUserId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        List<GroupResponseDto> groups = userService.getAllGroups(orgId, userId);
        return new ApiResponse(200, "All Groups Details fetched successfully", groups);
    }

    public ApiResponse deleteMember( Long groupId, String memberId) {
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        userService.deleteMember(groupId, memberId,orgId);
        return new ApiResponse(204, "Member Deleted successfully", "No Content");
    }

    public ApiResponse deleteGroup( Long groupId) {
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }

        userService.deleteGroup(groupId, orgId);
        return new ApiResponse(204, "Group Deleted successfully", "No Content");
    }

    public ApiResponse getMembers( Long roleId) {
        
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }

        List<User> members = userService.getMembers(orgId, roleId);

        // Convert to simplified structure: userId + userName
        List<Map<String, Object>> result = members.stream()
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", user.getUserId());
                    map.put("userName", user.getUserName());
                    return map;
                })
                .toList();
        return new ApiResponse(200, "Members fetched successfully", result);
    }

    public ApiResponse updateUserGroupType( EditUserGroupDto editUserGroupDto) {
        
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        UserGroup userGroup = userDtoMapper.toMiddleware(editUserGroupDto);
        boolean isUpdated = userService.updateUserGroupType(userGroup);
        if (!isUpdated) {
            throw new RuntimeException("Update failed: No matching user-group mapping found or type is the same.");
        }
        return new ApiResponse(200, "User group type updated successfully.", true);
    }

    public List<UserTimesheetResponseDto> getAllTimesheets( TimesheetReportDto request) {
        String orgId = authUtil.getOrgId();
        String userIdFromToken = authUtil.getUserId();
        if (orgId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        String role = authUtil.getRole();
        return timesheetService.getAllTimesheets(userIdFromToken, orgId, role, request);
    }

    public List<TimesheetHistoryDto> processTimesheetLogs( List<TimesheetHistoryDto> timesheetLogs) {
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        List<TimesheetHistory> middlewareLogs = timesheetLogs.stream()
                .map(timesheetDtoMapper::toMiddleware)
                .toList();
        List<TimesheetHistory> savedLogs = timesheetService.processTimesheetLogs(middlewareLogs);
        return savedLogs.stream()
                .map(timesheetDtoMapper::toDto)
                .toList();
    }

    public TimesheetDto updateClockInOut(String userId, LocalDate date, TimesheetDto request) {
        return timesheetService.updateClockInOut(userId, date, request);
    }

    public ApiResponse getUserGroups() {
        String orgId = authUtil.getOrgId();
        String role = authUtil.getRole();
        String userId = authUtil.getUserId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        List<GroupDto> groups = userService.getUserGroups(userId, role, orgId).stream()
                .toList();
        return new ApiResponse(200, "User Groups fetched successfully", groups);
    }

    public ApiResponse getUserGroupMembers( Long groupId, LocalDate date) {
        
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        String userIdFromToken = authUtil.getUserId();
        List<Map<String, Object>> groupMembers = userService.getGroupMembers(groupId, orgId, date, userIdFromToken);
        Map<String, Object> response = new HashMap<>();
        response.put("groupmember", groupMembers);

        return new ApiResponse(200, "Student members fetched successfully", response);
    }

    public TimesheetDto upsertClockInOut( String userId, LocalDate date, TimesheetDto request) {
        
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        return timesheetService.updateClockInOut(userId, date, request);
    }

    public ResponseEntity<ApiResponse> authenticateUserByMobile(String mobile, String otp, HttpServletResponse
            response, HttpServletRequest request) {
        return authService.authenticateUserByMobile(mobile, otp, response, request);
    }

    public ResponseEntity<ApiResponse> sendOTP(String mobile) {
        return authService.sendOtp(mobile);
    }

    public ApiResponse searchUsernames( String keyword) {
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        List<UserNameSuggestionDto> usernames = userService.searchUsernames(keyword);
        return new ApiResponse(200, "Usernames fetched successfully", usernames);
    }

    public ApiResponse getGroupUsers( List<Long> groupIds) {
        String orgId = authUtil.getOrgId();
        String loggedInUserId = authUtil.getUserId();
        String role = authUtil.getRole();
        String userRole = role.replace("ROLE_", "");
        if (orgId == null || loggedInUserId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization or User", null);
        }
        List<UserNameSuggestionDto> users;
        users = userService.getGroupUsers(groupIds, orgId, loggedInUserId, userRole);
        return new ApiResponse(200, "Users fetched successfully", users);
    }

    public ApiResponse getWorkSchedule(String orgId) {
        List<WorkScheduleDto> workScheduleDtos = workScheduleService.getAllWorkSchedules(orgId);
        return new ApiResponse(200, "Work Schedule fetched successfully", workScheduleDtos);
    }

    public ApiResponse addLocation( LocationDto locationDto) {
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        Location location = userService.addLocation(locationDto, orgId);
        return new ApiResponse(200, "Location added successfully", location);
    }

    public List<UserDashboardDto> getAllUserInfo( DashboardDto request) {
        LocalDate fromDate = request.getFromDate();
        LocalDate toDate = request.getToDate();
        if (toDate.isAfter(LocalDate.now())) {
            toDate = LocalDate.now();
        }
        String userId = request.getUserId();

        
            String orgId = authUtil.getOrgId();
        String userIdFromToken = authUtil.getUserId();

        if (orgId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        return timesheetService.getAllUserInfo(orgId, userIdFromToken, fromDate, toDate, userId, request.getGroupIds(), request.getType());
    }

    public byte[] exportTimesheetDayCsv(List<UserTimesheetResponseDto> timesheet, String sheetName) {
        return reportService.exportTimesheetDayCsv(timesheet, sheetName);
    }

    public byte[] exportTimesheetDayXlsx(List<UserTimesheetResponseDto> timesheets) {
        return reportService.exportTimesheetDayXlsx(timesheets);
    }

    public byte[] exportTimesheetWeekXlsx(List<UserTimesheetResponseDto> timesheets) {
        return reportService.exportTimesheetWeekXlsx(timesheets);
    }

    public byte[] exportWeekCsv(List<UserTimesheetResponseDto> timesheets) {
        return reportService.exportTimesheetWeekCsv(timesheets);
    }

    public FileExportResponseDto generateTimesheetFile( TimesheetReportDto request) throws IOException {
        List<UserTimesheetResponseDto> timesheets = getAllTimesheets( request);
        log.info("Requested Timesheet Date Range: {} to {}", request.getFromDate(), request.getToDate());
        log.info("Total timesheets fetched: {}", timesheets.size());

        String format = request.getFormat();
        String timePeriod = request.getTimePeriod();
        LocalDate startDate = request.getFromDate();
        LocalDate endDate = request.getToDate();

        if(request.getGroupId() != null && request.getGroupId().size() == 1){
            Long requestedGroupId  = request.getGroupId().get(0);
            String requestedGroupName = userService.findGroupName(requestedGroupId);
            log.info("Selected single Group name:{}", requestedGroupName);
            for (UserTimesheetResponseDto dto : timesheets){
                if (dto.getTimesheets() != null){
                    for (TimesheetDto timesheetDto : dto.getTimesheets()){
                        timesheetDto.setGroupname(requestedGroupName);
                    }
                }
            }
        }
        // 1. Build base filename
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String datePart = (timePeriod == null || timePeriod.trim().isEmpty()) ?
                "Timesheet_" + startDate.format(formatter) + "_to_" + endDate.format(formatter) :
                timePeriod.equalsIgnoreCase("DAY") ? "Timesheet_" + startDate.format(formatter) :
                        timePeriod.equalsIgnoreCase("WEEK") ? "Timesheet_Weekly" :
                                timePeriod.equalsIgnoreCase("MONTH") ? "Timesheet_Monthly" :
                                        timePeriod.equalsIgnoreCase("YEAR") ? "Timesheet_Yearly" :
                                                "Invalid or missing date range for time period";

        String baseName = "timesheetReport_" + datePart;
        String extension = "csv".equalsIgnoreCase(format) ? ".csv" : ".xlsx";

        // 2. Create directory
        Path resourcePath = Paths.get(downloadDir);
        if (!Files.exists(resourcePath)) {
            Files.createDirectories(resourcePath);
        }

        // 3. Avoid filename collisions
        String fileName = baseName + extension;
        Path filePath = resourcePath.resolve(fileName);
        int counter = 1;
        while (Files.exists(filePath)) {
            fileName = baseName + "(" + counter + ")" + extension;
            filePath = resourcePath.resolve(fileName);
            counter++;
        }

        // Sheet name
        String sheetName;
        if ("DAY".equalsIgnoreCase(timePeriod)) {
            sheetName = "Timesheet_" + startDate.format(formatter);
        } else if ("WEEK".equalsIgnoreCase(timePeriod)) {
            sheetName = "Timesheet_Weekly";
        } else if ("MONTH".equalsIgnoreCase(timePeriod)) {
            sheetName = "Timesheet_Monthly";
        } else {
            sheetName = "Timesheet_" + startDate.format(formatter) + "_to_" + endDate.format(formatter);
        }


        // 4. Generate file content
        byte[] data;
        if (Timeperiod.DAY.name().equalsIgnoreCase(timePeriod)) {
            data = "csv".equalsIgnoreCase(format) ?
                    exportTimesheetDayCsv(timesheets,sheetName) :
                    exportTimesheetDayXlsx(timesheets);
        } else {
            data = "csv".equalsIgnoreCase(format) ?
                    exportWeekCsv(timesheets) :
                    exportTimesheetWeekXlsx(timesheets);
        }

        // 5. Write to disk
        Files.write(filePath, data);

        // 6. Return file info
        return new FileExportResponseDto(filePath, fileName, format);
    }

    public ApiResponse getUserLocation() {
        String userId = authUtil.getUserId();
        List<LocationDto> locations = userService.getUserLocation(userId);
        return new ApiResponse(200, "User Location fetched successfully", locations);
    }

    public ResponseEntity<Resource> downloadSampleFile() {
        return userService.downloadSampleFile();}

    public List<UserTimesheetDto> getUserTimesheets( TimesheetReportDto request) {
        
        String orgId = authUtil.getOrgId();
        String userIdFromToken = authUtil.getUserId();
        if (orgId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        String role = authUtil.getRole();
        return timesheetService.getUserTimesheets(userIdFromToken, orgId, role, request);
    }

    public ApiResponse addPrivileges( PrivilegeDto privilegeDto) {
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        Privilege privilegeModel = userDtoMapper.toModel(privilegeDto);
        Privilege savePrivilege = userService.addPrivileges(privilegeModel, orgId);
        PrivilegeDto dto = userDtoMapper.toDto(savePrivilege);
        return new ApiResponse(200, "Privilege added successfully", dto);
    }

    public ApiResponse addRolwisePrivileges( RolePrivilegeDto rolePrivilegeDto) {
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        RolePrivilege rolePrivilegeModel = userDtoMapper.toModel(rolePrivilegeDto);
        RolePrivilege savePrivilege = userService.addRolwisePrivileges(rolePrivilegeModel, orgId);
        RolePrivilegeDto dto = userDtoMapper.toDto(savePrivilege);
        return new ApiResponse(200, "Privilege added successfully", dto);
    }

    public ApiResponse updateLocation( LocationListDto locationDto) {
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }

        LocationList location = userDtoMapper.toModel(locationDto);
        ApiResponse response = userService.updateLocation(orgId, location);
        return response;
    }

    public void deleteLocation( LocationListDto locationIds) {
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }

        userService.deleteLocation(locationIds, orgId);
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

    public void deleteSchedule( String scheduleId) {

        try {
            String orgId = authUtil.getOrgId();
            if (orgId == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Unauthorized - Invalid Organization");
            }
            workScheduleService.deleteWorkSchedule(orgId, scheduleId);
        } catch (RuntimeException e) {
            log.error("Error occurred while updating work schedule: {}", e.getMessage(), e);
            throw new RuntimeException( "Update failed: " + e.getMessage());
        }
    }

    public ApiResponse getStatus() {
        
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        List<TimesheetStatusDto> status = timesheetService.getStatus().stream()
                .map(timesheetDtoMapper::toStatusDto).
                toList();
        return new ApiResponse<>(200,"Timesheet Status fetched successfully",status);
    }

    public ApiResponse getType() {
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        List<WorkScheduleTypeDto> scheduleTypeEntities = workScheduleService.getAllTypes().stream()
                .map(workScheduleDtoMapper::toTypeDto)
                .toList();
        return new ApiResponse(200, "WorkSchedule Types fetched successfully", scheduleTypeEntities);
    }

    public ApiResponse createOrg(OrganizationDto organizationDto) {
        Organization organization = userDtoMapper.toModel(organizationDto);
        Organization response = organizationService.create(organization);
        return new ApiResponse(200,"Organization and Superadmin Created Successfully", response);
    }

    public ApiResponse validateOrg(OrganizationDto organizationDto) {
        Organization organization = userDtoMapper.toModel(organizationDto);
        Organization response = organizationService.validate(organization);
        return new ApiResponse<>(200,"Valid Organization", response);
    }

    public ApiResponse getOrgType() {
        return new ApiResponse<>(200,"Organization Type Fetched Successfully", organizationService.getOrgType());
    }

    public ApiResponse<OrgSetupValidationResponse> getValidation(String orgId) {
        OrgSetupValidationResponse validationResponse = organizationService.getValidation(orgId);
        return new ApiResponse<>(200, "Validation Successful", validationResponse);
    }

    public ApiResponse<OrganizationTypeDto> getUserOrgType() {
        
        String orgId = authUtil.getOrgId();
        if (orgId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
        }
        OrganizationType dto = organizationService.getUserOrgType(orgId);
        OrganizationTypeDto response = userDtoMapper.toDto(dto);
        return new ApiResponse<>(200, "User Organization Type Fetched Successfully", response);
    }

    public ApiResponse getInactiveUsers() {
        String orgId = authUtil.getOrgId();
        String role = authUtil.getRole();
        role = role.replace("ROLE_", "").toUpperCase();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        List<UserResponseDto> users = userService.getInactiveUsers(orgId, role);
        return new ApiResponse(200, "Users fetched successfully", users);
    }

    public ApiResponse updateIsActive(EditUserDto editUserDto) {
        String orgId = authUtil.getOrgId();
        String role = authUtil.getRole();
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        List<EditUserDto> editUserDtos = userService.updateIsActive(userDtoMapper.toMiddleware(editUserDto), orgId);
        return new ApiResponse(200,"User is activated", null);
    }
}
