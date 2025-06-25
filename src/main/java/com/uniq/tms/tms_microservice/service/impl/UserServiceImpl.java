package com.uniq.tms.tms_microservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniq.tms.tms_microservice.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.adapter.WorkScheduleAapter;
import com.uniq.tms.tms_microservice.dto.AddGroupDto;
import com.uniq.tms.tms_microservice.dto.GroupDto;
import com.uniq.tms.tms_microservice.dto.GroupResponseDto;
import com.uniq.tms.tms_microservice.dto.UserGroupDto;
import com.uniq.tms.tms_microservice.dto.UserRole;
import com.uniq.tms.tms_microservice.entity.GroupEntity;
import com.uniq.tms.tms_microservice.entity.LocationEntity;
import com.uniq.tms.tms_microservice.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.entity.UserGroupEntity;
import com.uniq.tms.tms_microservice.entity.WorkScheduleEntity;
import com.uniq.tms.tms_microservice.event.LocationCacheReloadEvent;
import com.uniq.tms.tms_microservice.event.PrivilegeCacheReloadEvent;
import com.uniq.tms.tms_microservice.event.RolePrivilegesCacheReloadEvent;
import com.uniq.tms.tms_microservice.exception.CommonExceptionHandler;
import com.uniq.tms.tms_microservice.mapper.LocationEntityMapper;
import com.uniq.tms.tms_microservice.mapper.UserDtoMapper;
import com.uniq.tms.tms_microservice.mapper.UserEntityMapper;
import com.uniq.tms.tms_microservice.model.*;
import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.entity.*;
import com.uniq.tms.tms_microservice.mapper.SecondaryDetailsMapper;
import com.uniq.tms.tms_microservice.model.Privilege;
import com.uniq.tms.tms_microservice.model.Role;
import com.uniq.tms.tms_microservice.repository.LocationRepository;
import com.uniq.tms.tms_microservice.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.repository.RoleRepository;
import com.uniq.tms.tms_microservice.repository.TeamRepository;
import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import com.uniq.tms.tms_microservice.service.UserService;
import com.uniq.tms.tms_microservice.util.CacheKeyUtil;
import com.uniq.tms.tms_microservice.util.EmailUtil;
import com.uniq.tms.tms_microservice.util.PasswordUtil;
import com.uniq.tms.tms_microservice.util.TextUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Validator;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.opencsv.CSVReader;
import static com.uniq.tms.tms_microservice.util.TextUtil.isBlank;

@Service
public class UserServiceImpl implements UserService {

    private final Validator validator;
    private final UserAdapter userAdapter;
    private final TimesheetAdapter timesheetAdapter;
    private final UserEntityMapper userEntityMapper;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final LocationRepository locationRepository;
    private final EmailUtil emailUtil;
    private final UserDtoMapper userDtoMapper;
    private final ObjectMapper objectMapper;
    private final SecondaryDetailsMapper secondaryDetailsMapper;
    private final WorkScheduleAapter workScheduleAdapter;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LocationEntityMapper locationEntityMapper;
    private final CacheLoaderService cacheLoaderService;
    private final ApplicationEventPublisher publisher;
    private final CacheKeyUtil cacheKeyUtil;
    private final TeamRepository teamRepository;

    public UserServiceImpl(Validator validator, UserAdapter userAdapter, TimesheetAdapter timesheetAdapter, UserEntityMapper userEntityMapper, OrganizationRepository organizationRepository, RoleRepository roleRepository, LocationRepository locationRepository, EmailUtil emailUtil, UserDtoMapper userDtoMapper, ObjectMapper objectMapper, SecondaryDetailsMapper secondaryDetailsMapper, RedisTemplate<String, Object> redisTemplate, LocationEntityMapper locationEntityMapper, CacheLoaderService cacheLoaderService, ApplicationEventPublisher publisher, WorkScheduleAapter workScheduleAdapter, CacheKeyUtil cacheKeyUtil, TeamRepository teamRepository) {
        this.validator = validator;
        this.userAdapter = userAdapter;
        this.timesheetAdapter = timesheetAdapter;
        this.userEntityMapper = userEntityMapper;
        this.organizationRepository = organizationRepository;
        this.roleRepository = roleRepository;
        this.locationRepository = locationRepository;
        this.emailUtil = emailUtil;
        this.userDtoMapper = userDtoMapper;
        this.objectMapper = objectMapper;
        this.secondaryDetailsMapper = secondaryDetailsMapper;
        this.workScheduleAdapter = workScheduleAdapter;
        this.redisTemplate = redisTemplate;
        this.locationEntityMapper = locationEntityMapper;
        this.cacheLoaderService = cacheLoaderService;
        this.publisher = publisher;
        this.cacheKeyUtil = cacheKeyUtil;
        this.teamRepository = teamRepository;
    }

    private static final  Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Value("${csv.upload.dir}")
    private String uploadDir;
    @Value("${CACHE_LOCATION}")
    private String location;

    @Override
    public List<Role> getAllRole(Long orgId, String role) {

        if (role != null && role.startsWith("ROLE_")) {
            role = role.substring(5);
        }
        int hierarchyLevel = UserRole.getLevel(role);
        List<Role> roles = userAdapter.getAllRole(orgId, hierarchyLevel)
                .stream()
                .map(userEntityMapper::toMiddleware)
                .toList();
        return roles;
    }

    @Override
    public List<Group> getAllGroup(Long orgId) {
        List<Group> groups = userAdapter.getAllGroup(orgId).stream().map(userEntityMapper::toMiddleware).toList();
        return groups;
    }

