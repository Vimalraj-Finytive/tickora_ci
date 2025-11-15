package com.uniq.tms.tms_microservice.modules.timesheetManagement.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.uniq.tms.tms_microservice.modules.locationManagement.dto.LocationDto;
import com.uniq.tms.tms_microservice.modules.locationManagement.services.LocationService;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.adapter.FaceAdapter;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.UserValidationDto;
import com.uniq.tms.tms_microservice.modules.userManagement.mapper.UserEntityMapper;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.*;
import com.uniq.tms.tms_microservice.modules.userManagement.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.RegisterDto;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.UserFaceEntity;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.LogType;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.mapper.TimesheetEntityMapper;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.mapper.UserFaceEntityMapper;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.model.TimesheetHistory;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.services.FaceService;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.services.TimesheetService;
import com.uniq.tms.tms_microservice.shared.helper.UserValidationHelper;
import com.uniq.tms.tms_microservice.shared.util.TimesheetLogParserUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
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
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FaceServiceImpl implements FaceService {

    private static final Logger log = LogManager.getLogger(FaceServiceImpl.class);

    private final UserAdapter userAdapter;
    private final RestTemplate restTemplate;
    private final UserFaceEntityMapper userFaceEntityMapper;
    private final TimesheetService timesheetService;
    private final TimesheetEntityMapper timesheetEntityMapper;
    private final FaceAdapter faceAdapter;
    private final UserEntityMapper userEntityMapper;
    private final LocationService locationService;
    private final UserValidationHelper userValidationHelper;
    private final Map<String, List<LocationDto>> locationCache;

    @Value("${external.python.service.register.url}")
    private String faceRegisterServiceUrl;

    @Value("${external.python.service.faceComparison.url}")
    private String faceComparisonUrl;

    @Value("${external.python.service.multiFaceComparison.url}")
    private String multiFaceComparisonUrl;

    public FaceServiceImpl(Map<String, List<LocationDto>> locationCache, UserAdapter userAdapter, RestTemplate restTemplate, UserFaceEntityMapper userFaceEntityMapper,
                           TimesheetService timesheetService, TimesheetEntityMapper timesheetEntityMapper,
                           FaceAdapter faceAdapter, UserEntityMapper userEntityMapper,
                           LocationService locationService, UserValidationHelper userValidationHelper) {
        this.locationCache = locationCache;
        this.userAdapter = userAdapter;
        this.restTemplate = restTemplate;
        this.userFaceEntityMapper = userFaceEntityMapper;
        this.timesheetService = timesheetService;
        this.timesheetEntityMapper = timesheetEntityMapper;
        this.faceAdapter = faceAdapter;
        this.userEntityMapper = userEntityMapper;
        this.locationService = locationService;
        this.userValidationHelper = userValidationHelper;
    }

    @Override
    public ApiResponse<RegisterDto> UserFaceRegister(RegisterDto registerDto, String orgSchema) {
        UserEntity userEntity = userValidationHelper.validateAndGetActiveUser(registerDto.getUserId());
        userValidationHelper.validateRegisteredUser(registerDto.getUserId());

        File tempFile = null;
        try {
            tempFile = File.createTempFile("face_", "_" + registerDto.getFaceImage().getOriginalFilename());
            registerDto.getFaceImage().transferTo(tempFile);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(tempFile));
            body.add("userId", registerDto.getUserId());
            body.add("orgSchema", orgSchema);
            log.info("Calling Python face registration service for userId: {}", registerDto.getUserId());
            ResponseEntity<FaceRegisterResponseDto> response =
                    restTemplate.postForEntity(faceRegisterServiceUrl, new HttpEntity<>(body, headers), FaceRegisterResponseDto.class);
            FaceRegisterResponseDto responseDto = response.getBody();
            if (responseDto == null || responseDto.getData() == null || responseDto.getData().isEmpty()) {
                return new ApiResponse<>(500, "Face registration failed: Empty response from service", null);
            }
            if (response.getStatusCode() == HttpStatus.CREATED) {
                UserEmbeddingDto userEmbeddingDto = responseDto.getData().getFirst();
                UserFaceEntity userFaceEntity = userFaceEntityMapper.toEntity(userEmbeddingDto);
                faceAdapter.saveUserFace(userFaceEntity);
                userEntity.setRegisterUser(true);
                userAdapter.saveUser(userEntity);
                log.info("Face registration completed successfully for userId: {}", registerDto.getUserId());
                return new ApiResponse<>(201, "Face Registered Successfully", responseDto.getData());
            }
            log.warn("Python service returned status {} for userId: {}", response.getStatusCode(), registerDto.getUserId());
            return new ApiResponse<>(response.getStatusCode().value(), responseDto.getMessage(), responseDto.getData());
        } catch (HttpClientErrorException e) {
            return handlePythonServiceError(e);
        } catch (IOException ioEx) {
            log.error("File handling error during face registration for userId {}: {}", registerDto.getUserId(), ioEx.getMessage());
            return new ApiResponse<>(500, "File processing failed. Please try again.", null);
        } finally {
            if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
                log.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath());
            }
        }
    }

    @Override
    public ApiResponse<ClockInOutRequestDto> clockInOutUser(ClockInOutRequestDto dto, String orgSchema) {
        long methodStart = System.currentTimeMillis();
        try {
            userValidationHelper.validateAndGetActiveUser(dto.getUserId());
            parseTimesheetLogs(dto);
            userValidationHelper.validateTimesheetLocations(dto.getTimesheetLogs());
            boolean isFaceMatched = validateFaceMatch(dto, orgSchema);
            if (!isFaceMatched) return new ApiResponse<>(400, "Face does not match", null);
            if (dto.getTimesheetLogs() != null && !dto.getTimesheetLogs().isEmpty()) {
                List<TimesheetHistory> middlewareLogs =
                        dto.getTimesheetLogs().stream()
                                .map(timesheetEntityMapper::toMiddleware)
                                .toList();
                try {
                    timesheetService.processTimesheetLogs(middlewareLogs);
                } catch (IllegalStateException ex) {
                    return new ApiResponse<>(400, ex.getMessage(), null);
                }
                LogType logType = dto.getTimesheetLogs().getFirst().getLogType();
                String message = (logType == LogType.CLOCK_IN) ? "Clock-In Success" : "Clock-Out Success";
                return new ApiResponse<>(200, message, null);
            }
            return new ApiResponse<>(200, "Clock operation successful", null);
        } catch (IllegalArgumentException ex) {
            return new ApiResponse<>(400, ex.getMessage(), null);
        } catch (HttpClientErrorException e) {
            return handlePythonServiceError(e);
        } catch (Exception ex) {
            log.error("Unexpected error in clockInOutUser: {}", ex.getMessage(), ex);
            return new ApiResponse<>(500, "An unexpected error occurred. Please try again.", null);
        } finally {
            log.info("Total execution time for clockInOutUser: {} ms", (System.currentTimeMillis() - methodStart));
        }
    }

    private void parseTimesheetLogs(ClockInOutRequestDto dto) {
        if (dto.getTimesheetLogsJson() != null && !dto.getTimesheetLogsJson().isBlank()) {
            dto.setTimesheetLogs(TimesheetLogParserUtil.parseLogs(dto.getTimesheetLogsJson()));
        }
    }

    private boolean validateFaceMatch(ClockInOutRequestDto dto, String orgSchema) throws IOException {
        UserFaceEntity userFace = faceAdapter.findUserEmbeddingsById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User Face Not Found"));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(dto.getFaceImage().getBytes()) {
            @Override
            public String getFilename() {
                return dto.getFaceImage().getOriginalFilename();
            }
        });
        body.add("userId", dto.getUserId());
        body.add("orgSchema", orgSchema);
        body.add("face_embedding", userFace.getEmbeddings());
        ResponseEntity<UserClockResponseDto> response =
                restTemplate.postForEntity(faceComparisonUrl, new HttpEntity<>(body, headers), UserClockResponseDto.class);
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null)
            throw new IllegalArgumentException("Face comparison service failed");
        UserClockResponseDto responseDto = response.getBody();
        if (responseDto.getData() == null || responseDto.getData().isEmpty())
            throw new IllegalArgumentException("Face comparison returned empty data");
        return responseDto.getData().getFirst().isFaceMatch();
    }

    @Override
    public ApiResponse<RegisterDto> compareMultiFace(FaceDto faceDto, String orgSchema, String userIdFromToken) {
        userValidationHelper.validateTimesheetLocations(faceDto.getTimesheetLogs());

        File convertFile = null;
        try {
            if (faceDto.getTimesheetLogsJson() != null && !faceDto.getTimesheetLogsJson().isBlank()) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                faceDto.setTimesheetLogs(mapper.readValue(faceDto.getTimesheetLogsJson(), new TypeReference<>() {}));
            }
            convertFile = File.createTempFile("face_", "_" + faceDto.getFaceImage().getOriginalFilename());
            faceDto.getFaceImage().transferTo(convertFile);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(convertFile));
            body.add("orgSchema", orgSchema);
            ResponseEntity<UserClockResponseDto> response =
                    restTemplate.postForEntity(multiFaceComparisonUrl, new HttpEntity<>(body, headers), UserClockResponseDto.class);
            UserClockResponseDto responseDto = response.getBody();
            if (response.getStatusCode() != HttpStatus.OK || responseDto == null)
                return new ApiResponse<>(500, "Face comparison service failed", null);
            if (responseDto.getData() == null || responseDto.getData().isEmpty())
                return new ApiResponse<>(400, "Face comparison failed: empty response", null);
            UserClockStatusDto statusDto = responseDto.getData().getFirst();
            if (!statusDto.isFaceMatch()) return new ApiResponse<>(400, "Face does not match", null);
            String userIdFromFace = statusDto.getUserId();
            if (userIdFromToken != null && userIdFromToken.equals(userIdFromFace))
                return new ApiResponse<>(400, "You cannot perform face comparison for your own ID", null);
            UserEntity faceUser = userValidationHelper.validateUserExists(userIdFromFace);
            UserEntity tokenUser = userValidationHelper.validateUserExists(userIdFromToken);
            userValidationHelper.validateHierarchy(faceUser, tokenUser);
            if (statusDto.getUserId() != null) {
                faceDto.getTimesheetLogs().forEach(log -> log.setUserId(statusDto.getUserId()));
            }
            UserEntity user = userAdapter.getUserById(statusDto.getUserId());
            List<TimesheetHistory> middlewareLogs = faceDto.getTimesheetLogs().stream()
                    .map(timesheetEntityMapper::toMiddleware)
                    .toList();
            try {
                timesheetService.processTimesheetLogs(middlewareLogs);
            } catch (IllegalStateException ex) {
                return new ApiResponse<>(400, ex.getMessage(), null);
            }
            LogType logType = faceDto.getTimesheetLogs().getFirst().getLogType();
            String message = (logType == LogType.CLOCK_IN) ? "ClockIn Success" : "ClockOut Success";
            Map<String, String> userResponse = Map.of(
                    "userId", statusDto.getUserId(),
                    "userName", user.getUserName()
            );
            return new ApiResponse<>(200, message, userResponse);
        } catch (HttpClientErrorException e) {
            return handlePythonServiceError(e);
        } catch (IOException ioEx) {
            log.error("File handling error during Multi User Clock In/Out: {}", ioEx.getMessage());
            return new ApiResponse<>(500, "File processing failed. Please try again.", null);
        } finally {
            if (convertFile != null && convertFile.exists() && !convertFile.delete()) {
                log.warn("Failed to delete temp file for Multi User Clock In/out: {}", convertFile.getAbsolutePath());
            }
        }
    }

    @Override
    public ApiResponse<UserValidationDto> validateUserLocation(String userId) {
        long start = System.currentTimeMillis();
        if (userId == null || userId.isBlank()) {
            return new ApiResponse<>(400, "UserId must not be null or blank", null);
        }
        try {
            UserEntity userEntity = userAdapter.findById(userId).orElse(null);
            if (userEntity == null) {
                return new ApiResponse<>(404, "User not found", null);
            }
            List<LocationDto> locations = getCachedUserLocations(userId);
            userValidationHelper.validateUserHasLocations(locations, userId);
            UserValidationDto dto = userEntityMapper.toDto(userEntity);
            dto.setLocation(locations);
            log.info("validateUserLocation success for userId: {} ({} ms)", userId, System.currentTimeMillis() - start);
            return new ApiResponse<>(200, "User validation returned successfully", dto);
        } catch (Exception e) {
            log.error("Error validating user {}: {}", userId, e.getMessage(), e);
            return new ApiResponse<>(500, "Unexpected error during user validation", null);
        }
    }

    private List<LocationDto> getCachedUserLocations(String userId) {
        return locationCache.computeIfAbsent(userId, id -> {
            try {
                return locationService.getUserLocation(id);
            } catch (Exception e) {
                log.error("Failed to fetch locations for userId {}: {}", id, e.getMessage());
                return Collections.emptyList();
            }
        });
    }

    private <T> ApiResponse<T> handlePythonServiceError(HttpClientErrorException e) {
        try {
            ObjectMapper objectMapper = TimesheetLogParserUtil.getObjectMapper();
            JsonNode root = objectMapper.readTree(e.getResponseBodyAsString());

            int statusCode = root.has("statusCode")
                    ? root.get("statusCode").asInt()
                    : e.getStatusCode().value();

            String message = root.has("message")
                    ? root.get("message").asText()
                    : "Face service error";

            return new ApiResponse<>(statusCode, message, null);
        } catch (Exception parseEx) {
            log.error("Failed to parse Python error response: {}", e.getResponseBodyAsString());
            return new ApiResponse<>(e.getStatusCode().value(), "Face service error", null);
        }
    }

}