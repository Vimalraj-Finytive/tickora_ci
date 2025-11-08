package com.uniq.tms.tms_microservice.shared.helper;

import com.uniq.tms.tms_microservice.modules.locationManagement.adapter.LocationAdapter;
import com.uniq.tms.tms_microservice.modules.locationManagement.dto.LocationDto;
import com.uniq.tms.tms_microservice.modules.locationManagement.entity.LocationEntity;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.TimesheetHistoryDto;
import com.uniq.tms.tms_microservice.modules.userManagement.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class UserValidationHelper {

    public static final Logger log = LogManager.getLogger(UserValidationHelper.class);

    private final UserAdapter userAdapter;
    private final LocationAdapter locationAdapter;

    public UserValidationHelper(UserAdapter userAdapter, LocationAdapter locationAdapter) {
        this.userAdapter = userAdapter;
        this.locationAdapter = locationAdapter;
    }

    /** Validate if a user exists and is active **/
    public UserEntity validateAndGetActiveUser(String userId) {
        UserEntity user = userAdapter.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!user.isActive()) {
            throw new IllegalArgumentException("User is Inactive");
        }
        return user;
    }

    /** Validate user presence only (used for hierarchy checks etc.) **/
    public UserEntity validateUserExists(String userId) {
        return userAdapter.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    /** Validate timesheet log locations **/
    public void validateTimesheetLocations(List<TimesheetHistoryDto> logs) {
        if (logs == null || logs.isEmpty()) return;
        List<Long> locationIds = logs.stream()
                .map(TimesheetHistoryDto::getLocationId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, LocationEntity> locationMap = locationAdapter.findAllLocationById(locationIds)
                .stream()
                .collect(Collectors.toMap(LocationEntity::getLocationId, loc -> loc));
        for (TimesheetHistoryDto log : logs) {
            if (log.getLocationId() == null || log.getLocationName() == null) {
                throw new IllegalArgumentException("Location ID and Name are required for all logs");
            }

            LocationEntity locationEntity = locationMap.get(log.getLocationId());
            if (locationEntity == null || !locationEntity.getName().equals(log.getLocationName())) {
                throw new IllegalArgumentException("Invalid location: " + log.getLocationName());
            }
        }
    }

    /** Check and compare hierarchy levels **/
    public void validateHierarchy(UserEntity faceUser, UserEntity tokenUser) {
        int faceUserLevel = faceUser.getRole().getHierarchyLevel();
        int tokenUserLevel = tokenUser.getRole().getHierarchyLevel();
        log.info("Hierarchy comparison: FaceUserLevel={} TokenUserLevel={}", faceUserLevel, tokenUserLevel);
        if (faceUserLevel <= tokenUserLevel) {
            throw new IllegalArgumentException("Access denied: insufficient hierarchy level");
        }
    }

    /** Validate user has assigned locations **/
    public void validateUserHasLocations(List<LocationDto> locations, String userId) {
        if (locations == null || locations.isEmpty()) {
            throw new IllegalArgumentException("No locations assigned to this user: " + userId);
        }
    }

    /** Validate user is registered or not **/
    public UserEntity validateRegisteredUser(String userId) {
        UserEntity user = userAdapter.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.isRegisterUser()) {
            throw new IllegalArgumentException("User is already Registered");
        }
        return user;
    }

}
