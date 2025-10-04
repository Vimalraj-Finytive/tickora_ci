package com.uniq.tms.tms_microservice.modules.userManagement.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.*;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import com.uniq.tms.tms_microservice.modules.userManagement.constant.UserConstant;
import com.uniq.tms.tms_microservice.modules.userManagement.facade.UserFacade;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(UserConstant.User_Url)
public class UserController {

    private final UserFacade userFacade;
    private final AuthHelper authHelper;

    public UserController(UserFacade userFacade, AuthHelper authHelper) {
        this.userFacade = userFacade;
        this.authHelper = authHelper;
    }

    @GetMapping("/group")
    public ResponseEntity<ApiResponse> getAllGroup(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String orgId;
        try {
            orgId = authHelper.getOrgId();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(403, "Unauthorized", false));
        }

        return ResponseEntity.ok(userFacade.getAllGroup(orgId));
    }

    @PostMapping(
            value = "/createBulkUser",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> createBulkUser(@RequestParam(value = "file") MultipartFile file) {
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("Received file: {}", file.getOriginalFilename());
        logger.info("Request received... checking file param");
        logger.info("File name: {}", file != null ? file.getOriginalFilename() : "null");
        if (file.isEmpty()) {
            return new ResponseEntity<>(
                    new ApiResponse(400, "No file uploaded", null),
                    HttpStatus.BAD_REQUEST
            );
        }

        try {
            ApiResponse response = userFacade.createBulkUser(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(500, e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PostMapping("/createUser")
    public ResponseEntity<ApiResponse> createUser(
            @Valid @RequestBody CreateUserDto request,
            @RequestHeader("Authorization") String token) {
        if (request == null || request.getUser() == null) {
            throw new IllegalArgumentException("Request body or user details cannot be null.");
        }
        UserDto userDto = request.getUser();
        ApiResponse  response = userFacade.createUser(userDto, request.getSecondaryDetails());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/updateUser")
    public ResponseEntity<ApiResponse> updateUser(
            @RequestHeader("Authorization") String token,
            @RequestBody CreateUserDto updates,
            @RequestParam String userId) {

        ApiResponse response = userFacade.updateUser( updates, userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<ApiResponse> getUsers(@RequestHeader("Authorization") String token) {
        ApiResponse response = userFacade.getUsers();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getUser(@RequestHeader("Authorization") String token, @RequestParam(required = false) String userId){
        ApiResponse response = userFacade.getUserProfile( userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserNameSuggestionDto>>> searchUsers(@RequestHeader("Authorization") String token,@RequestParam String keyword) {
        ApiResponse response = userFacade.searchUsernames(keyword);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/deleteUser")
    public ResponseEntity<ApiResponse> deleteUser(@RequestHeader("Authorization") String token, @Valid @RequestBody DeactivateUserRequestDto requestDto) {
        ApiResponse response = userFacade.deleteUser( requestDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/createGroup")
    public ResponseEntity<ApiResponse> createGroup(@RequestHeader("Authorization") String token, @RequestBody AddGroupDto addGroupDto) {
        ApiResponse response = userFacade.createGroup( addGroupDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/editType")
    public ResponseEntity<ApiResponse> updateUserGroupType(@RequestHeader("Authorization") String token, @RequestBody EditUserGroupDto editUserGroupDto){
        ApiResponse response = userFacade.updateUserGroupType(editUserGroupDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/addMember")
    public ResponseEntity<ApiResponse> addUserToGroup(
            @RequestHeader("Authorization") String token,
            @RequestBody AddMemberDto addMemberDto) {

        ApiResponse response = userFacade.addUserToGroup( addMemberDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/updateGroup")
    public ResponseEntity<ApiResponse> updateGroup(@RequestHeader("Authorization") String token, @RequestBody AddGroupDto addGroupDto, @RequestParam Long groupId){
        ApiResponse response = userFacade.updateGroupDetails( addGroupDto,groupId);

        return ResponseEntity.status((response.getStatusCode())).body(response);
    }

    @GetMapping("/getAllGroups")
    public ResponseEntity<ApiResponse> getAllGroups(@RequestHeader("Authorization") String token) throws JsonProcessingException {
        ApiResponse response = userFacade.getAllGroups();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/deleteMember")
    public ResponseEntity<ApiResponse> deleteMember(@RequestHeader("Authorization") String token,
                                                    @RequestParam Long groupId,
                                                    @RequestParam String memberId) {
        ApiResponse response = userFacade.deleteMember( groupId, memberId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/deleteGroup")
    public ResponseEntity<ApiResponse> deleteGroup(@RequestHeader("Authorization") String token, @RequestParam Long groupId) {
        ApiResponse response = userFacade.deleteGroup( groupId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/getMembers")
        public ResponseEntity<ApiResponse> getMembers(@RequestHeader("Authorization") String token, @RequestParam(required = false) Long roleId) {
        ApiResponse response = userFacade.getMembers( roleId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/getUserGroups")
    public ResponseEntity<ApiResponse> getUserGroups(@RequestHeader("Authorization") String token) {
        ApiResponse response = userFacade.getUserGroups();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/getGroupMembers")
    public ResponseEntity<ApiResponse> getUserGroupMembers(@RequestHeader("Authorization") String token,
                                                           @RequestParam Long groupId,@RequestParam(required = false) LocalDate date) {
        ApiResponse response = userFacade.getUserGroupMembers( groupId, date);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/getGroupUsers")
    public ResponseEntity<ApiResponse> getGroupUsers(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) List<Long> groupIds) {
        try {
            ApiResponse response = userFacade.getGroupUsers( groupIds);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(403,"Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @GetMapping("/download-sample-file")
    public ResponseEntity<Resource> downloadSampleFile() { return userFacade.downloadSampleFile(); }

    @GetMapping("/getInactiveUsers")
    public ResponseEntity<ApiResponse> getInactiveUsers(@RequestHeader("Authorization") String token) {
        ApiResponse response = userFacade.getInactiveUsers();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/activateUser")
    public ResponseEntity<ApiResponse> updateIsActive(@RequestHeader("Authorization") String token , @RequestBody EditUserDto editUserDto){
        ApiResponse response = userFacade.updateIsActive(editUserDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/userHistoryLog")
    public ResponseEntity<ApiResponse<List<UserHistoryResponseDto>>> getUserHistoryLog(
            @RequestHeader("Authorization") String token,
            @RequestBody UserValidationRequestDto requestDto) {

        ApiResponse<List<UserHistoryResponseDto>> response = userFacade.getUserHistoryLog(requestDto.getUserId());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/validation")
    public ResponseEntity<ApiResponse<UserValidationDto>> validateUser(@RequestHeader("Authorization") String token,
                                                                       @RequestBody UserValidationDto request){
        ApiResponse<UserValidationDto> response = userFacade.validateUser(request.getUserId());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


}
