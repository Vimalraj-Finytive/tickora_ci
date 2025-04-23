package com.uniq.tms.tms_microservice.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.config.JwtUtil;
import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.mapper.TimesheetDtoMapper;
import com.uniq.tms.tms_microservice.mapper.UserDtoMapper;
import com.uniq.tms.tms_microservice.model.*;
import com.uniq.tms.tms_microservice.service.AuthService;
import com.uniq.tms.tms_microservice.service.TimesheetService;
import com.uniq.tms.tms_microservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AuthFacade {
    private final UserAdapter userAdapter;
    private final AuthService authService;
    private final UserService userService;
    private final UserDtoMapper userDtoMapper;
    private final TimesheetService timesheetService;
    private final TimesheetDtoMapper timesheetDtoMapper;
    private final JwtUtil jwtUtil;


    public AuthFacade(UserAdapter userAdapter, AuthService authService, UserService userService, UserDtoMapper userDtoMapper, TimesheetService timesheetService, TimesheetDtoMapper timesheetDtoMapper, JwtUtil jwtUtil) {
        this.userAdapter = userAdapter;
        this.authService = authService;
        this.userService = userService;
        this.userDtoMapper = userDtoMapper;
        this.timesheetService = timesheetService;
        this.timesheetDtoMapper = timesheetDtoMapper;
        this.jwtUtil = jwtUtil;
    }

    public ResponseEntity<ApiResponse> handleLogin(String email, String password, HttpServletResponse response, HttpServletRequest request) {
        return authService.authenticateUser(email, password, response, request);
    }


    public ResponseEntity<ApiResponse> handleLogout(HttpServletRequest request, HttpServletResponse response) {
        return authService.logoutUser(request,response);
    }

    public ApiResponse getAllRole(Long orgId, String role) {
        List<RoleDto> roles =  userService.getAllRole(orgId, role).stream().map(userDtoMapper::toDto).toList();

        return new ApiResponse(
                200,
                "Roles fetched successfully",
                roles
        );
    }

    public ApiResponse getAllTeam() {
        List<GroupDto> teams = userService.getAllTeam().stream().map(userDtoMapper::toGroupDto).toList();

        return new ApiResponse(
                200, "Groups fetched successfully", teams
        );
    }

    public ApiResponse getAllLocation(Long orgId) {
        try {
            if (orgId == null) {
                return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
            }

            List<LocationDto> locations = userService.getAllLocation(orgId)
                    .stream()
                    .map(userDtoMapper::toDto)
                    .toList();

            return new ApiResponse(200, "Locations fetched successfully", locations);
        } catch (Exception e) {
            return new ApiResponse(500, "Internal Server Error: " + e.getMessage(), null);
        }
    }



    public ApiResponse createUser(UserDto userdto, String token) {

        if (!token.startsWith("Bearer ")) {
            return new ApiResponse(400, "Invalid token format", null);
        }
        String jwt = token.substring(7);
        Long orgId = jwtUtil.extractOrgIdFromToken(jwt);

        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        User usermiddleware = userDtoMapper.toMiddleware(userdto);
        User user = userService.createUser(usermiddleware, orgId);

        return new ApiResponse(201, "User Created successfully and Reset password link sent to email.", user);
    }


    public ResponseEntity<ApiResponse> validateEmail(EmailDto email) {
        UserEntity user = authService.validateEmailDto(email);

        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(404, "Email not registered", null));
            }

            if (user.isDefaultPassword()) {
                return ResponseEntity.ok(new ApiResponse(200, "Email validated", null));
            }
            return authService.forgotPassword(email.getEmail());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<ApiResponse> resetPassword(String email, ChangePasswordDto request) {
        return authService.resetPassword(email, request);
    }

    public ApiResponse updateUser(String token, Map<String, Object> updates, Long userId) {
        if (!token.startsWith("Bearer ")) {
            return new ApiResponse(400, "Invalid token format", null);
        }
        String jwt = token.substring(7);
        Long orgId = jwtUtil.extractOrgIdFromToken(jwt);

        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }

        User user = userService.updateUser(updates, orgId, userId);
        return new ApiResponse(200, "User Updated successfully", user);
    }

    public ApiResponse getUsers(String token) {
        if (!token.startsWith("Bearer ")) {
            return new ApiResponse(400, "Invalid token format", null);
        }
        String jwt = token.substring(7);
        Long orgId = jwtUtil.extractOrgIdFromToken(jwt);
        String role = jwtUtil.extractRoleFromToken(jwt);
        System.out.println("Role: " + role);
        role = role.replace("ROLE_", "").toUpperCase();
        System.out.println("role replaced: " + role);
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        List<UserResponseDto> users = userService.getUsers(orgId,role);
        return new ApiResponse(200, "Users fetched successfully", users);
    }

    public ApiResponse deleteUser(String token, Long userId) {
        if (!token.startsWith("Bearer ")) {
            return new ApiResponse(400, "Invalid token format", null);
        }
        String jwt = token.substring(7);
        Long orgId = jwtUtil.extractOrgIdFromToken(jwt);

        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        User user = userService.deleteUser(orgId, userId);
        return new ApiResponse(204, "User Deleted successfully", "No Content");
    }

    public ApiResponse createGroup(String token, AddGroupDto addGroupDto) {
        if (!token.startsWith("Bearer ")) {
            return new ApiResponse(400, "Invalid token format", null);
        }
        String jwt = token.substring(7);
        Long orgId = jwtUtil.extractOrgIdFromToken(jwt);

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


    public ApiResponse addUserToGroup(String token, AddMemberDto addMemberDto) {
        if (!token.startsWith("Bearer ")) {
            return new ApiResponse(400, "Invalid token format", null);
        }
        String jwt = token.substring(7);
        Long orgId = jwtUtil.extractOrgIdFromToken(jwt);

        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }

        AddMember addMemberMiddleware = userDtoMapper.toMiddleware(addMemberDto);

        try {
            List<User> member = userService.addUserToGroup(addMemberMiddleware,orgId);
            return new ApiResponse(201, "Users added successfully", true);
        } catch (DataIntegrityViolationException e) {
            return new ApiResponse(409, e.getMessage(), null);
        } catch(ResponseStatusException e){
            return new ApiResponse(e.getStatusCode().value(),e.getReason(),null);
        }
        catch (Exception e) {
            return new ApiResponse(500, "Internal Server Error: " + e.getMessage(), null);
        }
    }

    public ApiResponse updateGroupDetails(String token, AddGroupDto addGroupDto, Long groupId){
        if (!token.startsWith("Bearer ")) {
            return new ApiResponse(400, "Invalid token format", null);
        }
        String jwt = token.substring(7);
        Long orgId = jwtUtil.extractOrgIdFromToken(jwt);

        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }

        log.info("Updating group: groupId={}, groupName={}, locationId={}",
                groupId, addGroupDto.getGroupName(), addGroupDto.getLocationId());
        userService.updateGroupDetails(addGroupDto,groupId, orgId);

        return new ApiResponse(200,"Group details are updated successfullly", true);
    }

    public ApiResponse getAllGroups(String token) throws JsonProcessingException {
        if (!token.startsWith("Bearer ")) {
            return new ApiResponse(400, "Invalid token format", null);
        }
        String jwt = token.substring(7);
        Long orgId = jwtUtil.extractOrgIdFromToken(jwt);

        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        List<GroupResponseDto> groups=userService.getAllGroups(orgId);

        return new ApiResponse(200, "All Groups Details fetched successfully", groups);
    }

    public ApiResponse deleteMember(String token, Long groupId, Long memberId) {
        if (!token.startsWith("Bearer ")) {
            return new ApiResponse(400, "Invalid token format", null);
        }
        String jwt = token.substring(7);
        Long orgId = jwtUtil.extractOrgIdFromToken(jwt);

        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        userService.deleteMember(groupId, memberId);
        return new ApiResponse(204, "Member Deleted successfully", "No Content");
    }

    public ApiResponse deleteGroup(String token, Long groupId) {
        if (!token.startsWith("Bearer ")) {
            return new ApiResponse(400, "Invalid token format", null);
        }
        String jwt = token.substring(7);
        Long orgId = jwtUtil.extractOrgIdFromToken(jwt);        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }

        userService.deleteGroup(groupId);
        return new ApiResponse(204, "Group Deleted successfully", "No Content");
    }

    public ApiResponse getMembers(String token, String role) {
        if (!token.startsWith("Bearer ")) {
            return new ApiResponse(400, "Invalid token format", null);
        }

        String jwt = token.substring(7);
        Long orgId = jwtUtil.extractOrgIdFromToken(jwt);

        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }

        List<User> members;

        // if role is present, get users by role
        if (role != null && !role.isBlank()) {
            members = userService.getMembers(orgId, role);
        } else {
            // fetch all users except students
            members = userService.getMembersExcludingRole(orgId, "Student");
        }

        // Convert to simplified structure: userId + userName
        List<Map<String, Object>> result = members.stream()
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", user.getUserId());
                    map.put("userName", user.getUserName());
                    return map;
                })
                .collect(Collectors.toList());


        return new ApiResponse(200, "Members fetched successfully", result);
    }

    public ApiResponse updateUserGroupType(String token, EditUserGroupDto editUserGroupDto){
        if (!token.startsWith("Bearer ")) {
            return new ApiResponse(400, "Invalid token format", null);
        }
        String jwt = token.substring(7);
        Long orgId = jwtUtil.extractOrgIdFromToken(jwt);

        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }

        UserGroup userGroup = userDtoMapper.toMiddleware(editUserGroupDto);
        boolean isUpdated = userService.updateUserGroupType(userGroup);
        if(!isUpdated==true){
            throw new RuntimeException("Update failed: No matching user-group mapping found or type is the same.");
        }
        return new ApiResponse(200, "User group type updated successfully.", true);
    }

    public List<TimesheetDto> getAllTimesheets(LocalDate date, String timePeriod, Long userId) {
        return timesheetService.getAllTimesheets(date, timePeriod, userId);
    }

    public List<TimesheetHistoryDto> processTimesheetLogs(List<TimesheetHistoryDto> timesheetLogs) {
        List<TimesheetHistory> middlewareLogs = timesheetLogs.stream()
                .map(timesheetDtoMapper::toMiddleware)
                .toList();

        List<TimesheetHistory> savedLogs = timesheetService.processTimesheetLogs(middlewareLogs);

        return savedLogs.stream()
                .map(timesheetDtoMapper::toDto)
                .toList();
    }

    public TimesheetDto updateClockInOut(Long userId, LocalDate date, TimesheetDto request) {
        return timesheetService.updateClockInOut(userId, date, request);
    }

    public ApiResponse getUserGroups(String token, Long userId) {
        if (!token.startsWith("Bearer ")) {
            return new ApiResponse(400, "Invalid token format", null);
        }
        String jwt = token.substring(7);
        Long orgId = jwtUtil.extractOrgIdFromToken(jwt);

        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }

        List<GroupDto> groups = userService.getUserGroups(userId, orgId).stream()
                .toList();

        return new ApiResponse(200, "User Groups fetched successfully", groups);
    }

    public ApiResponse getUserGroupMembers(String token, Long groupId, LocalDate date) {
        if (!token.startsWith("Bearer ")) {
            return new ApiResponse(400, "Invalid token format", null);
        }

        String jwt = token.substring(7);
        Long orgId = jwtUtil.extractOrgIdFromToken(jwt);

        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }

        List<Map<String, Object>> groupMembers = userService.getStudentGroupMembers(groupId, orgId, date);
        Map<String, Object> response = new HashMap<>();
        response.put("groupmember", groupMembers);

        return new ApiResponse(200, "Student members fetched successfully", response);
    }


    public TimesheetDto upsertClockInOut(Long userId, LocalDate date, TimesheetDto request) {
        return timesheetService.updateClockInOut(userId, date, request);
    }
}