    @Override
    public List<Location> getAllLocation(Long orgId) {
        CachedData<Location> cachedData = null;

        try {
            cachedData = (CachedData<Location>) redisTemplate.opsForValue().get(location);
        } catch (Exception redisException) {
            log.warn("Redis not available or cache fetch failed: {}", redisException.getMessage());
        }

        try {
            if (cachedData != null && cachedData.getData() != null) {
                log.info("Cache hit for key: locations Cache called");
                return cachedData.getData().stream()
                        .filter(location -> location.getOrgId() != null && location.getOrgId().equals(orgId))
                        .toList();
            }

            log.info("Cache missing for key: locations, DB Called");

            // Load and cache if missing
            List<Location> locations = cacheLoaderService.loadLocationTable().get();
            log.info("Total locations fetched from DB: {}", locations.size());
            locations.forEach(loc -> log.info("Location ID: {}, Org ID: {}", loc.getLocationId(), loc.getOrgId()));
            return locations.stream()
                    .filter(location -> location.getOrgId() != null && location.getOrgId().equals(orgId))
                    .toList();

        } catch (Exception e) {
            log.error("Error loading data from DB/cache: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public ApiResponse bulkCreateUsers(MultipartFile file, Long orgId, Long userId) {
        log.info("Checking work flow for create bulk user");
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        if (contentType == null || fileName == null) {
            throw new RuntimeException("Invalid file input.");
        }
        if (fileName.endsWith(".csv") || contentType.equals("text/csv") || contentType.equals("application/vnd.ms-excel")) {
            // Process CSV
            try {
                String filePath = saveCsvToLocal(file);
                return processCsvFileFromPath(filePath, orgId, userId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            throw new RuntimeException("Unsupported file type. Only CSV files are supported.");
        }
    }

    public ApiResponse processCsvFileFromPath(String filePath, Long orgId, Long userId) {
        File file = new File(filePath);
        if (!file.exists()) throw new RuntimeException("CSV file not found: " + filePath);

        try (InputStream inputStream = new FileInputStream(file)) {
            return processCsvFile(inputStream, file.getName(), orgId, userId);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read local CSV file: " + e.getMessage(), e);
        }
    }

    public ApiResponse processCsvFile(InputStream inputStream, String originalFileName, Long orgId, Long userId) {

        UserEntity userFromToken = userAdapter.findUserByOrgIdAndUserId(orgId, userId);
        log.info("Starting processing for file: {}", originalFileName);
            long startTime = System.currentTimeMillis();

            List<UserEntity> userEntities = new ArrayList<>();
            List<SecondaryDetailsEntity> secondaryDetailsEntities = new ArrayList<>();
            List<EmailData> emailRequests = new ArrayList<>();
            List<String> successList = new ArrayList<>();
            Map<UserEntity, List<Long>> userGroupMappings = new HashMap<>();
            Map<UserEntity, List<Long>> userLocationMappings = new HashMap<>();

            int uploadedCount = 0, skippedCount = 0;

            List<Map<String, Object>> skippedRows = new ArrayList<>();
            int rowNumber = 1;

        try {
                // Preload data
                Set<String> existingMobiles = userAdapter.getAllMobileNumbers();
                Set<String> existingEmails = userAdapter.getAllEmails();
                Set<String> existingSecMobiles = userAdapter.getAllSecondaryMobile();
                Set<String> existingSecEmails = userAdapter.getAllSecondaryEmail();
                Map<String, Long> roleMap = userAdapter.getRoleNameIdMap();
                Map<String, Long> locationMap = userAdapter.getLocationNameToIdMap();
                Map<String, Long> groupMap = userAdapter.getGroupNameIdMap();
                UserEntity userEntity = new UserEntity();
                // Expected headers
                List<String> expectedHeaders = List.of(
                        "username", "email", "mobilenumber", "rolename", "locationname", "dateofjoining",
                        "secondaryusername", "secondarymobile", "secondaryemail", "relation", "groupname"
                );

                try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
                    String[] headerRow = reader.readNext();
                    if (headerRow == null) throw new RuntimeException("Missing header row");
                    validateFixedHeaders(headerRow, expectedHeaders);

                    String[] row;
                    while ((row = reader.readNext()) != null) {
                        if (row.length < expectedHeaders.size()) {
                            skippedRows.add(Map.of(
                                    "rowNumber", rowNumber,
                                    "data", Arrays.asList(row),
                                    "reason", "Incomplete row"
                            ));
                            skippedCount++;
                            rowNumber++;
                            continue;
                        }

                        String username = row[0].trim();
                        String email = row[1].trim();
                        String mobile = row[2].trim();
                        String roleName = row[3].trim();
                        String locationName = row[4].trim();
                        String doj = row[5].trim();
                        String secName = row[6].trim();
                        String secMobile = row[7].trim();
                        String secEmail = row[8].trim();
                        String relation = row[9].trim();
                        String groupName = row[10].trim();

                        Long roleId = roleMap.get(roleName.toLowerCase());
                        Long locationId = null;
                        List<Long> locationIds = new ArrayList<>();
                        if(!isBlank(locationName))
                        {
                            String[] locationList = locationName.split(",");
                            log.info("locationList" + ":" + locationList);
                            boolean invalidLocationFound = false;
                            for (String location : locationList) {
                                String trimmedLocation = location.trim().toLowerCase();
                                locationId = locationMap.get(trimmedLocation);
                                if (isBlank(locationId)) {
                                    invalidLocationFound = true;
                                    break;
                                }
                                locationIds.add(locationId);
                            }

                            if (invalidLocationFound || locationIds.isEmpty()) {
                                skippedRows.add(Map.of(
                                        "rowNumber", rowNumber,
                                        "data", Arrays.asList(row),
                                        "reason", "Location not found or invalid: " + locationName
                                ));
                                skippedCount++;
                                rowNumber++;
                                continue;
                            }
                        }


                        Long groupId = null;
                        List<Long> groupIds = new ArrayList<>();
                        if(!isBlank(groupName)){
                            String[] groupList = groupName.split(",");
                            log.info("groupList" + ":" + groupList);
                            for(String group : groupList){
                                String trimmedGroup = group.trim().toLowerCase();
                                groupId = groupMap.get(trimmedGroup);
                                if (isBlank(groupId)) {
                                    skippedRows.add(Map.of(
                                            "rowNumber", rowNumber,
                                            "data", Arrays.asList(row),
                                            "reason", "Group not found" + trimmedGroup
                                    ));
                                    continue;
                                }
                                groupIds.add(groupId);
                            }
                        }

                        if (isBlank(email) || isBlank(mobile) || isBlank(roleId)  || isBlank(doj)) {
                            skippedRows.add(Map.of(
                                    "rowNumber", rowNumber,
                                    "data", Arrays.asList(row),
                                    "reason", "Mandatory fields  are missing"
                            ));
                            skippedCount++;
                            rowNumber++;
                            continue;
                        }

                        log.info("Rolename in csv", roleName);
                        String key = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.HAVE_SECONDARY_DETAILS);
                        boolean hasSecondaryDetailsPrivilege = cacheKeyUtil.roleHasPrivilege(roleName, key);

                        // Validate primary email/mobile
                        if (existingEmails.contains(email) || existingMobiles.contains(mobile)) {
                            skippedRows.add(Map.of(
                                    "rowNumber", rowNumber,
                                    "data", Arrays.asList(row),
                                    "reason", "Email or mobile number already exists"
                            ));
                            skippedCount++;
                            continue;
                        }

                        if (hasSecondaryDetailsPrivilege) {
                            // Skip if any of the required secondary fields are missing
                            if (isBlank(secMobile) || isBlank(relation)){
                                skippedRows.add(Map.of(
                                        "rowNumber", rowNumber,
                                        "data", Arrays.asList(row),
                                        "reason", "Secondary fields are missing"
                                ));
                                skippedCount++;
                                rowNumber++;
                                continue;
                            }

                            // Skip if all secondary fields are empty
                            if (isBlank(secMobile) && isBlank(relation)) {
                                skippedRows.add(Map.of(
                                        "rowNumber", rowNumber,
                                        "data", Arrays.asList(row),
                                        "reason", "All Secondary fields are missing"
                                ));
                                skippedCount++;
                                rowNumber++;
                                continue;
                            }

                            // Check secondary mobile/email duplication
                            if (existingMobiles.contains(secMobile) || existingSecMobiles.contains(secMobile)
                                    || existingEmails.contains(secEmail) || existingSecEmails.contains(secEmail)) {
                                skippedRows.add(Map.of(
                                        "rowNumber", rowNumber,
                                        "data", Arrays.asList(row),
                                        "reason", "Secondary email or mobile number already exists"
                                ));
                                skippedCount++;
                                rowNumber++;
                                continue;
                            }

                            // Check if student and secondary mobile are same
                            if (mobile.equals(secMobile)) {
                                skippedRows.add(Map.of(
                                        "rowNumber", rowNumber,
                                        "data", Arrays.asList(row),
                                        "reason", "Same primary and secondary mobile number"
                                ));
                                skippedCount++;
                                rowNumber++;
                                continue;
                            }
                        }

                        // Create and validate user DTO
                        UserDto userDto = new UserDto();
                        userDto.setUserName(username);
                        userDto.setEmail(email);
                        userDto.setMobileNumber(mobile);
                        userDto.setRoleId(roleId);
                        userDto.setIsRegisterUser(false);
                        userDto.setDateOfJoining(parseDate(doj));

                        if (!validator.validate(userDto).isEmpty()) {
                            skippedRows.add(Map.of(
                                    "rowNumber", rowNumber,
                                    "data", Arrays.asList(row),
                                    "reason", "Invalid data"
                            ));
                            skippedCount++;
                            rowNumber++;
                            continue;
                        }

                        // Create and store user
                        String defaultPass = PasswordUtil.generateDefaultPassword();
                        userEntity = createUserEntity(userDto, orgId, defaultPass);
                        userEntities.add(userEntity);
                        successList.add(username);
                        emailRequests.add(new EmailData(email, userDto.getUserName(), defaultPass, userDto.isRegisterUser()));

                        uploadedCount++;

                        existingEmails.add(email);
                        existingMobiles.add(mobile);

                        // Group mapping
                        userGroupMappings.computeIfAbsent(userEntity, k -> new ArrayList<>())
                                .addAll(groupIds);

                        //Location Mapping
                        userLocationMappings.computeIfAbsent(userEntity, k -> new ArrayList<>())
                                .addAll(locationIds);
                        // Save secondary details if student
                        if (hasSecondaryDetailsPrivilege) {
                            String secondaryEmail = TextUtil.trim(secEmail);
                            SecondaryDetailsEntity secDetails = createSecondaryDetails(userEntity, secMobile, secondaryEmail, secName, relation);
                            secondaryDetailsEntities.add(secDetails);
                            existingSecMobiles.add(secMobile);
                            existingSecEmails.add(secondaryEmail);
                        }
                    }
                }

                // Persist users and secondary details
                List<UserEntity> savedUsers = userAdapter.saveAllUsers(userEntities);
                userAdapter.saveAllSecondaryDetails(secondaryDetailsEntities);

                // Persist user-group mappings
                List<UserGroupEntity> groupEntities = userGroupMappings.entrySet().stream()
                        .flatMap(entry -> entry.getValue().stream()
                                .map(groupIds -> {
                                    UserGroupEntity ug = new UserGroupEntity();
                                    ug.setUser(entry.getKey());
                                    ug.setGroup(new GroupEntity(groupIds));
                                    return ug;
                                }))
                        .toList();

                //user-location mapping
                List<UserLocationEntity> userLocationEntities = userLocationMappings.entrySet().stream()
                                .flatMap(entry -> entry.getValue().stream()
                                        .map(locationIds ->{
                                            UserLocationEntity ul = new UserLocationEntity();
                                            ul.setUser(entry.getKey());
                                            ul.setLocation(new LocationEntity(locationIds));
                                            return ul;
                                        }))
                                        .toList();
                log.info("Saving all user groups");
                userAdapter.saveAllUserGroups(groupEntities);
                log.info("Saved all user groups");
                log.info("Save all user locations");
                userAdapter.saveUserLocation(userLocationEntities);
                log.info("Sending emails to all users");
                sendEmailsAsync(emailRequests);
                log.info("Emails sent to all users");

            } catch (Exception e) {
                throw new RuntimeException("Error processing CSV: " + e.getMessage(), e);
            }

        log.info("Uploaded: {}, Skipped: {}, Time: {} ms", uploadedCount, skippedCount, (System.currentTimeMillis() - startTime));

        Map<String, Object> response = new HashMap<>();
        log.info("uploadedUsers: {}", successList);
        log.info("uploadedCount: {}", uploadedCount);
        log.info("skippedCount: {}", skippedCount);
        log.info("skippedRows: {}", skippedRows);

        String message = String.format(" %d Users created. Duplicate/invalid users were skipped.", uploadedCount);
        log.info("useremail: {} , username: {}", userFromToken.getEmail(),userFromToken.getUserName());
        emailUtil.sendSuccessEmail(userFromToken.getEmail(),userFromToken.getUserName(),uploadedCount,skippedCount);
        return new ApiResponse(200, message, response);

    }

    public String saveCsvToLocal(MultipartFile file) throws IOException {
        Path tempDirPath = Paths.get(uploadDir);
        Files.createDirectories(tempDirPath);

        String originalFilename = file.getOriginalFilename();
        String baseName = FilenameUtils.getBaseName(originalFilename);
        String extension = FilenameUtils.getExtension(originalFilename);

        Path destinationPath = tempDirPath.resolve(originalFilename);
        int count = 1;

        while (Files.exists(destinationPath)) {
            String newFileName = baseName + "(" + count + ")." + extension;
            destinationPath = tempDirPath.resolve(newFileName);
            count++;
        }

        // Save file to temp directory
        file.transferTo(destinationPath.toFile());
        // Log for debugging
        System.out.println("File saved at: " + destinationPath.toAbsolutePath());

        return destinationPath.toAbsolutePath().toString();
    }

    private UserEntity createUserEntity(UserDto userDto, Long orgId,String generatedPass) {
        UserEntity entity = userEntityMapper.toEntity(userDtoMapper.toMiddleware(userDto));
        entity.setOrganizationId(orgId);
        entity.setPassword(PasswordUtil.encryptPassword(generatedPass));
        entity.setDefaultPassword(true);
        entity.setActive(true);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    private SecondaryDetailsEntity createSecondaryDetails(UserEntity user, String secMobile, String secEmail, String secUserName, String relation) {
        SecondaryDetailsEntity entity = new SecondaryDetailsEntity();
        entity.setUser(user);
        entity.setMobile(secMobile);
        if (TextUtil.isBlank(secEmail)) {
            entity.setEmail(null);
        } else {
            entity.setEmail(secEmail);
        }
        entity.setUserName(secUserName);
        entity.setRelation(relation);
        return entity;
    }

    // The Methods are using for CreateBulkUser method.. Start<<<<
    private void validateFixedHeaders(String[] actual, List<String> expected) {
        if (actual.length != expected.size())
            throw new RuntimeException("Header count mismatch. Expected: " + expected.size() + " but found: " + actual.length);

        for (int i = 0; i < expected.size(); i++) {
            String expectedKey = normalizeHeader(expected.get(i));
            String actualKey = normalizeHeader(actual[i]);

            if (!expectedKey.equals(actualKey)) {
                throw new RuntimeException("Header mismatch at position " + (i + 1) +
                        ". Expected: '" + expected.get(i) + "' but found: '" + actual[i] + "'");
            }
        }
    }

    private String normalizeHeader(String header) {
        return header.trim().toLowerCase().replaceAll("[ _]", "");
    }
    
    private LocalDate parseDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return LocalDate.parse(dateStr, formatter);
    }

    @Async
    @Transactional
    public void sendEmailsAsync(List<EmailData> emailRequests) {
        int successCount = 0;
        int failureCount = 0;
        List<Map<String, String>> failedEmails = new ArrayList<>();

        for (EmailData emailData : emailRequests) {
            try {
                emailUtil.sendAccountCreationEmail(emailData.getEmail(), emailData.getUserName(), emailData.getGeneratedPass(), emailData.isNewUser());
                successCount++;
            }
            catch (Exception e) {
                log.error("Failed to send email to {}", emailData.getEmail(), e);
                failureCount++;
                failedEmails.add(Map.of(
                        "email", emailData.getEmail(),
                        "userName", emailData.getUserName(),
                        "reason", e.getMessage()
                ));
                log.error("Email failed for user {} : {}", emailData.getEmail(), e.getMessage());
            }
        }
            log.info("Email success: {} ", successCount);
            log.info("Email failure: {} ", failureCount);

            if (!failedEmails.isEmpty()) {
                log.info("Failed emails Details: {}", failedEmails);
            }
    }

    @Override
    @Transactional
    public ApiResponse createUser(UserDto userDto, SecondaryDetailsDto secondaryDetailsDto, Long organizationId) {
        log.info("Checking if the user is student: {}", userDto.getRoleId());
        Optional<RoleEntity> roleName = userAdapter.findRoleById(userDto.getRoleId());
        log.info("Role from DB for creating user: {}", roleName.get().getName());

        String key = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.HAVE_SECONDARY_DETAILS);
        boolean hasSecondaryDetailsPrivilege = cacheKeyUtil.roleHasPrivilege(roleName.get().getName(), key);
        log.info("hasSecondaryDetailsPrivilege: {}", hasSecondaryDetailsPrivilege);
        // Step 1: Privilege-based secondary validation
        if (hasSecondaryDetailsPrivilege) {
         validateSecondaryUser(secondaryDetailsDto);
        }

        // Step 2: Validate primary user (always checked)
        validatePrimaryUser(userDto);

        // Step 3: Create user
        log.info("Creating user: {}", userDto.getUserName());
        User userMiddleware = userDtoMapper.toMiddleware(userDto);
        UserEntity entity = userEntityMapper.toEntity(userMiddleware);
        entity.setOrganizationId(organizationId);

        if (isBlank(userMiddleware.getRoleId())) {
            throw new CommonExceptionHandler.BadRequestException("roleId must not be null");
        }

        RoleEntity role = roleRepository.findById(userMiddleware.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + userMiddleware.getRoleId()));

        entity.setRole(role);
        String defaultPassword = PasswordUtil.generateDefaultPassword();
        String encryptedPassword = PasswordUtil.encryptPassword(defaultPassword);
        entity.setPassword(encryptedPassword);
        entity.setDefaultPassword(true);
        entity.setActive(true);
        entity.setCreatedAt(LocalDateTime.now());

        log.info("Saving user: {}", userMiddleware.getUserName());
        UserEntity savedUserEntity = userAdapter.saveUser(entity);
        List<Long> locationIds = null;
        List<Long> groupIds = null;
        // Step 4: Save secondary details if provided
        if (hasSecondaryDetailsPrivilege) {
            log.info("Saving secondary details: {}", secondaryDetailsDto.getUserName());
            SecondaryDetails secondaryDetails = secondaryDetailsMapper.toMiddleware(secondaryDetailsDto);
            SecondaryDetailsEntity secondaryDetailsEntity = secondaryDetailsMapper.toEntity(secondaryDetails);
            secondaryDetailsEntity.setUser(savedUserEntity);
            secondaryDetailsEntity.setEmail(TextUtil.trim(secondaryDetails.getEmail()));
            userAdapter.saveSecondaryDetails(secondaryDetailsEntity);
            log.info("Saved secondary details: {}", secondaryDetails);
        }

        // Step 5: Add user to given locations
        if (!isBlank(userDto.getLocationId())) {
            log.info("Adding user to location: {}", userDto.getLocationId());
            List<UserLocationEntity> userLocationEntities = new ArrayList<>();
            for (Long locId : userDto.getLocationId()){
                LocationEntity locations = locationRepository.findById(locId)
                        .orElseThrow(() -> new NoSuchElementException("Location not found with ID: " + locId));

                UserLocationEntity userLocation = new UserLocationEntity();
                userLocation.setUser(savedUserEntity);
                userLocation.setLocation(locations);
                userLocationEntities.add(userLocation);
            }
            userAdapter.saveUserLocation(userLocationEntities);
            locationIds = userLocationEntities.stream()
                    .map(userLocation -> userLocation.getLocation().getLocationId())
                    .toList();
        }

        // Step 6: Add user to group if provided
        if (!isBlank(userDto.getGroupId())) {
            log.info("Adding user to group: {}", userDto.getGroupId());
            List<UserGroupEntity> userGroupEntities = new ArrayList<>();
            for (Long grpId : userDto.getGroupId()) {
                GroupEntity groups = teamRepository.findById(grpId)
                        .orElseThrow(() -> new NoSuchElementException("Group not found with ID: " + grpId));

                UserGroupEntity userGroup = new UserGroupEntity();
                userGroup.setUser(savedUserEntity);
                userGroup.setGroup(groups);
                userGroupEntities.add(userGroup);
            }
            for (UserGroupEntity userGroup : userGroupEntities) {
                userAdapter.saveUserGroup(userGroup); // called individually
            }
            groupIds = userGroupEntities.stream()
                    .map(userGroup -> userGroup.getGroup().getGroupId())
                    .toList();
        }

        User finalUser = userEntityMapper.toMiddleware(savedUserEntity);
        finalUser.setLocationId(locationIds);
        finalUser.setGroupId(groupIds);

        // Step 7: Send account creation email
        boolean isNewUser = savedUserEntity.isDefaultPassword();
        emailUtil.sendAccountCreationEmail(
                userMiddleware.getEmail(), userMiddleware.getUserName(), defaultPassword, isNewUser
        );

        return new ApiResponse(201, "Successfully saved user", finalUser);
    }

