package com.uniq.tms.tms_microservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uniq.tms.tms_microservice.constant.UserConstant;
import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.facade.AuthFacade;
import com.uniq.tms.tms_microservice.util.AuthUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(UserConstant.User_Url)
public class UserController {

    private final AuthFacade authFacade;
    private final AuthUtil authUtil;
    
    public UserController(AuthFacade authFacade, AuthUtil authUtil) {
        this.authFacade = authFacade;
        this.authUtil = authUtil;
    }

    @GetMapping("/role")
    public ResponseEntity<ApiResponse> getAllRole(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Logger logger = LoggerFactory.getLogger(getClass());
        try {
            String orgId = authUtil.getOrgId();
            if (orgId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(401, "Unauthorized - Invalid Organization", null));
            }
            String role = authUtil.getRole();
            return ResponseEntity.ok(authFacade.getAllRole(orgId, role));
        } catch (RuntimeException e) {
            logger.error("JWT Processing Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(403, "Unauthorized", false));
        }
    }

    @GetMapping("/group")
    public ResponseEntity<ApiResponse> getAllGroup(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String orgId;
        try {
            orgId = authUtil.getOrgId();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(403, "Unauthorized", false));
        }

        return ResponseEntity.ok(authFacade.getAllGroup(orgId));
    }

    @GetMapping("/location")
    public ResponseEntity<ApiResponse> getAllLocation(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("Received Authorization Header: {}", authHeader);
        try {
            String orgId = authUtil.getOrgId();
            if (orgId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(401, "Unauthorized - Invalid Organization", null));
            }
            return ResponseEntity.ok(authFacade.getAllLocation(orgId));
        } catch (RuntimeException e) {
            logger.error("JWT Processing Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(403, "Unauthorized", false));
        }
    }

    @PostMapping("/createBulkUser")
    public ResponseEntity<ApiResponse> createBulkUser(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String token
            ) {
        try {
            ApiResponse response = authFacade.createBulkUser(file);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse(500, e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
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
        ApiResponse  response = authFacade.createUser(userDto, request.getSecondaryDetails());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/updateUser")
    public ResponseEntity<ApiResponse> updateUser(
            @RequestHeader("Authorization") String token,
            @RequestBody CreateUserDto updates,
            @RequestParam String userId) {

        ApiResponse response = authFacade.updateUser( updates, userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<ApiResponse> getUsers(@RequestHeader("Authorization") String token) {
        ApiResponse response = authFacade.getUsers();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getUser(@RequestHeader("Authorization") String token, @RequestParam(required = false) String userId){
        ApiResponse response = authFacade.getUserProfile( userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserNameSuggestionDto>>> searchUsers(@RequestHeader("Authorization") String token,@RequestParam String keyword) {
        ApiResponse response = authFacade.searchUsernames(keyword);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deleteUser")
    public ResponseEntity<ApiResponse> deleteUser(@RequestHeader("Authorization") String token, @RequestParam String userId) {
        ApiResponse response = authFacade.deleteUser( userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/createGroup")
    public ResponseEntity<ApiResponse> createGroup(@RequestHeader("Authorization") String token, @RequestBody AddGroupDto addGroupDto) {
        ApiResponse response = authFacade.createGroup( addGroupDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/editType")
    public ResponseEntity<ApiResponse> updateUserGroupType(@RequestHeader("Authorization") String token, @RequestBody EditUserGroupDto editUserGroupDto){
        ApiResponse response = authFacade.updateUserGroupType(editUserGroupDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/addMember")
    public ResponseEntity<ApiResponse> addUserToGroup(
            @RequestHeader("Authorization") String token,
            @RequestBody AddMemberDto addMemberDto) {

        ApiResponse response = authFacade.addUserToGroup( addMemberDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/updateGroup")
    public ResponseEntity<ApiResponse> updateGroup(@RequestHeader("Authorization") String token,@RequestBody AddGroupDto addGroupDto,@RequestParam Long groupId){
        ApiResponse response = authFacade.updateGroupDetails( addGroupDto,groupId);

        return ResponseEntity.status((response.getStatusCode())).body(response);
    }

    @GetMapping("/getAllGroups")
    public ResponseEntity<ApiResponse> getAllGroups(@RequestHeader("Authorization") String token) throws JsonProcessingException {
        ApiResponse response = authFacade.getAllGroup(token);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/deleteMember")
    public ResponseEntity<ApiResponse> deleteMember(@RequestHeader("Authorization") String token,
                                                    @RequestParam Long groupId,
                                                    @RequestParam String memberId) {
        ApiResponse response = authFacade.deleteMember( groupId, memberId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/deleteGroup")
    public ResponseEntity<ApiResponse> deleteGroup(@RequestHeader("Authorization") String token, @RequestParam Long groupId) {
        ApiResponse response = authFacade.deleteGroup( groupId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/getMembers")
        public ResponseEntity<ApiResponse> getMembers(@RequestHeader("Authorization") String token, @RequestParam(required = false) Long roleId) {
        ApiResponse response = authFacade.getMembers( roleId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/getUserGroups")
    public ResponseEntity<ApiResponse> getUserGroups(@RequestHeader("Authorization") String token) {
        ApiResponse response = authFacade.getUserGroups();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/getGroupMembers")
    public ResponseEntity<ApiResponse> getUserGroupMembers(@RequestHeader("Authorization") String token,
                                                           @RequestParam Long groupId,@RequestParam(required = false) LocalDate date) {
        ApiResponse response = authFacade.getUserGroupMembers( groupId, date);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/getGroupUsers")
    public ResponseEntity<ApiResponse> getGroupUsers(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) List<Long> groupIds) {
        try {
            ApiResponse response = authFacade.getGroupUsers( groupIds);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(403,"Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("/addLocation")
    public ResponseEntity<ApiResponse> addLocation(@RequestHeader("Authorization") String token, @RequestBody LocationDto locationDto) {
        ApiResponse response = authFacade.addLocation( locationDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/getUserLocation")
    public ResponseEntity<ApiResponse> getUserLocation(@RequestHeader("Authorization") String token) {
        ApiResponse response = authFacade.getUserLocation();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/download-sample-file")
    public ResponseEntity<Resource> downloadSampleFile() { return authFacade.downloadSampleFile(); }

    @PostMapping("/addPrivileges")
    public ResponseEntity<ApiResponse> addPrivileges(@RequestHeader("Authorization") String token,
                                                     @RequestBody PrivilegeDto privilegeDto) {
        ApiResponse response = authFacade.addPrivileges( privilegeDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/addRolwisePrivileges")
    public ResponseEntity<ApiResponse> addRolwisePrivileges(@RequestHeader("Authorization") String token,
                                                     @RequestBody RolePrivilegeDto rolePrivilegeDto) {
        ApiResponse response = authFacade.addRolwisePrivileges( rolePrivilegeDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/updateLocation")
    public ResponseEntity<ApiResponse> updateLocation(@RequestHeader("Authorization") String token,@RequestBody LocationListDto locationDto) {
        ApiResponse response = authFacade.updateLocation( locationDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> deleteLocation(@RequestHeader("Authorization") String token, @RequestBody LocationListDto locationIds) {
        authFacade.deleteLocation( locationIds);
        return ResponseEntity.noContent().build();
    }
}
