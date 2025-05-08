package com.uniq.tms.tms_microservice.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.config.JwtUtil;
import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.mapper.SecondaryDetailsMapper;
import com.uniq.tms.tms_microservice.mapper.TimesheetDtoMapper;
import com.uniq.tms.tms_microservice.mapper.UserDtoMapper;
import com.uniq.tms.tms_microservice.mapper.UserEntityMapper;
import com.uniq.tms.tms_microservice.model.*;
import com.uniq.tms.tms_microservice.service.AuthService;
import com.uniq.tms.tms_microservice.service.TimesheetService;
import com.uniq.tms.tms_microservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AuthFacade {

    private static final Long STUDENT_ROLE_ID = 5l;
    private final UserAdapter userAdapter;
    private final AuthService authService;
    private final UserService userService;
    private final UserDtoMapper userDtoMapper;
    private final TimesheetService timesheetService;
    private final TimesheetDtoMapper timesheetDtoMapper;
    private final JwtUtil jwtUtil;
    private final SecondaryDetailsMapper secondaryDetailsMapper;
    private final UserEntityMapper userEntityMapper;
    private final Validator validator;

    public AuthFacade(UserAdapter userAdapter, AuthService authService, UserService userService, UserDtoMapper userDtoMapper, TimesheetService timesheetService, TimesheetDtoMapper timesheetDtoMapper, JwtUtil jwtUtil, SecondaryDetailsMapper secondaryDetailsMapper, UserEntityMapper userEntityMapper, Validator validator) {

        this.userAdapter = userAdapter;
        this.authService = authService;
        this.userService = userService;
        this.userDtoMapper = userDtoMapper;
        this.timesheetService = timesheetService;
        this.timesheetDtoMapper = timesheetDtoMapper;
        this.jwtUtil = jwtUtil;
        this.secondaryDetailsMapper = secondaryDetailsMapper;
        this.userEntityMapper = userEntityMapper;
        this.validator = validator;
    }

    private final Logger log = LoggerFactory.getLogger(AuthFacade.class);

        public ResponseEntity<ApiResponse> handleLoginByEmail (String email, String password, HttpServletResponse
        response, HttpServletRequest request){
            return authService.authenticateUserByEmail(email, password, response, request);
        }

        public ResponseEntity<ApiResponse> handleLogout (HttpServletRequest request, HttpServletResponse response){
            return authService.logoutUser(request, response);
        }

        public ApiResponse getAllRole (Long orgId, String role){
            List<RoleDto> roles = userService.getAllRole(orgId, role).stream().map(userDtoMapper::toDto).toList();

            return new ApiResponse(
                    200,
                    "Roles fetched successfully",
                    roles
            );
        }

        public ApiResponse getAllTeam () {
            List<GroupDto> teams = userService.getAllTeam().stream().map(userDtoMapper::toGroupDto).toList();

            return new ApiResponse(
                    200, "Groups fetched successfully", teams
            );
        }

        public ApiResponse getAllLocation (Long orgId){
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

    public ApiResponse createBulkUser (MultipartFile file, String token){

        if (!token.startsWith("Bearer ")) {
            return new ApiResponse(400, "Invalid token format", null);
        }
        String jwt = token.substring(7);
        Long orgId = jwtUtil.extractOrgIdFromToken(jwt);

        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        return userService.bulkCreateUsers(file,orgId);
    }

    public ApiResponse createUser (UserDto userDto, SecondaryDetailsDto secondaryDetailsDto, String token){

        if (!token.startsWith("Bearer ")) {
            return new ApiResponse(400, "Invalid token format", null);
        }
        String jwt = token.substring(7);
        Long orgId = jwtUtil.extractOrgIdFromToken(jwt);
        if (orgId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
        }
        User usermiddleware = userDtoMapper.toMiddleware(userDto);
        if (userAdapter.existsByEmail(usermiddleware.getEmail())) {
            throw new DataIntegrityViolationException("User with email already exists");
        }
        if(userAdapter.existsByMobileNumber(usermiddleware.getMobileNumber())){
            throw new DataIntegrityViolationException( "User with mobile number already exists");
        }
        ApiResponse user = userService.createUser(userDto, orgId);
        if (userDto.getRoleId().equals(STUDENT_ROLE_ID)) {
            UserEntity mappedUser = userEntityMapper.toEntity((User) user.getData());
            if (secondaryDetailsDto == null) {
                userAdapter.deleteUser(mappedUser);
                return new ApiResponse(400, "Secondary details are null", null);
            }
            Set<ConstraintViolation<SecondaryDetailsDto>> violations = validator.validate(secondaryDetailsDto);
            if (!violations.isEmpty()) {
                String errorMsg = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(", "));
                userAdapter.deleteUser(mappedUser);
                return new ApiResponse(400, "Validation failed for secondary details: " + errorMsg, null);
            }
            boolean isMobileExist = userAdapter.existsMobileByMobile(secondaryDetailsDto.getMobile());
            if (isMobileExist) {
                userAdapter.deleteUser(mappedUser);
                throw new RuntimeException("Secondary User Mobile Number is Exist!");
            }
            if (secondaryDetailsDto.getEmail() != null) {
                boolean isEmailExist = userAdapter.existsEmailByEmail(secondaryDetailsDto.getEmail());
                if (isEmailExist) {
                    userAdapter.deleteUser(mappedUser);
                    throw new RuntimeException("Secondary User Email is Exist!");
                }
                boolean secondaryUserCreated = userService.createSecondaryUser(secondaryDetailsDto, mappedUser);
                if (!secondaryUserCreated) {
                    userAdapter.deleteUser(mappedUser);
                    return new ApiResponse(400, "Secondary details creation failed", null);
                }
            }
        }
        return new ApiResponse(201, "User Created successfully and Reset password link sent to email.", user);
    }

    public ApiResponse getUser(String token){
            if (!token.startsWith("Bearer ")) {
                return new ApiResponse(400, "Invalid token format", null);
            }
            String jwt = token.substring(7);
            Long orgId = jwtUtil.extractOrgIdFromToken(jwt);
            String role = jwtUtil.extractRoleFromToken(jwt);
            Long userId = jwtUtil.extractUserIdFromToken(jwt);
            role = role.replace("ROLE_", "").toUpperCase();
            if (orgId == null) {
                return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
            }
            UserProfileResponse response = userService.getUser(orgId,userId);
            return new ApiResponse(HttpStatus.OK.value(), "User fetched successfully",response);
        }

        public ResponseEntity<ApiResponse> validateEmail (EmailDto email){
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

        public ResponseEntity<ApiResponse> resetPassword (String email, ChangePasswordDto request){
            return authService.resetPassword(email, request);
        }

        public ApiResponse updateUser (String token, CreateUserDto updates, Long userId){
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

        public ApiResponse getUsers (String token){
            if (!token.startsWith("Bearer ")) {
                return new ApiResponse(400, "Invalid token format", null);
            }
            String jwt = token.substring(7);
            Long orgId = jwtUtil.extractOrgIdFromToken(jwt);
            String role = jwtUtil.extractRoleFromToken(jwt);
            role = role.replace("ROLE_", "").toUpperCase();
            if (orgId == null) {
                return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
            }
            List<UserResponseDto> users = userService.getUsers(orgId, role);
            return new ApiResponse(200, "Users fetched successfully", users);
        }

        public ApiResponse deleteUser (String token, Long userId){
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

        public ApiResponse createGroup (String token, AddGroupDto addGroupDto){
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

        public ApiResponse addUserToGroup (String token, AddMemberDto addMemberDto){
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
                return userService.addUserToGroup(addMemberMiddleware, orgId);

            } catch (DataIntegrityViolationException e) {
                return new ApiResponse(409, e.getMessage(), null);
            } catch (ResponseStatusException e) {
                return new ApiResponse(e.getStatusCode().value(), e.getReason(), null);
            } catch (Exception e) {
                return new ApiResponse(500, "Internal Server Error: " + e.getMessage(), null);
            }
        }

        public ApiResponse updateGroupDetails (String token, AddGroupDto addGroupDto, Long groupId){
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
            return userService.updateGroupDetails(addGroupDto, groupId, orgId);

        }

        public ApiResponse getAllGroups (String token) throws JsonProcessingException {
            if (!token.startsWith("Bearer ")) {
                return new ApiResponse(400, "Invalid token format", null);
            }
            String jwt = token.substring(7);
            Long orgId = jwtUtil.extractOrgIdFromToken(jwt);
            Long userId = jwtUtil.extractUserIdFromToken(jwt);
            if (orgId == null) {
                return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
            }
            List<GroupResponseDto> groups = userService.getAllGroups(orgId, userId);

            return new ApiResponse(200, "All Groups Details fetched successfully", groups);
        }

        public ApiResponse deleteMember (String token, Long groupId, Long memberId){
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

        public ApiResponse deleteGroup (String token, Long groupId){
            if (!token.startsWith("Bearer ")) {
                return new ApiResponse(400, "Invalid token format", null);
            }
            String jwt = token.substring(7);
            Long orgId = jwtUtil.extractOrgIdFromToken(jwt);
            if (orgId == null) {
                return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
            }

            userService.deleteGroup(groupId);
            return new ApiResponse(204, "Group Deleted successfully", "No Content");
        }

        public ApiResponse getMembers (String token, Long roleId){
            if (!token.startsWith("Bearer ")) {
                return new ApiResponse(400, "Invalid token format", null);
            }

            String jwt = token.substring(7);
            Long orgId = jwtUtil.extractOrgIdFromToken(jwt);

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

        public ApiResponse updateUserGroupType (String token, EditUserGroupDto editUserGroupDto){
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
            if (!isUpdated) {
                throw new RuntimeException("Update failed: No matching user-group mapping found or type is the same.");
            }
            return new ApiResponse(200, "User group type updated successfully.", true);
        }

        public List<TimesheetDto> getAllTimesheets (String token, TimesheetReportDto request) {

            if (!token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token format");
            }

            String jwt = token.substring(7);
            Long orgId = jwtUtil.extractOrgIdFromToken(jwt);

            Long userIdFromToken = jwtUtil.extractUserIdFromToken(jwt);
            if (orgId == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
            }
            String role = jwtUtil.extractRoleFromToken(jwt);
            return timesheetService.getAllTimesheets(userIdFromToken,orgId, role, request);
        }

        public List<TimesheetHistoryDto> processTimesheetLogs (String token, List < TimesheetHistoryDto > timesheetLogs) {
            if (!token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token format");
            }

            String jwt = token.substring(7);
            Long orgId = jwtUtil.extractOrgIdFromToken(jwt);

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

        public TimesheetDto updateClockInOut (Long userId, LocalDate date, TimesheetDto request){
            return timesheetService.updateClockInOut(userId, date, request);
        }

        public ApiResponse getUserGroups (String token){
            if (!token.startsWith("Bearer ")) {
                return new ApiResponse(400, "Invalid token format", null);
            }
            String jwt = token.substring(7);
            Long orgId = jwtUtil.extractOrgIdFromToken(jwt);
            String role = jwtUtil.extractRoleFromToken(jwt);
            Long userId = jwtUtil.extractUserIdFromToken(jwt);
            if (orgId == null) {
                return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
            }

            List<GroupDto> groups = userService.getUserGroups(userId, role, orgId).stream()
                    .toList();

            return new ApiResponse(200, "User Groups fetched successfully", groups);
        }

        public ApiResponse getUserGroupMembers (String token, Long groupId, LocalDate date){
            if (!token.startsWith("Bearer ")) {
                return new ApiResponse(400, "Invalid token format", null);
            }

            String jwt = token.substring(7);
            Long orgId = jwtUtil.extractOrgIdFromToken(jwt);

            if (orgId == null) {
                return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
            }
            Long userIdFromToken = jwtUtil.extractUserIdFromToken(jwt);
            List<Map<String, Object>> groupMembers = userService.getGroupMembers(groupId, orgId, date, userIdFromToken);
            Map<String, Object> response = new HashMap<>();
            response.put("groupmember", groupMembers);

            return new ApiResponse(200, "Student members fetched successfully", response);
        }

        public TimesheetDto upsertClockInOut (String token, Long userId, LocalDate date, TimesheetDto request){
            if (!token.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token format");
            }

            String jwt = token.substring(7);
            Long orgId = jwtUtil.extractOrgIdFromToken(jwt);

            if (orgId == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - Invalid Organization");
            }
            return timesheetService.updateClockInOut(userId, date, request);
        }

        public ResponseEntity<ApiResponse> authenticateUserByMobile (String mobile, String otp, HttpServletResponse
        response, HttpServletRequest request){
            return authService.authenticateUserByMobile(mobile, otp, response, request);
        }

        public ResponseEntity<ApiResponse> sendOTP (String mobile, HttpSession session){
            return authService.sendOtp(mobile, session);
        }

        public ApiResponse searchUsernames (String token, String keyword){
            if (!token.startsWith("Bearer ")) {
                return new ApiResponse(400, "Invalid token format", null);
            }

            String jwt = token.substring(7);
            Long orgId = jwtUtil.extractOrgIdFromToken(jwt);

            if (orgId == null) {
                return new ApiResponse(401, "Unauthorized - Invalid Organization", null);
            }
            List<UserNameSuggestionDto> usernames = userService.searchUsernames(keyword);
            return new ApiResponse(200, "Usernames fetched successfully", usernames);
        }

    public ApiResponse getGroupUsers(String token, List<Long> groupIds) {
        if (!token.startsWith("Bearer ")) {
            return new ApiResponse(400, "Invalid token format", null);
        }

        String jwt = token.substring(7);
        Long orgId = jwtUtil.extractOrgIdFromToken(jwt);
        Long loggedInUserId = jwtUtil.extractUserIdFromToken(jwt);
        String role = jwtUtil.extractRoleFromToken(jwt);
        String userRole = role.replace("ROLE_","");
        if (orgId == null || loggedInUserId == null) {
            return new ApiResponse(401, "Unauthorized - Invalid Organization or User", null);
        }
        List<UserNameSuggestionDto> users;
        users = userService.getGroupUsers(groupIds, orgId, loggedInUserId,userRole);
        return new ApiResponse(200, "Users fetched successfully", users);
    }
}
