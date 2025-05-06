package com.uniq.tms.tms_microservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uniq.tms.tms_microservice.config.JwtUtil;
import com.uniq.tms.tms_microservice.constant.UserConstant;
import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.facade.AuthFacade;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(UserConstant.User_Url)
public class UserController {

    private final AuthFacade authFacade;
    private final JwtUtil jwtUtil;

    public UserController(AuthFacade authFacade, JwtUtil jwtUtil) {
        this.authFacade = authFacade;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/role")
    public ResponseEntity<ApiResponse> getAllRole(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Logger logger = LoggerFactory.getLogger(getClass());

        if (authHeader == null || authHeader.isBlank()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(403, "Authorization header missing", false));
        }

        try {
            String jwt = jwtUtil.extractJwt(authHeader);
            logger.info("Extracted JWT: " + jwt);

            Long orgId = jwtUtil.extractOrgIdFromToken(jwt);
            if (orgId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(401, "Unauthorized - Invalid Organization", null));
            }

            String role = jwtUtil.extractRoleFromToken(jwt);
            return ResponseEntity.ok(authFacade.getAllRole(orgId, role));
        } catch (RuntimeException e) {
            logger.error("JWT Processing Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(403, "Unauthorized", false));
        }
    }

    @GetMapping("/group")
    public ResponseEntity<ApiResponse> getAllTeam(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String jwt = jwtUtil.extractJwt(authHeader);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(403, "Unauthorized", false));
        }

        return ResponseEntity.ok(authFacade.getAllTeam());
    }

    @GetMapping("/location")
    public ResponseEntity<ApiResponse> getAllLocation(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Logger logger = LoggerFactory.getLogger(getClass());

        logger.info("Received Authorization Header: {}", authHeader);

        if (authHeader == null || authHeader.isBlank()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(403, "Authorization header missing", false));
        }

        try {
            String jwt = jwtUtil.extractJwt(authHeader);
            logger.info("Extracted JWT: " + jwt);

            Long orgId = jwtUtil.extractOrgIdFromToken(jwt);
            if (orgId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(401, "Unauthorized - Invalid Organization", null));
            }

            return ResponseEntity.ok(authFacade.getAllLocation(orgId));
        } catch (RuntimeException e) {
            logger.error("JWT Processing Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(403, "Unauthorized", false));
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

        ApiResponse  response = authFacade.createUser(userDto, request.getSecondaryDetails(),token);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/updateUser")
    public ResponseEntity<ApiResponse> updateUser(
            @RequestHeader("Authorization") String token,
            @RequestBody CreateUserDto updates,
            @RequestParam Long userId) {

        ApiResponse response = authFacade.updateUser(token, updates, userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<ApiResponse> getUsers(@RequestHeader("Authorization") String token) {
        ApiResponse response = authFacade.getUsers(token);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getUser(@RequestHeader("Authorization") String token){
        ApiResponse response = authFacade.getUser(token);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserNameSuggestionDto>>> searchUsers(@RequestHeader("Authorization") String token,@RequestParam String keyword) {

        ApiResponse response = authFacade.searchUsernames(token,keyword);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deleteUser")
    public ResponseEntity<ApiResponse> deleteUser(@RequestHeader("Authorization") String token, @RequestParam Long userId) {
        ApiResponse response = authFacade.deleteUser(token, userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/createGroup")
    public ResponseEntity<ApiResponse> createGroup(@RequestHeader("Authorization") String token, @RequestBody AddGroupDto addGroupDto) {
        ApiResponse response = authFacade.createGroup(token, addGroupDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/editType")
    public ResponseEntity<ApiResponse> updateUserGroupType(@RequestHeader("Authorization") String token, @RequestBody EditUserGroupDto editUserGroupDto){
        ApiResponse response = authFacade.updateUserGroupType(token,editUserGroupDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/addMember")
    public ResponseEntity<ApiResponse> addUserToGroup(
            @RequestHeader("Authorization") String token,
            @RequestBody AddMemberDto addMemberDto) {

        ApiResponse response = authFacade.addUserToGroup(token, addMemberDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/updateGroup")
    public ResponseEntity<ApiResponse> updateGroup(@RequestHeader("Authorization") String token,@RequestBody AddGroupDto addGroupDto,@RequestParam Long groupId){
        ApiResponse response = authFacade.updateGroupDetails(token, addGroupDto,groupId);

        return ResponseEntity.status((response.getStatusCode())).body(response);
    }

    @GetMapping("/getAllGroups")
    public ResponseEntity<ApiResponse> getAllGroups(@RequestHeader("Authorization") String token) throws JsonProcessingException {
        ApiResponse response = authFacade.getAllGroups(token);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/deleteMember")
    public ResponseEntity<ApiResponse> deleteMember(@RequestHeader("Authorization") String token,
                                                    @RequestParam Long groupId,
                                                    @RequestParam Long memberId) {
        ApiResponse response = authFacade.deleteMember(token, groupId, memberId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/deleteGroup")
    public ResponseEntity<ApiResponse> deleteGroup(@RequestHeader("Authorization") String token, @RequestParam Long groupId) {
        ApiResponse response = authFacade.deleteGroup(token, groupId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/getMembers")
        public ResponseEntity<ApiResponse> getMembers(@RequestHeader("Authorization") String token, @RequestParam(required = false) Long roleId) {
        ApiResponse response = authFacade.getMembers(token, roleId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/getUserGroups")
    public ResponseEntity<ApiResponse> getUserGroups(@RequestHeader("Authorization") String token) {
        ApiResponse response = authFacade.getUserGroups(token);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/getGroupMembers")
    public ResponseEntity<ApiResponse> getUserGroupMembers(@RequestHeader("Authorization") String token,
                                                           @RequestParam Long groupId,@RequestParam LocalDate date) {
        ApiResponse response = authFacade.getUserGroupMembers(token, groupId, date);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
