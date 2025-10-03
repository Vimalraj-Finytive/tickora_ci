package com.uniq.tms.tms_microservice.modules.locationManagement.controller;

import com.uniq.tms.tms_microservice.modules.locationManagement.dto.LocationDto;
import com.uniq.tms.tms_microservice.modules.locationManagement.dto.LocationListDto;
import com.uniq.tms.tms_microservice.modules.locationManagement.facade.LocationFacade;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class LocationController {

    private final LocationFacade locationFacade;
    private final AuthHelper authHelper;

    public LocationController(LocationFacade locationFacade, AuthHelper authHelper) {
        this.locationFacade = locationFacade;
        this.authHelper = authHelper;
    }

    @GetMapping("/location")
    public ResponseEntity<ApiResponse> getAllLocation(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("Received Authorization Header: {}", authHeader);
        try {
            String orgId = authHelper.getOrgId();
            if (orgId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(401, "Unauthorized - Invalid Organization", null));
            }
            return ResponseEntity.ok(locationFacade.getAllLocation(orgId));
        } catch (RuntimeException e) {
            logger.error("JWT Processing Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(403, "Unauthorized", false));
        }
    }

    @PostMapping("/addLocation")
    public ResponseEntity<ApiResponse> addLocation(@RequestHeader("Authorization") String token, @RequestBody LocationDto locationDto) {
        ApiResponse response = locationFacade.addLocation( locationDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/getUserLocation")
    public ResponseEntity<ApiResponse> getUserLocation(@RequestHeader("Authorization") String token) {
        ApiResponse response = locationFacade.getUserLocation();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PatchMapping("/updateLocation")
    public ResponseEntity<ApiResponse> updateLocation(@RequestHeader("Authorization") String token,@RequestBody LocationListDto locationDto) {
        ApiResponse response = locationFacade.updateLocation( locationDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> deleteLocation(@RequestHeader("Authorization") String token, @RequestBody LocationListDto locationIds) {
        locationFacade.deleteLocation( locationIds);
        return ResponseEntity.noContent().build();
    }
}
