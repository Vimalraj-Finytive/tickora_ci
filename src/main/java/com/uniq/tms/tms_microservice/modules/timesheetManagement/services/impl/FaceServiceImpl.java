package com.uniq.tms.tms_microservice.modules.timesheetManagement.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.uniq.tms.tms_microservice.modules.locationManagement.adapter.LocationAdapter;
import com.uniq.tms.tms_microservice.modules.locationManagement.dto.LocationDto;
import com.uniq.tms.tms_microservice.modules.locationManagement.services.LocationService;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.adapter.FaceAdapter;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.UserValidationDto;
import com.uniq.tms.tms_microservice.modules.userManagement.mapper.UserEntityMapper;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.*;
import com.uniq.tms.tms_microservice.modules.userManagement.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.modules.locationManagement.entity.LocationEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.RegisterDto;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.UserFaceEntity;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.LogType;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.mapper.TimesheetEntityMapper;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.mapper.UserFaceEntityMapper;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.model.TimesheetHistory;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.services.FaceService;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.services.TimesheetService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class FaceServiceImpl implements FaceService {

    private final Logger log = LogManager.getLogger(FaceServiceImpl.class);

    private final UserAdapter userAdapter;
    private final RestTemplate restTemplate;
    private final UserFaceEntityMapper userFaceEntityMapper;
    private final TimesheetService timesheetService;
    private final TimesheetEntityMapper timesheetEntityMapper;
    private final FaceAdapter faceAdapter;
    private final LocationAdapter locationAdapter;
    private final UserEntityMapper userEntityMapper;
    private final LocationService locationService;

    public FaceServiceImpl(UserAdapter userAdapter, RestTemplate restTemplate, UserFaceEntityMapper userFaceEntityMapper,
                           TimesheetService timesheetService, TimesheetEntityMapper timesheetEntityMapper,
                           FaceAdapter faceAdapter, LocationAdapter locationAdapter, UserEntityMapper userEntityMapper, LocationService locationService) {
        this.userAdapter = userAdapter;
        this.restTemplate = restTemplate;
        this.userFaceEntityMapper = userFaceEntityMapper;
        this.timesheetService = timesheetService;
        this.timesheetEntityMapper = timesheetEntityMapper;
        this.faceAdapter = faceAdapter;
        this.locationAdapter = locationAdapter;
        this.userEntityMapper = userEntityMapper;
        this.locationService = locationService;
    }

    @Value("${external.python.service.register.url}")
    private String faceRegisterServiceUrl;
    @Value("${external.python.service.faceComparison.url}")
    private String faceComparisonUrl;
    @Value("${external.python.service.multiFaceComparison.url}")
    private String multiFaceComparisonUrl;

    @Override
    public ApiResponse<RegisterDto> UserFaceRegister(RegisterDto registerDto, String orgSchema) {
        UserEntity userEntity = userAdapter.findById(registerDto.getUserId())
                .orElse(null);

        if (userEntity == null) {
            return new ApiResponse<>(404, "User Not Found", null);
        }

        if (!userEntity.isActive()) {
            return new ApiResponse<>(400, "User is Inactive", null);
        }

        if (userEntity.isRegisterUser()) {
            log.info("User face already exists for userId: {}", registerDto.getUserId());
            return new ApiResponse<>(400, "User Face Already Exists", null);
        }

        File convertFile = null;

        try {
            convertFile = File.createTempFile("face_", "_" + registerDto.getFaceImage().getOriginalFilename());
            registerDto.getFaceImage().transferTo(convertFile);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(convertFile));
            body.add("userId", registerDto.getUserId());
            body.add("orgSchema", orgSchema);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            log.info("Calling Python face registration service...");
            ResponseEntity<FaceRegisterResponseDto> response = restTemplate.postForEntity(
                    faceRegisterServiceUrl, requestEntity, FaceRegisterResponseDto.class
            );

            FaceRegisterResponseDto responseDto = response.getBody();

            if (responseDto == null || responseDto.getData() == null || responseDto.getData().isEmpty()) {
                log.error("Python service returned empty response for userId: {}", registerDto.getUserId());
                return new ApiResponse<>(500, "Face registration failed", null);
            }

            if (response.getStatusCode() == HttpStatus.CREATED) {
                UserEmbeddingDto userEmbeddingDto = responseDto.getData().getFirst();
                UserFaceEntity userFaceEntity = userFaceEntityMapper.toEntity(userEmbeddingDto);

                log.info("Saving user face entity for userId: {}", userFaceEntity.getUserId());
                faceAdapter.saveUserFace(userFaceEntity);

                userEntity.setRegisterUser(true);
                userAdapter.saveUser(userEntity);

                return new ApiResponse<>(201, "Face Registered Successfully", responseDto.getData());
            }

            return new ApiResponse<>(response.getStatusCode().value(), responseDto.getMessage(), responseDto.getData());

        } catch (HttpClientErrorException e) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(e.getResponseBodyAsString());

                int statusCode = root.has("statusCode")
                        ? root.get("statusCode").asInt()
                        : e.getStatusCode().value();

                String finalMessage = root.has("message")
                        ? root.get("message").asText()
                        : "Face registration failed";

                JsonNode dataNode = root.has("data") ? root.get("data") : null;

                log.warn("Face registration failed for userId: {}, reason: {}", registerDto.getUserId(), finalMessage);

                return new ApiResponse<>(statusCode, finalMessage, null);

            } catch (Exception parseEx) {
                log.error("Unexpected error while parsing Python response: {}", e.getResponseBodyAsString());
                return new ApiResponse<>(e.getStatusCode().value(), "Face registration failed", null);
            }
        } catch (IOException ioEx) {
            log.error("File handling error during face registration: {}", ioEx.getMessage());
            return new ApiResponse<>(500, "File processing failed. Please try again.", null);
        } finally {
            if (convertFile != null && convertFile.exists()) {
                boolean deleted = convertFile.delete();
                if (!deleted) {
                    log.warn("Failed to delete temp file: {}", convertFile.getAbsolutePath());
                }
            }
        }
    }


    @Override
    public ApiResponse<ClockInOutRequestDto> clockInOutUser(ClockInOutRequestDto registerDto, String orgSchema) {
        UserEntity userEntity = userAdapter.findById(registerDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        if (!userEntity.isActive()) {
            return new ApiResponse<>(400, "User is Inactive", null);
        }

        log.info("User found for clockIn/ClockOut");

        if (registerDto.getTimesheetLogs() != null && !registerDto.getTimesheetLogs().isEmpty()) {
            log.info("Location validation");
            for (TimesheetHistoryDto log : registerDto.getTimesheetLogs()) {
                if (log.getLocationId() == null || log.getLocationName() == null) {
                    return new ApiResponse<>(400, "Location ID and name are required for each timesheet log", null);
                }

                // Validate location in DB
                Optional<LocationEntity> location = locationAdapter.findLocationByLocationId(log.getLocationId());
                LocationEntity locationEntity = location.get();
                if (!locationEntity.getName().equals(log.getLocationName())) {
                    return new ApiResponse<>(400, "Location name does not match ID for log: " + log.getTimesheetHistoryId(), null);
                }
            }
        }
        log.info("reach try block");
        File convertFile = null;
        try {
            // Your existing face/image processing logic
            log.info("Get timesheet request");
            if (registerDto.getTimesheetLogsJson() != null && !registerDto.getTimesheetLogsJson().isBlank()) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                List<TimesheetHistoryDto> timesheetLogs = mapper.readValue(
                        registerDto.getTimesheetLogsJson(),
                        new TypeReference<List<TimesheetHistoryDto>>() {}
                );
                registerDto.setTimesheetLogs(timesheetLogs);
            }
            log.info("Convert file");
            convertFile = File.createTempFile("face_", "_" + registerDto.getFaceImage().getOriginalFilename());
            registerDto.getFaceImage().transferTo(convertFile);

            UserFaceEntity userFace = faceAdapter.findUserEmbeddingsById(registerDto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User Face Not Found"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(convertFile));
            body.add("userId", registerDto.getUserId());
            body.add("orgSchema", orgSchema);
            body.add("face_embedding", userFace.getEmbeddings());
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            log.info("Calling Python service for face comparison...");
            ResponseEntity<UserClockResponseDto> response =
                    restTemplate.postForEntity(faceComparisonUrl, requestEntity, UserClockResponseDto.class);

            UserClockResponseDto responseDto = response.getBody();

            if (response.getStatusCode() != HttpStatus.OK || responseDto == null) {
                log.error("Python API failed. Status: {}, Body: {}", response.getStatusCode(), responseDto);
                return new ApiResponse<>(500, "Face comparison service failed", null);
            }

            if (responseDto.getData() == null || responseDto.getData().isEmpty()) {
                log.error("Python API returned empty data: {}", responseDto);
                return new ApiResponse<>(400, "Face comparison failed: empty response", null);
            }

            UserClockStatusDto userClockStatusDto = responseDto.getData().getFirst();

            if (!userClockStatusDto.isFaceMatch()) {
                return new ApiResponse<>(400, "Face does not match", null);
            }

            log.info("Face matched successfully, saving timesheet history...");

            if (registerDto.getTimesheetLogs() != null && !registerDto.getTimesheetLogs().isEmpty()) {
                List<TimesheetHistory> middlewareLogs =
                        registerDto.getTimesheetLogs().stream()
                                .map(timesheetEntityMapper::toMiddleware)
                                .toList();

                // Here, processTimesheet might throw IllegalStateException for double clock-in
                try {
                    timesheetService.processTimesheetLogs(middlewareLogs);
                } catch (IllegalStateException ex) {
                    log.warn("Timesheet validation failed: {}", ex.getMessage());
                    return new ApiResponse<>(400, ex.getMessage(), null);
                }

                LogType logType = registerDto.getTimesheetLogs().getFirst().getLogType();
                String logTypeMessage = (logType == LogType.CLOCK_IN) ? "ClockIn Success" : "ClockOut Success";

                return new ApiResponse<>(200, logTypeMessage, null);
            }

            return new ApiResponse<>(200, "Clock operation successful", null);

        } catch (HttpClientErrorException e) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(e.getResponseBodyAsString());

                int statusCode = root.has("statusCode")
                        ? root.get("statusCode").asInt()
                        : e.getStatusCode().value();

                String finalMessage = root.has("message")
                        ? root.get("message").asText()
                        : "User Clock In/Out failed";

                JsonNode dataNode = root.has("data") ? root.get("data") : null;

                log.warn("User Clock In/Out failed for userId: {}, reason: {}", registerDto.getUserId(), finalMessage);

                return new ApiResponse<>(statusCode, finalMessage, null);

            } catch (Exception parseEx) {
                log.error("Unexpected error while parsing Python response for Clock In/Out: {}", e.getResponseBodyAsString());
                return new ApiResponse<>(e.getStatusCode().value(), "User Clock In/Out failed", null);
            }
        } catch (IOException ioEx) {
            log.error("File handling error during User Clock In/Out: {}", ioEx.getMessage());
            return new ApiResponse<>(500, "File processing failed. Please try again.", null);
        } finally {
            if (convertFile != null && convertFile.exists()) {
                boolean deleted = convertFile.delete();
                if (!deleted) {
                    log.warn("Failed to delete temp file for Clock In/out: {}", convertFile.getAbsolutePath());
                }
            }
        }
    }

    @Override
    public ApiResponse<RegisterDto> compareMultiFace(FaceDto faceDto, String orgSchema) {
        if (faceDto.getTimesheetLogs() != null && !faceDto.getTimesheetLogs().isEmpty()) {
            for (TimesheetHistoryDto log : faceDto.getTimesheetLogs()) {
                if (log.getLocationId() == null || log.getLocationName() == null) {
                    return new ApiResponse<>(400, "Location ID and name are required for each timesheet log", null);
                }

                Optional<LocationEntity> location = locationAdapter.findLocationByLocationId(log.getLocationId());
                if (location.isPresent()){
                    LocationEntity locationEntity = location.get();
                    if (!locationEntity.getName().equals(log.getLocationName())) {
                        return new ApiResponse<>(400, "Location name does not match ID for log: " + log.getTimesheetHistoryId(), null);
                    }
                }
            }
        }
        File convertFile = null;
        try{
            log.info("set the header and body for multi comparison");
             if (faceDto.getTimesheetLogsJson() != null && !faceDto.getTimesheetLogsJson().isBlank()) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                List<TimesheetHistoryDto> timesheetLogs = mapper.readValue(
                        faceDto.getTimesheetLogsJson(),
                        new TypeReference<List<TimesheetHistoryDto>>() {}
                );
                 faceDto.setTimesheetLogs(timesheetLogs);
            }

            convertFile = File.createTempFile("face_", "_" + faceDto.getFaceImage().getOriginalFilename());
            faceDto.getFaceImage().transferTo(convertFile);


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file",new FileSystemResource(convertFile));
            body.add("orgSchema",orgSchema);

            HttpEntity<MultiValueMap<String,Object>> requestEntity = new HttpEntity<>(body, headers);
            log.info("Call Python service for Multi face comparison");
            ResponseEntity<UserClockResponseDto> response = restTemplate.postForEntity(multiFaceComparisonUrl,requestEntity, UserClockResponseDto.class);
            UserClockResponseDto responseDto = response.getBody();

            if (response.getStatusCode() != HttpStatus.OK || responseDto == null) {
                log.error("Python API failed. Status: {}, Body: {}", response.getStatusCode(), responseDto);
                return new ApiResponse<>(500, "Face comparison service failed", null);
            }

            if (responseDto.getData() == null || responseDto.getData().isEmpty()) {
                log.error("Python API returned empty data: {}", responseDto);
                return new ApiResponse<>(400, "Face comparison failed: empty response", null);
            }

            UserClockStatusDto userClockStatusDto = responseDto.getData().getFirst();

            if (!userClockStatusDto.isFaceMatch()) {
                return new ApiResponse<>(400, "Face does not match", null);
            }

            if(userClockStatusDto.getUserId() != null){
                faceDto.getTimesheetLogs()
                        .forEach(log -> log.setUserId(userClockStatusDto.getUserId()));
                log.info("User Id from clock status : {}", userClockStatusDto.getUserId());
            }
            log.info("Get User Detail.");
            UserEntity user = userAdapter.getUserById(userClockStatusDto.getUserId());
            log.info("Face matched successfully, saving timesheet history...");

            if (faceDto.getTimesheetLogs() != null && !faceDto.getTimesheetLogs().isEmpty()) {
                List<TimesheetHistory> middlewareLogs =
                        faceDto.getTimesheetLogs().stream()
                                .map(timesheetEntityMapper::toMiddleware)
                                .toList();
                try {
                    timesheetService.processTimesheetLogs(middlewareLogs);
                } catch (IllegalStateException ex) {
                    log.warn("Timesheet validation failed: {}", ex.getMessage());
                    return new ApiResponse<>(400, ex.getMessage(), null);
                }

                LogType logType = faceDto.getTimesheetLogs().getFirst().getLogType();
                String logTypeMessage = (logType == LogType.CLOCK_IN) ? "ClockIn Success" : "ClockOut Success";
                Map<String, String> userResponse = new HashMap<>();
                userResponse.put("userId", userClockStatusDto.getUserId());
                userResponse.put("userName", user.getUserName());

                return new ApiResponse<>(200, logTypeMessage, userResponse);
            }

            return new ApiResponse<>(200, "Clock operation successful", null);

        } catch (HttpClientErrorException e) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(e.getResponseBodyAsString());

                int statusCode = root.has("statusCode")
                        ? root.get("statusCode").asInt()
                        : e.getStatusCode().value();

                String finalMessage = root.has("message")
                        ? root.get("message").asText()
                        : "User Clock In/Out failed";

                JsonNode dataNode = root.has("data") ? root.get("data") : null;

                log.warn("User Clock In/Out failed  reason: {}", finalMessage);

                return new ApiResponse<>(statusCode, finalMessage, null);

            } catch (Exception parseEx) {
                log.error("Unexpected error while parsing Python response for Multi face Clock In/Out: {}", e.getResponseBodyAsString());
                return new ApiResponse<>(e.getStatusCode().value(), "User Clock In/Out failed", null);
            }
        } catch (IOException ioEx) {
            log.error("File handling error during Multi User Clock In/Out: {}", ioEx.getMessage());
            return new ApiResponse<>(500, "File processing failed. Please try again.", null);
        } finally {
            if (convertFile != null && convertFile.exists()) {
                boolean deleted = convertFile.delete();
                if (!deleted) {
                    log.warn("Failed to delete temp file for Multi User Clock In/out: {}", convertFile.getAbsolutePath());
                }
            }
        }
    }

    @Override
    public ApiResponse<UserValidationDto> validateUser(String userId) {
        if (userId == null || userId.isBlank()) {
            log.warn("Validation failed: userId is null or blank");
            return new ApiResponse<>(400, "UserId must not be null or blank", null);
        }

        try {
            Optional<UserEntity> userOptional = userAdapter.findById(userId);
            if (userOptional.isEmpty()) {
                log.info("No User found for userId: {}", userId);
                return new ApiResponse<>(404, "User not found", null);
            }

            UserEntity userEntity = userOptional.get();
            UserValidationDto dto = userEntityMapper.toDto(userEntity);

            try {
                List<LocationDto> locations = locationService.getUserLocation(userId);
                if (locations == null || locations.isEmpty()) {
                    log.warn("No locations found for userId: {}", userId);
                    return new ApiResponse<>(404, "No locations assigned to this user", null);
                }
                log.info("User {} locations: {}", userId, locations);
                dto.setLocation(locations);
            } catch (Exception ex) {
                log.error("Error fetching locations for userId {}: {}", userId, ex.getMessage(), ex);
                return new ApiResponse<>(500, "Failed to fetch user locations", null);
            }

            log.info("User {} validated successfully", userId);
            return new ApiResponse<>(200, "User validation returned successfully", dto);

        } catch (Exception e) {
            log.error("Unexpected error during user validation for userId {}: {}", userId, e.getMessage(), e);
            return new ApiResponse<>(500, "Unexpected error during user validation", null);
        }
    }
}