    public boolean validateSecondaryUser(SecondaryDetailsDto dto) {
        if (isBlank(dto.getMobile()) || isBlank(dto.getRelation()) || isBlank(dto.getUserName())) {
            log.error("Mandatory secondary user fields must not be null");
            throw new CommonExceptionHandler.BadRequestException("Mandatory secondary user fields must not be null");
        }
        Optional<SecondaryDetailsEntity> mobileExists = userAdapter.findByMobileByMobile(dto.getMobile());
        if(mobileExists.isPresent()){
            boolean isPrimaryActive = mobileExists.get().getUser().isActive();
            if (!isPrimaryActive){
                throw new CommonExceptionHandler.DuplicateUserException(
                        "Inactive User."
                );
            }
            throw new CommonExceptionHandler.DuplicateUserException(
                    "Secondary User Mobile number already exists."
            );
        }
        Optional<SecondaryDetailsEntity> emailExists = userAdapter.findByEmailByEmail(dto.getEmail());
        if(emailExists.isPresent()){
            boolean isPrimaryActive = emailExists.get().getUser().isActive();
            if (!isPrimaryActive){
                throw new CommonExceptionHandler.DuplicateUserException(
                        "Inactive User."
                );
            }
            throw new CommonExceptionHandler.DuplicateUserException(
                    "Secondary User Email already exists."
            );
        }
        return true;
    }

    private boolean validatePrimaryUser(UserDto userDto) {
        if (isBlank(String.valueOf(userDto))|| isBlank(userDto.getUserName()) || isBlank(userDto.getMobileNumber()) || isBlank(userDto.getEmail())) {
            log.error("Mandatory primary user fields must not be null");
            throw new CommonExceptionHandler.BadRequestException("Mandatory Primary user fields must not be null");
        }
        Optional<UserEntity> mobilExists = userAdapter.findByMobileNumber(userDto.getMobileNumber());
        if(mobilExists.isPresent())
        {
            if(!mobilExists.get().isActive()){
                throw new CommonExceptionHandler.DuplicateUserException(
                        "Inactive Users."
                );
            }
            throw new CommonExceptionHandler.DuplicateUserException(
                    "User with this mobile number already exists."
            );
        }
        Optional<UserEntity> emailExists = userAdapter.findByEmail(userDto.getEmail());
        if (emailExists.isPresent())
        {
            if (!emailExists.get().isActive()){
                throw new CommonExceptionHandler.DuplicateUserException(
                        "Inactive User."
                );
            }
            throw new CommonExceptionHandler.DuplicateUserException(
                    "User with this email already exists."
            );
        }

        return true;
    }

    @Override
    public User updateUser(CreateUserDto updates, Long orgId, Long userId) {

        UserEntity existingUser = userAdapter.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!existingUser.getOrganizationId().equals(orgId)) {
            throw new RuntimeException("Unauthorized");
        }

        List<Long> userLocation = userAdapter.findUserLocationByUserId(userId)
                .stream()
                .map(location -> location.getLocation().getLocationId())
                .toList();

        log.info("User location: {}", userLocation);

        List<Long> userGroup = userAdapter.findUserGroupByUserId(userId)
                .stream()
                .map(group -> group.getGroup().getGroupId())
                .toList();
        log.info("User group: {}", userGroup);

        UserDto userDto = updates.getUser();
        List<Long> location = userDto.getLocationId();
        List<Long> group = userDto.getGroupId();

        if (userDto != null) {

            if (userDto.getRoleId() != null) {
                existingUser.setRole(userAdapter.findRoleById(userDto.getRoleId())
                        .orElseThrow(() -> new RuntimeException("Role not found")));
            }
            if (userDto.getEmail() != null) {
                existingUser.setEmail(userDto.getEmail());
            }
            if (userDto.getUserName()!= null) {
                existingUser.setUserName(userDto.getUserName());
            }
            if (userDto.getMobileNumber() != null) {
                existingUser.setMobileNumber(userDto.getMobileNumber());
            }
            if (userDto.isRegisterUser() ) {
                existingUser.setRegisterUser(userDto.isRegisterUser());
            }
            if(location != null) {
                Set<Long> toDelete = new HashSet<>(userLocation);
                toDelete.removeAll(location);
                Set<Long> toInsert = new HashSet<>(location);
                toInsert.removeAll(userLocation);
                if (toDelete.size() > 0) {
                    userAdapter.deleteUserLocationByUserId(userId, toDelete);
                    log.info("Deleted user location: {}", toDelete);
                }
                if (toInsert.size() > 0) {
                    List<UserLocationEntity> newEntities = toInsert.stream()
                                    .map(locations ->
                                    {
                                        UserLocationEntity userLocationEntity = new UserLocationEntity();
                                        userLocationEntity.setUser(existingUser);
                                        userLocationEntity.setLocation(locationRepository.findById(locations)
                                                .orElseThrow(() -> new RuntimeException("Location not found")));
                                        return userLocationEntity;
                                    })
                                            .toList();
                    userAdapter.updateUserLocationByUserId(newEntities);
                    log.info("Added user location: {}", toInsert);
                }
            }
            if (group != null) {
                Set<Long> toDelete = new HashSet<>(userGroup);
                toDelete.removeAll(group);
                Set<Long> toInsert = new HashSet<>(group);
                toInsert.removeAll(userGroup);
                if (toDelete.size() > 0) {
                    userAdapter.deleteUserGroupByUserId(userId, toDelete);
                    log.info("Deleted user group: {}", toDelete);
                }
                if (toInsert.size() > 0) {
                    List<UserGroupEntity> newEntities = toInsert.stream()
                            .map(groups ->
                            {
                                UserGroupEntity userGroupEntity = new UserGroupEntity();
                                userGroupEntity.setUser(existingUser);
                                userGroupEntity.setGroup(teamRepository.findById(groups)
                                        .orElseThrow(() -> new RuntimeException("Group not found")));
                                return userGroupEntity;
                            })
                            .toList();
                    userAdapter.updateUserGroupByUserId(newEntities);
                }
            }
            if(userDto.getDateOfJoining() != null){
                existingUser.setDateOfJoining(userDto.getDateOfJoining());
            }

            String key = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.HAVE_SECONDARY_DETAILS);
            boolean hasSecondaryDetailsPrivilege = cacheKeyUtil.roleHasPrivilege(existingUser.getRole().getName(), key);
            //If user is edit the table also edit the secondary table...
            if(hasSecondaryDetailsPrivilege){
                SecondaryDetailsDto secondaryDetails = updates.getSecondaryDetails();
                SecondaryDetailsEntity existingSecondaryUser = userAdapter.findSecondaryUserById(userId)
                        .orElseThrow(() -> new RuntimeException("Secondary User not found"));
                System.out.println("Fetched Secondary User Id: " + existingSecondaryUser.getId());
                System.out.println("Fetched Secondary User's User Id: " + existingSecondaryUser.getUser().getUserId());

                if (secondaryDetails != null) {
                    if (secondaryDetails.getUserName() !=null){
                        existingSecondaryUser.setUserName(secondaryDetails.getUserName());
                    }
                    if (secondaryDetails.getMobile() != null) {
                        existingSecondaryUser.setMobile(secondaryDetails.getMobile());
                    }
                    if (secondaryDetails.getEmail() != null) {
                        existingSecondaryUser.setEmail(TextUtil.trim(secondaryDetails.getEmail()));
                    }
                    if (secondaryDetails.getRelation() != null) {
                        existingSecondaryUser.setRelation(secondaryDetails.getRelation());
                    }

                }
                userAdapter.saveSecondaryDetails(existingSecondaryUser);
            }else{System.out.println("User Role Id: "+existingUser.getRole().getRoleId());}
        }

        return userEntityMapper.toMiddleware(userAdapter.updateUser(existingUser));
    }

    private void setField(UserEntity user, String key, Object value) {
        try {
            Field field = UserEntity.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(user, convertValue(field.getType(), value));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Error updating field: " + key, e);
        }
    }

    private Object convertValue(Class<?> fieldType, Object value) {
        if (value == null) {
            return null;
        }
        if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
            return Long.parseLong(value.toString());
        }
        if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
            return Integer.parseInt(value.toString());
        }
        if (fieldType.equals(Double.class)) {
            return Double.parseDouble(value.toString());
        }
        if (fieldType.equals(LocalDate.class)) {
            return LocalDate.parse(value.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        return value;
    }

    @Override
    public List<UserResponseDto> getUsers(Long orgId, String role) {
        return fetchActiveUsers(orgId, role);
    }

    private List<UserResponseDto> fetchActiveUsers(Long orgId, String role) {
        int hierarchyLevel = UserRole.getLevel(role);

        List<UserResponse> users = userAdapter.findByOrganizationId(orgId, hierarchyLevel);
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ("No Users found"));
        }

        List<UserResponseDto> usersDto = users.stream()
                .map(userDtoMapper::toDto)
                .toList();

        Map<Long, UserResponseDto> userMap = new LinkedHashMap<>();
        for (UserResponseDto user : usersDto) {
            userMap.compute(user.getUserId(), (id, existing) -> {
                if (existing == null) {
                    SecondaryDetailsEntity secondaryDetails = userAdapter.findSecondaryUserById(user.getUserId()).orElse(null);
                    SecondaryDetailsDto secDto = secondaryDetailsMapper.toMiddleware(secondaryDetails);
                    user.setSecondaryDetails(secDto);
                    return user;
                } else {
//                    String mergedGroups = existing.getGroupName();
//                    if (!mergedGroups.contains(user.getGroupName())) {
//                        mergedGroups += ", " + user.getGroupName();
//                        existing.setGroupName(mergedGroups);
//                    }
                    List<String> existingGroups = existing.getGroupName();
                    if (!existingGroups.contains(user.getGroupName().get(0))) {
                        existingGroups.add(user.getGroupName().get(0));
                    }
                    List<String> existingLocations = existing.getLocationName();
                    if (!existingLocations.contains(user.getLocationName().get(0))) {
                        existingLocations.add(user.getLocationName().get(0));
                    }
                    return existing;
                }
            });
        }

        return new ArrayList<>(userMap.values());
    }


    @Override
    public User deleteUser(Long orgId, Long userId) {
        UserEntity user = userAdapter.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User ID not found."));

        if (!user.getOrganizationId().equals(orgId)) {
            throw new RuntimeException("Unauthorized");
        }
        userAdapter.deactivateUserById(userId,orgId);
        return userEntityMapper.toMiddleware(user);
    }

    @Override
    public AddGroup createGroup(AddGroup groupMiddleware, Long orgId) {
        if (userAdapter.findByGroup(groupMiddleware.getGroupName(), orgId)) {
            throw new DataIntegrityViolationException("Group '" + groupMiddleware.getGroupName() + "' already exists in this organization");
        }

        GroupEntity entity = userEntityMapper.toEntity(groupMiddleware);

        OrganizationEntity orgEntity = organizationRepository.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with ID: " + orgId));
        entity.setOrganizationEntity(orgEntity);

        if (groupMiddleware.getLocationId() != null) {
            LocationEntity locationEntity = locationRepository.findById(groupMiddleware.getLocationId())
                    .orElseThrow(() -> new EntityNotFoundException("Location not found with ID: " + groupMiddleware.getLocationId()));
            entity.setLocationEntity(locationEntity);
        }

        if (groupMiddleware.getWorkScheduleId() != null) {
            WorkScheduleEntity ws = workScheduleAdapter.findByWorkscheduleId(groupMiddleware.getWorkScheduleId());
            entity.setWorkSchedule(ws);
        } else {
            WorkScheduleEntity defaultWs = workScheduleAdapter.findDefaultActiveSchedule(orgId);
            entity.setWorkSchedule(defaultWs);
        }

        GroupEntity savedEntity = userAdapter.saveGroup(entity);

        for (Long id : groupMiddleware.getSupervisorsId()) {
            UserEntity user = userAdapter.findById(id).orElseThrow(()->new UsernameNotFoundException("User ID " + id + " not found."));

            createUserGroup(new UserGroup(savedEntity.getGroupId(), id, groupMiddleware.getType()),orgId);

        }
        return userEntityMapper.toGroupMiddleware(savedEntity);
    }

    @Override
    public ApiResponse addUserToGroup(AddMember addMemberMiddleware, Long orgId) {
        List<Long> userIds = addMemberMiddleware.getUserId();
        List<String> addedUserNames = new ArrayList<>();
        List<String> alreadyExistsUsers = new ArrayList<>();

        // Validate all users first
        for (Long id : userIds) {
            boolean exists = userAdapter.existsById(id);
            if (!exists) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id);
            }
        }

        // Proceed to add members if all exist
        for (Long id : userIds) {
            List<UserGroupEntity> existing = userAdapter.findByUserIdAndGroupId(id, addMemberMiddleware.getGroupId());
            UserEntity userEntity = userAdapter.findById(id).get(); // safe because already validated

            if (!existing.isEmpty()) {
                alreadyExistsUsers.add(userEntity.getUserName());
                continue;
            }

            createUserGroup(new UserGroup(addMemberMiddleware.getGroupId(), id, addMemberMiddleware.getType()), orgId);
            addedUserNames.add(userEntity.getUserName());
        }

        // Prepare message
        String addedMessage = addedUserNames.isEmpty()
                ?""
                : "Successfully added users: " + String.join(", ", addedUserNames) + ".";

        String existsMessage = alreadyExistsUsers.isEmpty()
                ? ""
                : "These users were already in the group: " + String.join(", ", alreadyExistsUsers) + ".";

        String finalMessage = addedMessage + existsMessage;

        // Return ApiResponse — no need to return data list of users
        return new ApiResponse<>(200, finalMessage, null);
    }

    @Override
    public UserGroup createUserGroup(UserGroup userGroupMiddleware, Long orgId) {
        List<UserGroupEntity> existing = userAdapter.findByUserIdAndGroupId(
                userGroupMiddleware.getUserId(),
                userGroupMiddleware.getGroupId()
        );
        if (!existing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This user is already assigned to this group more than once.");
        }

        UserGroupEntity entity = userEntityMapper.toEntity(userGroupMiddleware);
        UserGroupEntity savedEntity=null;
        savedEntity = userAdapter.saveUserGroup(entity);

        return userEntityMapper.toMiddleware(savedEntity);
    }

    @Transactional
    @Override
    public ApiResponse<?> updateGroupDetails(AddGroupDto addGroupDto, Long groupId, Long orgId) {
        AddGroup addGroup = userDtoMapper.toMiddleware(addGroupDto);

        List<String> conflictMessages = new ArrayList<>();
        List<String> addedSupervisors = new ArrayList<>();

        // Fetch existing group
        GroupEntity existingGroup = userAdapter.findByGroupId(groupId).orElse(null);
        if (existingGroup == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found with ID: " + groupId);
        }

        // Check for duplicate group name in the same org
        boolean groupNameChanged = false;
        if (addGroup.getGroupName() != null && !addGroup.getGroupName().equals(existingGroup.getGroupName())) {
            boolean nameExists = userAdapter.existsGroupNameInOrganization(addGroup.getGroupName(), orgId, groupId);
            if (nameExists) {
                conflictMessages.add("Group name already exists");
            } else {
                existingGroup.setGroupName(addGroup.getGroupName());
                groupNameChanged = true;
            }
        }

        // Update location if provided
        if (addGroup.getLocationId() != null) {
            LocationEntity locationEntity = userAdapter.findLocationById(addGroup.getLocationId());
            if (locationEntity == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found with ID: " + addGroup.getLocationId());
            }
            existingGroup.setLocationEntity(locationEntity);
        }

        // Save updated group details (name/location)
        userAdapter.saveGroup(existingGroup);

        // Remove supervisors not in the new list
        List<Long> existingSupervisorIds = userAdapter.findSupervisorIdsByGroupId(groupId);
        Set<Long> newSupervisorIds = addGroup.getSupervisorsId() != null
                ? new HashSet<>(addGroup.getSupervisorsId())
                : new HashSet<>();

        for (Long existingSupervisorId : existingSupervisorIds) {
            if (!newSupervisorIds.contains(existingSupervisorId)) {
                userAdapter.deleteSupervisorsByGroupId(groupId, existingSupervisorId);
            }
        }

        // Insert new supervisors, checking against existing members
        List<Long> existingMemberIds = userAdapter.findMemberIdsByGroupId(groupId);
        for (Long supervisorId : newSupervisorIds) {
            if (existingMemberIds.contains(supervisorId)) {
                // Fetch username for the supervisor
                UserEntity supervisorUser = userAdapter.findById(supervisorId).orElse(null);
                if (supervisorUser != null) {
                    conflictMessages.add("User " + supervisorUser.getUserName() + " is already a member in this group");
                } else {
                    conflictMessages.add("User ID " + supervisorId + " is already a member in this group");
                }
            } else {
                // Delete if already supervisor (safe clean-up)
                userAdapter.deleteSupervisorsByGroupId(groupId, supervisorId);

                // Add new supervisor entry
                UserGroupEntity supervisorEntry = new UserGroupEntity();
                supervisorEntry.setGroup(existingGroup);
                supervisorEntry.setUser(new UserEntity(supervisorId));
                supervisorEntry.setType("Supervisor");
                userAdapter.saveUserGroup(supervisorEntry);

                // Fetch username for the added supervisor
                UserEntity supervisorUser = userAdapter.findById(supervisorId).orElse(null);
                if (supervisorUser != null) {
                    addedSupervisors.add(supervisorUser.getUserName());
                } else {
                    addedSupervisors.add("UserID: " + supervisorId);
                }
            }
        }

        // Build final message
        String conflictMessage = conflictMessages.isEmpty()
                ? ""
                :  String.join(", ", conflictMessages) + ".";

        String finalMessage = conflictMessage.trim();

        // If conflicts occurred, return only conflict message (if any)
        if (!conflictMessages.isEmpty()) {
            return new ApiResponse<>(HttpStatus.CONFLICT.value(), finalMessage, Collections.emptyList());
        }
        // If no conflicts and no supervisors added, indicate that no changes were made
        return new ApiResponse<>(HttpStatus.OK.value(), "Group updated Successfully.", Collections.emptyList());
    }

    @Override
    public List<UserNameSuggestionDto> searchUsernames(String keywords) {
        if (keywords == null || keywords.trim().length() < 3) {
            throw new RuntimeException("Minimum 3 characters required");
        }
        List<UserNameSuggestionDto> results = userAdapter.searchUserNamesContaining(keywords);
        return results;
    }

    @Override
    public UserProfileResponse getUserProfile(Long orgId, Long userId) {
        // Fetch the user groups (may be empty if user has no group)
        List<UserGroupEntity> userGroups = userAdapter.findUserByOrganizationIdAndUserId(orgId, userId);
        UserEntity user;
        if (userGroups.isEmpty()) {
            // If no group, fetch user manually
            user = userAdapter.findUserByOrgIdAndUserId(orgId, userId);
            if (user == null || !user.isActive()) {
                throw new UsernameNotFoundException("User not found");
            }
        } else {
            // If in groups, get user from the first entry
            user = userGroups.get(0).getUser();
        }

        // Fetch and map user location
        List<UserLocationEntity> userLocation = userAdapter.findUserLocationByUserId(userId);
        List<LocationDto> locationDtos = new ArrayList<>();
        if (userLocation.isEmpty()) {
            throw new NullPointerException("No Location found to a user.");
        }
        for (UserLocationEntity ul : userLocation) {
            if (ul.getLocation() == null) continue;
            locationDtos.add(userDtoMapper.toDto(ul.getLocation()));
        }

        // Map groups if present
        List<UserGroupProfileDto> groupDtos = userGroups.isEmpty()
                ? Collections.emptyList()
                : userGroups.stream()
                .map(userDtoMapper::toGroupsDto)
                .collect(Collectors.toList());

        OrganizationEntity org = userAdapter.findByOrgId(orgId);
        // Construct response
        return new UserProfileResponse(
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getRole().getName(),
                user.getDateOfJoining(),
                locationDtos,
                groupDtos,
                org != null ? org.getOrgName() : null
        );
    }

    public List<GroupResponseDto> getAllGroups(Long orgId, Long userId) throws JsonProcessingException {

        UserEntity currentUser = userAdapter.getUserById(userId);
        String roleName = currentUser.getRole().getName().toUpperCase();

        String canSeeAllGroupskey = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_ALL_GROUPS);
        boolean canSeeAllGroups = cacheKeyUtil.roleHasPrivilege(roleName, canSeeAllGroupskey);
        String canSeeSupervisingGroupsKey = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_SUPERVISING_GROUPS);
        boolean canSeeSupervisingGroups = cacheKeyUtil.roleHasPrivilege(roleName, canSeeSupervisingGroupsKey);

        log.info("canSeeAllGroups={}, canSeeSupervisingGroups={}", canSeeAllGroups, canSeeSupervisingGroups);
        List<Object[]> results = userAdapter.getGroupData(orgId);

        // Maps for collecting union data
        Map<Long, Set<String>> userToGroups = new HashMap<>();
        Map<Long, Set<String>> userToLocations = new HashMap<>();
        Map<Long, List<UserGroupDto>> groupIdToActiveMembers = new HashMap<>();

        for (Object[] row : results) {
            Long groupId = ((Number) row[0]).longValue();
            String groupName = (String) row[1];
            String location = (String) row[2];

            if (row[3] != null) {
                String json = row[3].toString();
                List<UserGroupDto> allMembers = objectMapper.readValue(json, new TypeReference<>() {});

                for (UserGroupDto member : allMembers) {
                    if (Boolean.TRUE.equals(member.getActive())) {
                        Long memberUserId = member.getUserId();

                        // Collect unions
                        userToGroups.computeIfAbsent(memberUserId, k -> new HashSet<>()).add(groupName);
                        userToLocations.computeIfAbsent(memberUserId, k -> new HashSet<>()).add(location);

                        // Assign member to group
                        groupIdToActiveMembers.computeIfAbsent(groupId, k -> new ArrayList<>()).add(member);
                    }
                }
            }
        }

        List<GroupResponseDto> finalList = new ArrayList<>();

        for (Object[] row : results) {
            Long groupId = ((Number) row[0]).longValue();
            String groupName = (String) row[1];
            String location = (String) row[2];

            List<UserGroupDto> members = groupIdToActiveMembers.get(groupId);
            if (members == null) {
                members = new ArrayList<>();
            }

            // If user has CAN_SEE_ALL_GROUPS, allow all groups
            if (canSeeAllGroups) {
                for (UserGroupDto member : members) {
                    member.setGroupName(new ArrayList<>(userToGroups.getOrDefault(member.getUserId(), Set.of())));
                    member.setLocation(new ArrayList<>(userToLocations.getOrDefault(member.getUserId(), Set.of())));
                }
                finalList.add(new GroupResponseDto(groupId, groupName, location, members));
            }
            // If user has CAN_SEE_SUPERVISING_GROUPS, only show groups where user is supervisor
            else if (canSeeSupervisingGroups) {
                boolean isSupervisor = members.stream()
                        .anyMatch(member -> member.getUserId().equals(userId) && MemberType.SUPERVISOR.getValue().equals(member.getType()));
                if (isSupervisor) {
                    for (UserGroupDto member : members) {
                        member.setGroupName(new ArrayList<>(userToGroups.getOrDefault(member.getUserId(), Set.of())));
                        member.setLocation(new ArrayList<>(userToLocations.getOrDefault(member.getUserId(), Set.of())));
                    }
                    finalList.add(new GroupResponseDto(groupId, groupName, location, members));
                }
            }
        }

        if (finalList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No Groups Found");
        }

        return finalList;
    }

    @Override
    public boolean updateUserGroupType(UserGroup userGroup) {
        // Map of valid prefixes to their corresponding roles
        Map<String, String> typeMap = Map.of(
                "m", MemberType.MEMBER.getValue(),
                "s", MemberType.SUPERVISOR.getValue()
        );

        String type = typeMap.entrySet().stream()
                .filter(entry -> userGroup.getType().toLowerCase().startsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid type. Must start with 'm' or 's'."));

        int updatedCount = userAdapter.updateUserGroupType(userGroup.getUserId(),userGroup.getGroupId(),type);
        return updatedCount > 0;
    }

    @Override
    public void deleteMember(Long groupId, Long memberId) {
        userAdapter.deleteMember(groupId, memberId);
    }

    @Override
    public void deleteGroup(Long groupId, Long orgId) {
        boolean exist = teamRepository.existsByGroupIdAndOrganizationEntity_OrganizationId(groupId,orgId);
        if(!exist){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Group not found or access denied");
        }
        userAdapter.deleteByGroupId(groupId);
        userAdapter.deleteGroup(groupId, orgId);
    }

    @Override
    public List<User> getMembers(Long orgId, Long roleId) {
        // if roleId is present, get users by roleId
        if (roleId != null) {
            return userAdapter.getMembers(orgId, roleId).stream()
                    .map(userEntityMapper::toMiddleware).toList();
        } else {
            // Get the hierarchy level of the "Student" role dynamically
            int studentHierarchyLevel = UserRole.STUDENT.getHierarchyLevel();
            // Get all roles above the "Student" role hierarchy level
            List<UserRole> higherRoles = Arrays.stream(UserRole.values())
                    .filter(r -> r.getHierarchyLevel() < studentHierarchyLevel)
                    .toList();
            List<Integer> higherRoleIds = higherRoles.stream()
                    .map(roles-> roles.getHierarchyLevel())
                    .toList();
           return userAdapter.getMembersByRole(orgId, higherRoleIds).stream()
                   .map(userEntityMapper::toMiddleware).toList();
        }
    }

    @Override
    public List<GroupDto> getUserGroups(Long userId, String role, Long orgId) {
        String roleName = role.replace("ROLE_", "");
        if(RoleName.SUPERADMIN.getRoleName().equalsIgnoreCase(roleName)){
            List<GroupDto> Allgroup = userAdapter.getAllgroups(orgId);
            return Allgroup;
        }
        List<GroupDto> group = userAdapter.getUserGroups(userId, orgId);
        return group;
    }

    @Override
    public List<Map<String, Object>> getGroupMembers(Long groupId, Long orgId, LocalDate date, Long userIdFromToken) {

        // Fetch group members, filtering out supervisors and logged-in user
        List<UserGroupEntity> groupEntity = userAdapter.getGroupMembersByGroupId(groupId, orgId);

        List<Long> memberIds = groupEntity.stream()
                .map(ug -> ug.getUser().getUserId())
                .filter(id -> !id.equals(userIdFromToken))  // Exclude logged-in user
                .toList();

        if (memberIds == null || memberIds.isEmpty()) {
            return Collections.emptyList();  // If no members found, return empty
        }
        //Fetch user type
        Map<Long, String> userType = groupEntity.stream()
                .collect(Collectors.toMap(
                        ug -> ug.getUser().getUserId(),
                        UserGroupEntity::getType
                ));

        // Fetch list of users based on memberIds
        List<UserEntity> groupUsers = userAdapter.getUsersByIds(memberIds, orgId);

        // Fetch the latest timesheet logs for the members
        List<TimesheetEntity> latestLogs = timesheetAdapter.getLatestLogsByTimesheetIds(memberIds, orgId, date);
        Map<Long, TimesheetEntity> latestLogsMap = latestLogs.stream()
                .collect(Collectors.toMap(TimesheetEntity::getUserId, Function.identity()));

        // Map user details to return response
        List<Map<String, Object>> userDetailsList = groupUsers.stream()
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", user.getUserId());
                    map.put("name", user.getUserName());
                    map.put("role", user.getRole().getName());
                    map.put("isRegistered", user.isRegisterUser());
                    map.put("type", userType.get(user.getUserId()));
                    TimesheetEntity timesheet = latestLogsMap.get(user.getUserId());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

                    if (timesheet != null) {
                        LocalTime clockIn = timesheet.getFirstClockIn();
                        LocalTime clockOut = timesheet.getLastClockOut();

                        map.put("firstClockIn", clockIn != null ? clockIn.format(formatter) : null);
                        map.put("lastClockOut", clockOut != null ? clockOut.format(formatter) : null);
                    } else {
                        map.put("firstClockIn", null);
                        map.put("lastClockOut", null);
                    }
                    return map;
                })
                .toList();

        return userDetailsList;
    }

    @Override
    public List<UserNameSuggestionDto> getGroupUsers(List<Long> groupIds, Long orgId, Long loggedInUserId, String role) {
        // Case 1: groupIds is null or empty → return all active users
        if (groupIds == null || groupIds.isEmpty()) {
            int hierarchyLevel = UserRole.getLevel(role);
            List<UserNameSuggestionDto> allUsers = userAdapter.getAllActiveUsers(orgId,hierarchyLevel);

            return allUsers.stream()
                    .filter(user -> !user.getUserId().equals(loggedInUserId))  // Exclude logged-in user
                    .map(userDto -> new UserNameSuggestionDto(userDto.getUserId(), userDto.getUserName()))  // Map to UserNameSuggestionDto
                    .collect(Collectors.toList());
        }
        log.info("Role: {}", role);
        if (RoleName.SUPERADMIN.getRoleName().equalsIgnoreCase(role)) {
            log.info("SuperAdmin role");
            List<UserNameSuggestionDto> allUsers = userAdapter.getAllGroupUsers(groupIds,orgId);
            List<UserNameSuggestionDto> groupMembers = allUsers.stream()
                    .collect(Collectors.toMap(
                            UserNameSuggestionDto::getUserId,
                            Function.identity(),
                            (existing, replacement) -> existing
                    ))
                    .values()
                    .stream()
                    .toList();
            return groupMembers;
        }
        log.info("Role not used" + role);
        // Case 2: groupIds provided → return only members from those groups
        List<UserGroupEntity> groupEntity = userAdapter.getGroupUsersByGroupId(groupIds, orgId);

        List<Long> memberIds = groupEntity.stream()
                .filter(ug -> ug.getType().equalsIgnoreCase("Member"))  // Only members
                .map(ug -> ug.getUser().getUserId())
                .filter(id -> !id.equals(loggedInUserId))  // Exclude logged-in user
                .distinct()
                .collect(Collectors.toList());

        if (memberIds.isEmpty()) {
            return Collections.emptyList();  // No valid members
        }

        List<UserEntity> groupUsers = userAdapter.getUsersByIds(memberIds, orgId);
        return groupUsers.stream()
                .map(userEntity -> new UserNameSuggestionDto(userEntity.getUserId(), userEntity.getUserName()))  // Map to UserNameSuggestionDto
                .collect(Collectors.toList());
    }

    @Override
    public Location addLocation(LocationDto locationDto, Long orgId) {
        // Convert DTO to Entity
        Location locationModel = locationEntityMapper.toModel(locationDto);
        locationModel.setOrgId(orgId);

        if (userAdapter.findByLocation(locationModel.getName(), orgId)) {
            throw new DataIntegrityViolationException("Location '" + locationModel.getName() + "' already exists in this organization");
        }

        // Save to DB — triggers @PostPersist in listener
        LocationEntity savedEntity = userAdapter.addLocation(locationModel);

        log.info("Publishing LocationCacheReloadEvent");
        publisher.publishEvent(new LocationCacheReloadEvent());
        log.info("LocationCacheReloadEvent published");
        return locationEntityMapper.toDto(savedEntity);
    }

    @Override
    public List<LocationDto> getUserLocation(Long userId) {

        UserEntity existingUser = userAdapter.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserLocationEntity> userLocation = userAdapter.findUserLocationByUserId(userId);

        List<Long> locationIds = userLocation.stream()
                .map(userLocationEntity -> userLocationEntity.getLocation().getLocationId())
                .distinct()
                .toList();

        List<LocationEntity> locations = userAdapter.findAllLocationById(locationIds);

        return locations.stream()
                .map(locationEntityMapper::tolocationDto)
                .toList();
    }

    @Override
    public ResponseEntity<Resource> downloadSampleFile() {
        try {
            Resource resource =new ClassPathResource("templates/Sample_Template.csv");

            if (! resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Sample_Template.csv");
            return ResponseEntity.ok().headers(headers).contentLength(resource.contentLength())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }catch (IOException e){
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @Override
    public Privilege addPrivileges(Privilege privilegeModel) {
        PrivilegeEntity privilegeEntity = userEntityMapper.toEntity(privilegeModel);
        PrivilegeEntity privilege = userAdapter.addPrivilege(privilegeEntity);
        log.info("Publishing PrivilegeCacheReloadEvent");
        publisher.publishEvent(new PrivilegeCacheReloadEvent());
        log.info("PrivilegeCacheReloadEvent published");
        return userEntityMapper.toModel(privilege);
    }

    @Override
    public RolePrivilege addRolwisePrivileges(RolePrivilege rolePrivilege) {

        //Find Role
        RoleEntity role = userAdapter.findRoleById(rolePrivilege.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        //Find Privileges
        PrivilegeEntity privilegeEntity = userAdapter.findPrivilegeById(rolePrivilege.getPrivilegeId())
                .orElseThrow(() -> new RuntimeException("Privilege not found"));

        if(rolePrivilege.isType() == true) {
            role.getPrivilegeEntities().add(privilegeEntity);
        }else{
            role.getPrivilegeEntities().remove(privilegeEntity);
        }
        userAdapter.saveRole(role);

        log.info("Publishing RolePrivilegesCacheReloadEvent");
        publisher.publishEvent(new RolePrivilegesCacheReloadEvent());
        log.info("RolePrivilegesCacheReloadEvent published");

        return rolePrivilege;
    }
}
