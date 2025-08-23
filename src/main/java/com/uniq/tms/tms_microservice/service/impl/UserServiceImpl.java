package com.uniq.tms.tms_microservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uniq.tms.tms_microservice.adapter.TimesheetAdapter;
import com.uniq.tms.tms_microservice.adapter.UserAdapter;
import com.uniq.tms.tms_microservice.adapter.WorkScheduleAdapter;
import com.uniq.tms.tms_microservice.config.security.cache.CacheKeyConfig;
import com.uniq.tms.tms_microservice.config.security.cache.CacheReloadHandlerRegistry;
import com.uniq.tms.tms_microservice.config.security.schema.TenantContext;
import com.uniq.tms.tms_microservice.dto.AddGroupDto;
import com.uniq.tms.tms_microservice.dto.GroupDto;
import com.uniq.tms.tms_microservice.dto.GroupResponseDto;
import com.uniq.tms.tms_microservice.dto.UserRole;
import com.uniq.tms.tms_microservice.entity.GroupEntity;
import com.uniq.tms.tms_microservice.entity.LocationEntity;
import com.uniq.tms.tms_microservice.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.entity.TimesheetEntity;
import com.uniq.tms.tms_microservice.entity.UserEntity;
import com.uniq.tms.tms_microservice.entity.UserGroupEntity;
import com.uniq.tms.tms_microservice.entity.WorkScheduleEntity;
import com.uniq.tms.tms_microservice.enums.OrganizationStatusEnum;
import com.uniq.tms.tms_microservice.exception.CommonExceptionHandler;
import com.uniq.tms.tms_microservice.helper.EmailHelper;
import com.uniq.tms.tms_microservice.helper.RolePrivilegeHelper;
import com.uniq.tms.tms_microservice.mapper.LocationEntityMapper;
import com.uniq.tms.tms_microservice.mapper.UserDtoMapper;
import com.uniq.tms.tms_microservice.mapper.UserEntityMapper;
import com.uniq.tms.tms_microservice.model.*;
import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.entity.*;
import com.uniq.tms.tms_microservice.mapper.SecondaryDetailsMapper;
import com.uniq.tms.tms_microservice.model.Privilege;
import com.uniq.tms.tms_microservice.model.Role;
import com.uniq.tms.tms_microservice.repository.*;
import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import com.uniq.tms.tms_microservice.service.IdGenerationService;
import com.uniq.tms.tms_microservice.service.UserService;
import com.uniq.tms.tms_microservice.util.*;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.concurrent.ExecutionException;
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
    private final EmailHelper emailHelper;
    private final UserDtoMapper userDtoMapper;
    private final SecondaryDetailsMapper secondaryDetailsMapper;
    private final WorkScheduleAdapter workScheduleAdapter;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LocationEntityMapper locationEntityMapper;
    private final CacheLoaderService cacheLoaderService;
    private final ApplicationEventPublisher publisher;
    private final CacheKeyUtil cacheKeyUtil;
    private final TeamRepository teamRepository;
    private final CacheKeyConfig cacheKeyConfig;
    private final CacheReloadHandlerRegistry cacheReloadHandlerRegistry;
    private final OrganizationTypeRepository organizationTypeRepository;
    private final IdGenerationService idGenerationService;
    private final SecondaryDetailsRepository secondaryDetailsRepository;
    private final UserFaceRepository userFaceRepository;
    private final RolePrivilegeHelper rolePrivilegeHelper;

    public UserServiceImpl(Validator validator, UserAdapter userAdapter, TimesheetAdapter timesheetAdapter,
                           UserEntityMapper userEntityMapper, OrganizationRepository organizationRepository,
                           RoleRepository roleRepository, LocationRepository locationRepository, EmailHelper emailHelper,
                           UserDtoMapper userDtoMapper, SecondaryDetailsMapper secondaryDetailsMapper,
                           @Nullable RedisTemplate<String, Object> redisTemplate, LocationEntityMapper locationEntityMapper,
                           CacheLoaderService cacheLoaderService, ApplicationEventPublisher publisher, WorkScheduleAdapter workScheduleAdapter,
                           CacheKeyUtil cacheKeyUtil, TeamRepository teamRepository, CacheKeyConfig cacheKeyConfig, CacheReloadHandlerRegistry cacheReloadHandlerRegistry,
                           OrganizationTypeRepository organizationTypeRepository, IdGenerationService idGenerationService, SecondaryDetailsRepository secondaryDetailsRepository, UserFaceRepository userFaceRepository, RolePrivilegeHelper rolePrivilegeHelper) {
        this.validator = validator;
        this.userAdapter = userAdapter;
        this.timesheetAdapter = timesheetAdapter;
        this.userEntityMapper = userEntityMapper;
        this.organizationRepository = organizationRepository;
        this.roleRepository = roleRepository;
        this.locationRepository = locationRepository;
        this.emailHelper = emailHelper;
        this.userDtoMapper = userDtoMapper;
        this.secondaryDetailsMapper = secondaryDetailsMapper;
        this.workScheduleAdapter = workScheduleAdapter;
        this.redisTemplate = redisTemplate;
        this.locationEntityMapper = locationEntityMapper;
        this.cacheLoaderService = cacheLoaderService;
        this.publisher = publisher;
        this.cacheKeyUtil = cacheKeyUtil;
        this.teamRepository = teamRepository;
        this.cacheKeyConfig = cacheKeyConfig;
        this.cacheReloadHandlerRegistry = cacheReloadHandlerRegistry;
        this.organizationTypeRepository = organizationTypeRepository;
        this.idGenerationService = idGenerationService;
        this.secondaryDetailsRepository = secondaryDetailsRepository;
        this.userFaceRepository = userFaceRepository;
        this.rolePrivilegeHelper = rolePrivilegeHelper;
    }

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Value("${csv.upload.dir}")
    private String uploadDir;

    @Value("${cache.redis.enabled:true}")
    private boolean isRedisEnabled;

    @Value("${basic.plan.max.users}")
    private int subscribedUsers;

    @Override
    public List<Role> getAllRole(String orgId, String role) {

        if (role != null && role.startsWith("ROLE_")) {
            role = role.substring(5);
        }
        int  hierarchyLevel = UserRole.getLevel(role);
        // Step 1: Get orgType from orgId
        String orgTypeId = organizationRepository.findOrgTypeByOrganizationId(orgId);
        log.info("orgtype:{}",orgTypeId);
        OrganizationTypeEntity orgType = organizationTypeRepository.findById(orgTypeId).orElseThrow(() ->
                new RuntimeException("Org Type not found for id: " + orgTypeId));
        log.info("Org type:{}", orgType);
        // Step 2: Get roles above current hierarchy level
        List<RoleEntity> roleEntities = userAdapter.getAllRole(hierarchyLevel);

        // Step 3: Filter STUDENT if org type is not ACADEMIC
        if (!"ACADEMIC".equalsIgnoreCase(orgType.getOrgTypeName())) {
            roleEntities = roleEntities.stream()
                    .filter(r -> !RoleName.STUDENT.getRoleName().equalsIgnoreCase(r.getName()))
                    .toList();
        }

        // Step 4: Map to middleware
        return roleEntities.stream()
                .map(userEntityMapper::toMiddleware)
                .toList();
    }


    @Override
    public List<Group> getAllGroup(String orgId) {
        List<Group> groups = userAdapter.getAllGroup(orgId).stream().map(userEntityMapper::toMiddleware).toList();
        return groups;
    }

    @Override
    public List<Location> getAllLocation(String orgId) {
        String schema = TenantContext.getCurrentTenant();
        String redisKey = cacheKeyUtil.getLocationKey(orgId,schema);
        CachedData<Location> cachedData = null;

        // Only try Redis if redisTemplate is not null
        if (redisTemplate != null) {
            try {
                cachedData = (CachedData<Location>) redisTemplate.opsForValue().get(redisKey);
            } catch (Exception redisException) {
                log.warn("Redis not available or cache fetch failed: {}", redisException.getMessage());
            }
        } else {
            log.warn("RedisTemplate is null, skipping cache fetch for key: {}", redisKey);
        }

        try {
            if (cachedData != null && cachedData.getData() != null) {
                log.info("Cache hit for key orgId: {} locations Cache called", orgId);
                return cachedData.getData();
            }

            log.info("Cache missing for key: locations, DB Called");

            // Load and cache if missing
            List<Location> locations = cacheLoaderService.loadLocationTable(orgId,schema).get();
            log.info("Total locations fetched from DB: {}", locations.size());
            locations.forEach(loc -> log.info("Location ID: {}, Org ID: {}", loc.getLocationId(), loc.getOrgId()));
            return locations;

        } catch (Exception e) {
            log.error("Error loading data from DB/cache: {}", e.getMessage(), e);
            return List.of();
        }
    }


    @Override
    public ApiResponse bulkCreateUsers(MultipartFile file, String orgId, String userId) {
        log.info("Checking work flow for create user");
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        if (contentType == null || fileName == null) {
            throw new RuntimeException("Invalid file input.");
        }
        if (fileName.endsWith(".csv") || contentType.equals("text/csv") || contentType.equals("application/vnd.ms-excel")) {
            try {
                String filePath = saveCsvToLocal(file);
                return processCsvFileFromPath(filePath, orgId, userId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Unsupported file type. Only CSV files are supported.");
        }
    }

    public ApiResponse processCsvFileFromPath(String filePath, String orgId, String userId) {
        File file = new File(filePath);
        if (!file.exists()) throw new RuntimeException("CSV file not found: " + filePath);

        try (InputStream inputStream = new FileInputStream(file)) {
            return processCsvFile(inputStream, file.getName(), orgId, userId);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read local CSV file: " + e.getMessage(), e);
        }
    }

    public ApiResponse processCsvFile(InputStream inputStream, String originalFileName, String orgId, String userId) {

        String schema = TenantUtil.getCurrentTenant();
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
            log.info("Fetching existing mobile numbers for orgId: {}", orgId);
            Set<String> existingMobiles = userAdapter.getAllMobileNumbers(orgId);
            log.info("Fetched {} existing mobile numbers: {}", existingMobiles.size(), existingMobiles);

            log.info("Fetching existing email addresses for orgId: {}", orgId);
            Set<String> existingEmails = userAdapter.getAllEmails(orgId);
            log.info("Fetched {} existing emails: {}", existingEmails.size(), existingEmails);

            log.info("Fetching existing secondary mobile numbers for orgId: {}", orgId);
            Set<String> existingSecMobiles = userAdapter.getAllSecondaryMobile(orgId);
            log.info("Fetched {} secondary mobile numbers: {}", existingSecMobiles.size(), existingSecMobiles);

            log.info("Fetching existing secondary emails for orgId: {}", orgId);
            Set<String> existingSecEmails = userAdapter.getAllSecondaryEmail(orgId);
            log.info("Fetched {} secondary emails: {}", existingSecEmails.size(), existingSecEmails);

            log.info("Fetching role name to ID map for orgId: {}", orgId);
            Map<String, Long> roleMap = userAdapter.getRoleNameIdMap();
            log.info("Fetched {} role entries: {}", roleMap.size(), roleMap);

            log.info("Fetching location name to ID map for orgId: {}", orgId);
            Map<String, Long> locationMap = userAdapter.getLocationNameToIdMap(orgId);
            log.info("Fetched {} location entries: {}", locationMap.size(), locationMap);

            log.info("Fetching group name to ID map for orgId: {}", orgId);
            Map<String, Long> groupMap = userAdapter.getGroupNameIdMap(orgId);
            log.info("Fetched {} group entries: {}", groupMap.size(), groupMap);

            log.info("Fetching work schedule map for orgId: {}", orgId);
            Map<String, String> workScheduleMap = workScheduleAdapter.getAllSchedules(orgId);
            log.info("Fetched {} work schedules: {}", workScheduleMap.size(), workScheduleMap);

            UserEntity userEntity = new UserEntity();
            List<String> expectedHeaders = List.of(
                    "username", "email", "mobilenumber", "rolename", "locationname", "dateofjoining",
                    "secondaryusername", "secondarymobile", "secondaryemail", "relation", "groupname", "workschedule"
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
                    String workSchedule = row[11].trim();

                    Long roleId = roleMap.get(roleName.toLowerCase());
                    log.info("RL ID:{}", roleId);
                    String scheduleId = workScheduleMap.get(workSchedule.toLowerCase());
                    log.info("ES ID:{}", scheduleId);
                    Long locationId = null;
                    List<Long> locationIds = new ArrayList<>();
                    if (!isBlank(locationName)) {
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
                    if (!isBlank(groupName)) {
                        String[] groupList = groupName.split(",");
                        log.info("groupList" + ":" + groupList);
                        for (String group : groupList) {
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

                    if (!isBlank(workSchedule)) {
                        String workScheduleId = workScheduleMap.get(workSchedule.trim());
                        if (isBlank(workScheduleId)) {
                            skippedRows.add(Map.of(
                                    "rowNumber", rowNumber,
                                    "data", Arrays.asList(row),
                                    "reason", "WorkSchedule not found" + workScheduleId
                            ));
                        }
                    }

                    if (isBlank(email) || isBlank(mobile) || isBlank(roleId) || isBlank(doj) || isBlank(workSchedule)) {
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
                    boolean hasSecondaryDetailsPrivilege = rolePrivilegeHelper.roleHasPrivilege(roleName, key);

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
                        if (isBlank(secMobile) || isBlank(relation)) {
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
                    WorkScheduleEntity workScheduleEntity = workScheduleAdapter.findByScheduleId(scheduleId, orgId);
                    // Create and validate user DTO
                    UserDto userDto = new UserDto();
                    userDto.setUserName(username);
                    userDto.setEmail(email);
                    userDto.setMobileNumber(mobile);
                    userDto.setRoleId(roleId);
                    userDto.setWorkSchedule(workScheduleEntity.getScheduleId());
                    userDto.setIsRegisterUser(false);
                    userDto.setDateOfJoining(parseDate(doj));
                    userDto.setUserId(idGenerationService.generateNextUserId(orgId));

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
            List<SecondaryDetailsEntity> savedSecondaryDetails = userAdapter.saveAllSecondaryDetails(secondaryDetailsEntities);

            if(savedUsers != null) {
                log.info("Creating user mapping for all saved users in bulk upload");
                List<UserSchemaMappingEntity> mappings = savedUsers.stream()
                        .map(u -> userEntityMapper.toSchema(
                                u.getEmail(),
                                u.getMobileNumber(),
                                orgId,
                                TenantContext.getCurrentTenant()))
                        .toList();

                userAdapter.saveAllMappings(mappings);
            }
            if(savedSecondaryDetails != null) {
                log.info("Creating user mapping for all saved secondary users in bulk upload");
                List<UserSchemaMappingEntity> secondaryMappings = savedSecondaryDetails.stream()
                        .map(su -> userEntityMapper.toSchema(
                                su.getEmail(),
                                su.getMobile(),
                                orgId,
                                TenantContext.getCurrentTenant()
                        ))
                        .toList();

                userAdapter.saveAllSecondaryMappings(secondaryMappings);
            }
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
                            .map(locationIds -> {
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
        log.info("useremail: {} , username: {}", userFromToken.getEmail(), userFromToken.getUserName());
        emailHelper.sendSuccessEmail(userFromToken.getEmail(), userFromToken.getUserName(), uploadedCount, skippedCount);
        if (isRedisEnabled) {
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getUsers(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("UserCacheReloadEvent published after User bulk creation");
        }else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache Bulk reload.");
        }
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
        file.transferTo(destinationPath.toFile());
        System.out.println("File saved at: " + destinationPath.toAbsolutePath());

        return destinationPath.toAbsolutePath().toString();
    }

    private UserEntity createUserEntity(UserDto userDto, String orgId, String generatedPass) {
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
        entity.setId(idGenerationService.generateNextSecondaryUserId(user.getOrganizationId()));
        entity.setUser(user);
        entity.setMobile(secMobile);
        if (isBlank(secEmail)) {
            entity.setEmail(null);
        } else {
            entity.setEmail(secEmail);
        }
        entity.setUserName(secUserName);
        entity.setRelation(relation);
        return entity;
    }

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
                emailHelper.sendAccountCreationEmail(emailData.getEmail(), emailData.getUserName(), emailData.getGeneratedPass(), emailData.isNewUser());
                successCount++;
            } catch (Exception e) {
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
    public ApiResponse createUser(UserDto userDto, SecondaryDetailsDto secondaryDetailsDto, String organizationId) {
        String schema = TenantUtil.getCurrentTenant();
        log.info("Checking if the user is student: {}", userDto.getRoleId());
        Optional<RoleEntity> roleName = userAdapter.findRoleById(userDto.getRoleId());
        log.info("Role from DB for creating user: {}", roleName.get().getName());
        String key = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.HAVE_SECONDARY_DETAILS);
        boolean hasSecondaryDetailsPrivilege = rolePrivilegeHelper.roleHasPrivilege(roleName.get().getName(), key);
        log.info("hasSecondaryDetailsPrivilege: {}", hasSecondaryDetailsPrivilege);
        if (hasSecondaryDetailsPrivilege) {
            validateSecondaryUser(secondaryDetailsDto);
        }
        validatePrimaryUser(userDto);

        log.info("Creating user: {}", userDto.getUserName());
        User userMiddleware = userDtoMapper.toMiddleware(userDto);
        UserEntity entity = userEntityMapper.toEntity(userMiddleware);
        entity.setOrganizationId(organizationId);
        String customUserId = idGenerationService.generateNextUserId(organizationId);
        entity.setUserId(customUserId);
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
        WorkScheduleEntity scheduleToSet = null;
        if (userMiddleware.getWorkSchedule() == null || userMiddleware.getWorkSchedule().isEmpty()) {
            scheduleToSet = workScheduleAdapter.findDefaultActiveSchedule(organizationId);
            if (scheduleToSet == null) {
                throw new IllegalStateException("No default work schedule found for this organization");
            }
        } else {
            scheduleToSet = workScheduleAdapter.findByScheduleId(userDto.getWorkSchedule(), organizationId);
        }
        entity.setWorkSchedule(scheduleToSet);
        entity.setActive(true);
        entity.setCreatedAt(LocalDateTime.now());
        log.info("Saving user: {}", userMiddleware.getUserName());
        UserEntity savedUserEntity = userAdapter.saveUser(entity);
        List<Long> locationIds = null;
        List<Long> groupIds = null;
        SecondaryDetailsEntity saveSecondaryUser = null;
        if (hasSecondaryDetailsPrivilege) {
            log.info("Saving secondary details: {}", secondaryDetailsDto.getUserName());
            SecondaryDetails secondaryDetails = secondaryDetailsMapper.toMiddleware(secondaryDetailsDto);
            SecondaryDetailsEntity secondaryDetailsEntity = secondaryDetailsMapper.toEntity(secondaryDetails);
            log.info("Call id generation service: {}", organizationId);
            String customSecondaryUserId = idGenerationService.generateNextSecondaryUserId(organizationId);
            secondaryDetailsEntity.setId(customSecondaryUserId);
            secondaryDetailsEntity.setUser(savedUserEntity);
            secondaryDetailsEntity.setEmail(TextUtil.trim(secondaryDetails.getEmail()));
            saveSecondaryUser = userAdapter.saveSecondaryDetails(secondaryDetailsEntity);
            log.info("Saved secondary details: {}", secondaryDetails);
        }

        if(savedUserEntity != null) {
            log.info("Creating user mapping for all saved users");
            UserSchemaMappingEntity mappings = userEntityMapper.toSchema(
                    savedUserEntity.getEmail(),
                    savedUserEntity.getMobileNumber(),
                    organizationId,
                    TenantContext.getCurrentTenant());

            userAdapter.create(mappings);
        }
        if(saveSecondaryUser != null) {
            log.info("Creating user mapping for all saved secondary users");
            UserSchemaMappingEntity secondaryMappings = userEntityMapper.toSchema(
                    null,
                    saveSecondaryUser.getMobile(),
                    organizationId,
                    TenantContext.getCurrentTenant()
            );

            userAdapter.create(secondaryMappings);
        }
        if (!isBlank(userDto.getLocationId())) {
            log.info("Adding user to location: {}", userDto.getLocationId());
            List<UserLocationEntity> userLocationEntities = new ArrayList<>();
            for (Long locId : userDto.getLocationId()) {
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

        boolean isNewUser = savedUserEntity.isDefaultPassword();
        emailHelper.sendAccountCreationEmail(
                userMiddleware.getEmail(), userMiddleware.getUserName(), defaultPassword, isNewUser
        );
        if (isRedisEnabled) {
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getUsers(),
                    organizationId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("UserCacheReloadEvent published after User creation");
        }else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache Create user reload.");
        }
        return new ApiResponse(201, "Successfully saved user", finalUser);
    }

    public boolean validateSecondaryUser(SecondaryDetailsDto dto) {
        if (isBlank(dto.getMobile()) || isBlank(dto.getRelation()) || isBlank(dto.getUserName())) {
            log.error("Mandatory secondary user fields must not be null");
            throw new CommonExceptionHandler.BadRequestException("Mandatory secondary user fields must not be null");
        }
        Optional<SecondaryDetailsEntity> mobileExists = userAdapter.findByMobileByMobile(dto.getMobile());
        if (mobileExists.isPresent()) {
            boolean isPrimaryActive = mobileExists.get().getUser().isActive();
            if (!isPrimaryActive) {
                throw new CommonExceptionHandler.DuplicateUserException(
                        "Inactive User."
                );
            }
            throw new CommonExceptionHandler.DuplicateUserException(
                    "Secondary User Mobile number already exists."
            );
        }
        Optional<SecondaryDetailsEntity> emailExists = userAdapter.findByEmailByEmail(dto.getEmail());
        if (emailExists.isPresent()) {
            boolean isPrimaryActive = emailExists.get().getUser().isActive();
            if (!isPrimaryActive) {
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
        if (isBlank(String.valueOf(userDto)) || isBlank(userDto.getUserName()) || isBlank(userDto.getMobileNumber()) || isBlank(userDto.getEmail())) {
            log.error("Mandatory primary user fields must not be null");
            throw new CommonExceptionHandler.BadRequestException("Mandatory Primary user fields must not be null");
        }
        Optional<UserEntity> mobilExists = userAdapter.findByMobileNumber(userDto.getMobileNumber());
        if (mobilExists.isPresent()) {
            if (!mobilExists.get().isActive()) {
                throw new CommonExceptionHandler.DuplicateUserException(
                        "Inactive Users."
                );
            }
            throw new CommonExceptionHandler.DuplicateUserException(
                    "User with this mobile number already exists."
            );
        }
        Optional<UserEntity> emailExists = userAdapter.findByEmail(userDto.getEmail());
        if (emailExists.isPresent()) {
            if (!emailExists.get().isActive()) {
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
    public User updateUser(CreateUserDto updates, String orgId, String userId) {
        String schema = TenantUtil.getCurrentTenant();
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
            if (userDto.getUserName() != null) {
                existingUser.setUserName(userDto.getUserName());
            }
            if (userDto.getMobileNumber() != null) {
                existingUser.setMobileNumber(userDto.getMobileNumber());
            }
            if (userDto.isRegisterUser()) {
                existingUser.setRegisterUser(userDto.isRegisterUser());
            }
            if (userDto.getWorkSchedule() != null) {
                existingUser.setWorkSchedule(workScheduleAdapter.findByScheduleId(userDto.getWorkSchedule(), orgId));
            }
            if (location != null) {
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
            if (userDto.getDateOfJoining() != null) {
                existingUser.setDateOfJoining(userDto.getDateOfJoining());
            }

            String key = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.HAVE_SECONDARY_DETAILS);
            boolean hasSecondaryDetailsPrivilege = rolePrivilegeHelper.roleHasPrivilege(existingUser.getRole().getName(), key);
            if (hasSecondaryDetailsPrivilege) {
                SecondaryDetailsDto secondaryDetails = updates.getSecondaryDetails();
                SecondaryDetailsEntity existingSecondaryUser = userAdapter.findSecondaryUserById(userId)
                        .orElseThrow(() -> new RuntimeException("Secondary User not found"));
                System.out.println("Fetched Secondary User Id: " + existingSecondaryUser.getId());
                System.out.println("Fetched Secondary User's User Id: " + existingSecondaryUser.getUser().getUserId());

                if (secondaryDetails != null) {
                    if (secondaryDetails.getUserName() != null) {
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
            } else {
                System.out.println("User Role Id: " + existingUser.getRole().getRoleId());
            }
        }
        userAdapter.updateUser(existingUser);
        if (isRedisEnabled) {
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getUsers(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("UserCacheReloadEvent published after User update");
        } else {
                log.info("Redis is not enabled or RedisTemplate is null. Skipping cache Update User reload.");
            }
        return userEntityMapper.toMiddleware(existingUser);
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
    public List<UserResponseDto> getUsers(String orgId, String role) {
        return fetchActiveUsers(orgId, role);
    }

    private List<UserResponseDto> fetchActiveUsers(String orgId, String role) {
        String schema = TenantUtil.getCurrentTenant();
        log.info("Current active user tenant:{}", schema);
        String userCacheKey = cacheKeyUtil.getMemberKey(orgId,schema);
        String roleField = role.toLowerCase();
        log.info("roleField:{}", roleField);
        try {
            if (redisTemplate != null) {
                Object cachedObj = redisTemplate.opsForHash().get(userCacheKey, roleField);
                if (cachedObj != null) {
                    log.info("Cache hit for Active User orgId={}, role={}", orgId, role);
                    return (List<UserResponseDto>) cachedObj;
                }
            } else {
                log.warn("RedisTemplate is null, skipping cache fetch for Active User key: {}, field: {}", userCacheKey, roleField);
            }

            // 2. Cache miss or Redis unavailable - load all into Redis (and also get fresh data)
            log.info("Cache miss for Active user orgId={}, role={}. Loading from DB...", orgId, role);
            Map<String, List<UserResponseDto>> roleMap = cacheLoaderService.loadAllUsers(orgId,schema).get();
            log.info("rolemap:{}", roleMap);
            String loggedRole = roleField.toUpperCase();
            List<UserResponseDto> fallbackList = roleMap.get(loggedRole);
            if (fallbackList != null && !fallbackList.isEmpty()) {
                log.info("Returning response from DB for All Users");
                return fallbackList;
            }

        } catch (Exception e) {
            log.error("Failed to fetch users for orgId={}, role={}. Error: {}", orgId, role, e.getMessage(), e);
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Users found for this organization");
    }

    @Override
    public User deleteUser(String orgId, String userId) {
        String schema = TenantUtil.getCurrentTenant();
        UserEntity user = userAdapter.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User ID not found."));

        if (!user.getOrganizationId().equals(orgId)) {
            throw new RuntimeException("Unauthorized");
        }

        userAdapter.deactivateUserById(userId, orgId);
        log.info("Deactivated user: {} in org: {}", userId, orgId);

        if (isRedisEnabled) {
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getUsers(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("Reloaded cache for active and inactive users for org: {}", orgId);
        } else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache reload.");
        }

        return userEntityMapper.toMiddleware(user);
    }

    @Override
    @Transactional
    public AddGroup createGroup(AddGroup groupMiddleware, String orgId) {
        String schema = TenantUtil.getCurrentTenant();
        if (userAdapter.findByGroup(groupMiddleware.getGroupName(), orgId)) {
            throw new DataIntegrityViolationException("Group '" + groupMiddleware.getGroupName() + "' already exists in this organization");
        }

        GroupEntity entity = userEntityMapper.toEntity(groupMiddleware);

        OrganizationEntity orgEntity = organizationRepository.findByOrganizationId(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with ID: " + orgId));
        entity.setOrganizationEntity(orgEntity);

        if (groupMiddleware.getLocationId() != null) {
            LocationEntity locationEntity = locationRepository.findById(groupMiddleware.getLocationId())
                    .orElseThrow(() -> new EntityNotFoundException("Location not found with ID: " + groupMiddleware.getLocationId()));
            entity.setLocationEntity(locationEntity);
        }

        if (groupMiddleware.getWorkScheduleId() == null || groupMiddleware.getWorkScheduleId().isEmpty()) {
            WorkScheduleEntity defaultWs = workScheduleAdapter.findDefaultActiveSchedule(orgId);
            log.info("Default schedule:{}", defaultWs);
            entity.setWorkSchedule(defaultWs);
        } else {
            WorkScheduleEntity ws = workScheduleAdapter.findByScheduleId(groupMiddleware.getWorkScheduleId(), orgId);
            entity.setWorkSchedule(ws);
        }

        GroupEntity savedEntity = userAdapter.saveGroup(entity);

        for (String id : groupMiddleware.getSupervisorsId()) {
            UserEntity user = userAdapter.findById(id).orElseThrow(() -> new UsernameNotFoundException("User ID " + id + " not found."));

            createUserGroup(new UserGroup(savedEntity.getGroupId(), id, groupMiddleware.getType()), orgId);

        }
        if (isRedisEnabled) {
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getGroups(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("GroupCacheReloadEvent published after Group Creation");
        }else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache create group reload.");
        }
        return userEntityMapper.toGroupMiddleware(savedEntity);
    }

    @Override
    public ApiResponse addUserToGroup(AddMember addMemberMiddleware, String orgId) {
        String schema = TenantUtil.getCurrentTenant();
        List<String> userIds = addMemberMiddleware.getUserId();
        List<String> addedUserNames = new ArrayList<>();
        List<String> alreadyExistsUsers = new ArrayList<>();

        // Validate all users first
        for (String id : userIds) {
            boolean exists = userAdapter.existsById(id);
            if (!exists) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id);
            }
        }

        for (String id : userIds) {
            List<UserGroupEntity> existing = userAdapter.findByUserIdAndGroupId(id, addMemberMiddleware.getGroupId());
            UserEntity userEntity = userAdapter.findById(id).get();

            if (!existing.isEmpty()) {
                alreadyExistsUsers.add(userEntity.getUserName());
                continue;
            }

            createUserGroup(new UserGroup(addMemberMiddleware.getGroupId(), id, addMemberMiddleware.getType()), orgId);
            addedUserNames.add(userEntity.getUserName());
        }

        String addedMessage = addedUserNames.isEmpty()
                ? ""
                : "Successfully added users: " + String.join(", ", addedUserNames) + ".";

        String existsMessage = alreadyExistsUsers.isEmpty()
                ? ""
                : "These users were already in the group: " + String.join(", ", alreadyExistsUsers) + ".";

        String finalMessage = addedMessage + existsMessage;
        if (isRedisEnabled) {
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getGroups(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("GroupCacheReloadEvent published after Adding user to the Group");
        }else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache add User to group reload.");
        }
        return new ApiResponse<>(200, finalMessage, null);
    }

    @Override
    public UserGroup createUserGroup(UserGroup userGroupMiddleware, String orgId) {
        List<UserGroupEntity> existing = userAdapter.findByUserIdAndGroupId(
                userGroupMiddleware.getUserId(),
                userGroupMiddleware.getGroupId()
        );
        if (!existing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This user is already assigned to this group more than once.");
        }

        UserGroupEntity entity = userEntityMapper.toEntity(userGroupMiddleware);
        UserGroupEntity savedEntity = null;
        savedEntity = userAdapter.saveUserGroup(entity);

        return userEntityMapper.toMiddleware(savedEntity);
    }

    @Transactional
    @Override
    public ApiResponse<?> updateGroupDetails(AddGroupDto addGroupDto, Long groupId, String orgId) {
        String schema = TenantUtil.getCurrentTenant();
        AddGroup addGroup = userDtoMapper.toMiddleware(addGroupDto);

        List<String> conflictMessages = new ArrayList<>();
        List<String> addedSupervisors = new ArrayList<>();

        GroupEntity existingGroup = userAdapter.findByGroupId(groupId).orElse(null);
        if (existingGroup == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found with ID: " + groupId);
        }

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

        if (addGroup.getLocationId() != null) {
            LocationEntity locationEntity = userAdapter.findLocationById(addGroup.getLocationId(), orgId);
            if (locationEntity == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found with ID: " + addGroup.getLocationId());
            }
            existingGroup.setLocationEntity(locationEntity);
        }

        if (addGroup.getWorkScheduleId() != null) {
            WorkScheduleEntity workScheduleEntity = workScheduleAdapter.findByScheduleId(addGroup.getWorkScheduleId(), orgId);
            if (workScheduleEntity == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "WorkSchedule Not found with ID: " + addGroup.getWorkScheduleId());
            }
            existingGroup.setWorkSchedule(workScheduleEntity);
        }

        userAdapter.saveGroup(existingGroup);

        List<String> existingSupervisorIds = userAdapter.findSupervisorIdsByGroupId(groupId);
        Set<String> newSupervisorIds = addGroup.getSupervisorsId() != null
                ? new HashSet<>(addGroup.getSupervisorsId())
                : new HashSet<>();

        for (String existingSupervisorId : existingSupervisorIds) {
            if (!newSupervisorIds.contains(existingSupervisorId)) {
                userAdapter.deleteSupervisorsByGroupId(groupId, existingSupervisorId);
            }
        }

        List<String> existingMemberIds = userAdapter.findMemberIdsByGroupId(groupId);
        for (String supervisorId : newSupervisorIds) {
            if (existingMemberIds.contains(supervisorId)) {
                UserEntity supervisorUser = userAdapter.findById(supervisorId).orElse(null);
                if (supervisorUser != null) {
                    conflictMessages.add("User " + supervisorUser.getUserName() + " is already a member in this group");
                } else {
                    conflictMessages.add("User ID " + supervisorId + " is already a member in this group");
                }
            } else {
                userAdapter.deleteSupervisorsByGroupId(groupId, supervisorId);

                UserGroupEntity supervisorEntry = new UserGroupEntity();
                supervisorEntry.setGroup(existingGroup);
                supervisorEntry.setUser(new UserEntity(supervisorId));
                supervisorEntry.setType("Supervisor");
                userAdapter.saveUserGroup(supervisorEntry);

                UserEntity supervisorUser = userAdapter.findById(supervisorId).orElse(null);
                if (supervisorUser != null) {
                    addedSupervisors.add(supervisorUser.getUserName());
                } else {
                    addedSupervisors.add("UserID: " + supervisorId);
                }
            }
        }

        String conflictMessage = conflictMessages.isEmpty()
                ? ""
                : String.join(", ", conflictMessages) + ".";

        String finalMessage = conflictMessage.trim();

        if (!conflictMessages.isEmpty()) {
            return new ApiResponse<>(HttpStatus.CONFLICT.value(), finalMessage, Collections.emptyList());
        }
        if (isRedisEnabled) {
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getGroups(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("GroupCacheReloadEvent published after Group Update");
        }else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache update group reload.");
        }
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
    public UserProfileResponse getUserProfile(String orgId, String userId) {
        String schema = TenantUtil.getCurrentTenant();
        log.info("Current User Profile tenant:{}", schema);
        String redisKey = cacheKeyUtil.getProfileKey(orgId,schema);
        try {
            if (redisTemplate != null) {
                UserProfileResponse cachedData = (UserProfileResponse) redisTemplate.opsForHash().get(redisKey, userId);
                if (cachedData != null) {
                    log.info("Cache hit for userId {} in orgId {}", userId, orgId);
                    return cachedData;
                }else{
                    log.info("Cache miss for userId {}, loading from DB...", userId);
                    Map<String, UserProfileResponse> loadedProfiles = cacheLoaderService.loadUsersProfile(orgId,schema).get();

                    UserProfileResponse response = loadedProfiles.get(userId);
                    if (response != null) {
                        log.info("Fallback DB profile returned for userId {}", userId);
                        return response;
                    } else {
                        log.warn("User profile not found even in DB for userId {}", userId);
                        return null;
                    }
                }
            }
            else {
                log.warn("Redis disabled, directly loading userId {} from DB", userId);
                return loadSingleUserProfile(orgId, userId);
            }

        } catch (Exception e) {
            log.error("Error in user profile fetch logic for userId {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }

    public UserProfileResponse loadSingleUserProfile(String orgId, String userId) {
        UserEntity user = userAdapter.findUserByOrgIdAndUserId(orgId, userId);
        if (user == null) {
            log.warn("User not found for userId {} in orgId {}", userId, orgId);
            return null;
        }

        List<UserLocationEntity> userLocations = userAdapter.findByUser_UserId(userId);
        if (userLocations.isEmpty()) {
            log.warn("Skipping userId {} due to no locations", userId);
            return null;
        }

        List<LocationDto> locationDtos = userLocations.stream()
                .filter(ul -> ul.getLocation() != null)
                .map(ul -> userDtoMapper.toDto(ul.getLocation()))
                .toList();

        List<UserGroupEntity> userGroups = userAdapter.findUserByOrganizationIdAndUserId(orgId, userId);
        List<UserGroupProfileDto> groupDtos = userGroups.isEmpty()
                ? Collections.emptyList()
                : userGroups.stream().map(userDtoMapper::toGroupsDto).toList();

        OrganizationEntity org = organizationRepository.findByOrganizationId(orgId).orElse(null);
        Optional<OrganizationTypeEntity> organizationType =
                (org != null && org.getOrgType() != null)
                        ? organizationTypeRepository.findById(org.getOrgType())
                        : Optional.empty();

        List<ParentDto> parentDtos = Collections.emptyList();
        if (UserRole.STUDENT.name().equalsIgnoreCase(user.getRole().getName())) {
            parentDtos = secondaryDetailsRepository.findByUserId(userId).stream()
                    .map(parentEntity -> new ParentDto(
                            parentEntity.getId(),
                            parentEntity.getUserName(),
                            parentEntity.getEmail(),
                            parentEntity.getMobile()
                    ))
                    .toList();
        }

        return new UserProfileResponse(
                userId,
                user.getUserName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getRole().getName(),
                user.getDateOfJoining(),
                locationDtos,
                groupDtos,
                org != null ? org.getOrgName() : null,
                user.getWorkSchedule() != null ? user.getWorkSchedule().getScheduleName() : "-",
                organizationType.map(OrganizationTypeEntity::getOrgTypeName).orElse("-"),
                parentDtos
        );
    }

    public List<GroupResponseDto> getAllGroups(String orgId, String userId) throws JsonProcessingException {
        String schema = TenantUtil.getCurrentTenant();
        log.info("Current Group tenant:{}", schema);
        String cacheGroupkey = cacheKeyUtil.getAllGroupsKey(orgId,schema);
        String cacheSupervisedGroupKey = cacheKeyUtil.getSupervisedGroupsKey(orgId,schema);

        UserEntity currentUser = userAdapter.getUserById(userId);
        String roleName = currentUser.getRole().getName().toUpperCase();

        String canSeeAllGroupskey = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_ALL_GROUPS);
        boolean canSeeAllGroups = rolePrivilegeHelper.roleHasPrivilege(roleName, canSeeAllGroupskey);
        String canSeeSupervisingGroupsKey = cacheLoaderService.getPrivilegeKey(PrivilegeConstants.CAN_SEE_SUPERVISING_GROUPS);
        boolean canSeeSupervisingGroups = rolePrivilegeHelper.roleHasPrivilege(roleName, canSeeSupervisingGroupsKey);

        log.info("canSeeAllGroups={}, canSeeSupervisingGroups={}", canSeeAllGroups, canSeeSupervisingGroups);
        try {
            if (canSeeAllGroups && redisTemplate != null) {
                Map<Object, Object> allGroups = redisTemplate.opsForHash().entries(cacheGroupkey);
                if (!allGroups.isEmpty()) {
                    log.info("Cache hit for All groups key");
                    return allGroups.values().stream()
                            .map(obj -> (GroupResponseDto) obj)
                            .toList();
                }
                log.warn("Cache miss for all groups. Loading from DB...");
            } else if (canSeeAllGroups) {
                log.warn("RedisTemplate is null, skipping cache fetch for all groups.");
            }

            else if (canSeeSupervisingGroups && redisTemplate != null) {
                Object groupIdSetObj = redisTemplate.opsForHash().get(cacheSupervisedGroupKey, String.valueOf(userId));
                if (groupIdSetObj instanceof Set<?> groupIdSet && !groupIdSet.isEmpty()) {
                    List<GroupResponseDto> result = new ArrayList<>();
                    for (Object gid : groupIdSet) {
                        GroupResponseDto group = (GroupResponseDto) redisTemplate.opsForHash().get(cacheGroupkey, String.valueOf(gid));
                        if (group != null) result.add(group);
                    }
                    if (!result.isEmpty()) {
                        log.info("Fetched {} groups from supervisor cache for userId={}", result.size(), userId);
                        return result;
                    }
                    log.warn("Supervisor group cache empty or group details not found. Loading from DB...");
                }
            } else if (canSeeSupervisingGroups) {
                log.warn("RedisTemplate is null, skipping supervisor cache fetch.");
            }

            log.info("Loading from DB as cache is missing for orgId={}, userId={}", orgId, userId);
            try {
                List<GroupResponseDto> groupsFromDb = cacheLoaderService
                        .loadGroupsCache(orgId,schema)
                        .get();

                if (canSeeAllGroups) {
                    return groupsFromDb;
                } else if (canSeeSupervisingGroups) {
                    return groupsFromDb.stream()
                            .filter(group -> group.getMembersDetails().stream()
                                    .anyMatch(m -> m.getUserId().equals(userId) && "SUPERVISOR".equalsIgnoreCase(m.getType())))
                            .toList();
                } else {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to view any groups");
                }

            } catch (InterruptedException | ExecutionException e) {
                log.error("Failed to load group cache: {}", e.getMessage(), e);
                throw new RuntimeException("Cache loading failed", e);
            }

        } catch (Exception e) {
            log.error("Failed to get groups from cache or DB: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to load groups");
        }
    }

    @Override
    public boolean updateUserGroupType(UserGroup userGroup) {
        Map<String, String> typeMap = Map.of(
                "m", MemberType.MEMBER.getValue(),
                "s", MemberType.SUPERVISOR.getValue()
        );

        String type = typeMap.entrySet().stream()
                .filter(entry -> userGroup.getType().toLowerCase().startsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid type. Must start with 'm' or 's'."));

        int updatedCount = userAdapter.updateUserGroupType(userGroup.getUserId(), userGroup.getGroupId(), type);
        return updatedCount > 0;
    }

    @Override
    public void deleteMember(Long groupId, String memberId, String orgId) {
        String schema = TenantUtil.getCurrentTenant();
        userAdapter.deleteMember(groupId, memberId);
        if (isRedisEnabled) {
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getGroups(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("GroupCacheReloadEvent published after Group Member Deletion");
        }else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache delete member from group reload.");
        }
    }

    @Override
    public void deleteGroup(Long groupId, String orgId) {
        String schema = TenantUtil.getCurrentTenant();
        boolean exist = teamRepository.existsByGroupIdAndOrganizationEntity_OrganizationId(groupId, orgId);
        if (!exist) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Group not found or access denied");
        }
        userAdapter.deleteByGroupId(groupId);
        userAdapter.deleteGroup(groupId, orgId);
        if (isRedisEnabled) {
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getGroups(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("GroupCacheReloadEvent published after Group Deletion");
        }else {
                log.info("Redis is not enabled or RedisTemplate is null. Skipping cache delete group reload.");
            }
    }

    @Override
    public List<User> getMembers(String orgId, Long roleId) {
        if (roleId != null) {
            return userAdapter.getMembers(orgId, roleId).stream()
                    .map(userEntityMapper::toMiddleware).toList();
        } else {
            int studentHierarchyLevel = UserRole.STUDENT.getHierarchyLevel();
            int superadminHierarchyLevel = UserRole.SUPERADMIN.getHierarchyLevel();
            List<UserRole> higherRoles = Arrays.stream(UserRole.values())
                    .filter(r -> r.getHierarchyLevel() < studentHierarchyLevel && r.getHierarchyLevel() > superadminHierarchyLevel)
                    .toList();
            List<Integer> higherRoleIds = higherRoles.stream()
                    .map(roles -> roles.getHierarchyLevel())
                    .toList();
            return userAdapter.getMembersByRole(orgId, higherRoleIds).stream()
                    .map(userEntityMapper::toMiddleware).toList();
        }
    }

    @Override
    public List<GroupDto> getUserGroups(String userId, String role, String orgId) {
        String roleName = role.replace("ROLE_", "");
        if (RoleName.SUPERADMIN.getRoleName().equalsIgnoreCase(roleName)) {
            List<GroupDto> Allgroup = userAdapter.getAllgroups(orgId);
            return Allgroup;
        }
        List<GroupDto> group = userAdapter.getUserGroups(userId, orgId);
        return group;
    }

    @Override
    public List<Map<String, Object>> getGroupMembers(Long groupId, String orgId, LocalDate date, String userIdFromToken) {
        List<UserGroupEntity> groupEntity = userAdapter.getGroupMembersByGroupId(groupId, orgId);

        List<String> memberIds = groupEntity.stream()
                .map(ug -> ug.getUser().getUserId())
                .filter(id -> !id.equals(userIdFromToken))
                .toList();

        if (memberIds == null || memberIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, String> userType = groupEntity.stream()
                .collect(Collectors.toMap(
                        ug -> ug.getUser().getUserId(),
                        UserGroupEntity::getType
                ));

        List<UserEntity> groupUsers = userAdapter.getUsersByIds(memberIds, orgId);

        List<TimesheetEntity> latestLogs = timesheetAdapter.getLatestLogsByTimesheetIds(memberIds, orgId, date);
        Map<String, TimesheetEntity> latestLogsMap = latestLogs.stream()
                .collect(Collectors.toMap(TimesheetEntity::getUserId, Function.identity()));

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
    public List<UserNameSuggestionDto> getGroupUsers(List<Long> groupIds, String orgId, String loggedInUserId, String role) {
        if (groupIds == null || groupIds.isEmpty()) {
            int hierarchyLevel = UserRole.getLevel(role);
            List<UserNameSuggestionDto> allUsers = userAdapter.getAllActiveUsers(orgId, hierarchyLevel);

            return allUsers.stream()
                    .filter(user -> !user.getUserId().equals(loggedInUserId))  // Exclude logged-in user
                    .map(userDto -> new UserNameSuggestionDto(userDto.getUserId(), userDto.getUserName()))  // Map to UserNameSuggestionDto
                    .collect(Collectors.toList());
        }
        log.info("Role: {}", role);
        if (RoleName.SUPERADMIN.getRoleName().equalsIgnoreCase(role)) {
            log.info("SuperAdmin role");
            List<UserNameSuggestionDto> allUsers = userAdapter.getAllGroupUsers(groupIds, orgId);
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
        List<UserGroupEntity> groupEntity = userAdapter.getGroupUsersByGroupId(groupIds, orgId);

        List<String> memberIds = groupEntity.stream()
                .filter(ug -> ug.getType().equalsIgnoreCase("Member"))
                .map(ug -> ug.getUser().getUserId())
                .filter(id -> !id.equals(loggedInUserId))
                .distinct()
                .collect(Collectors.toList());

        if (memberIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<UserEntity> groupUsers = userAdapter.getUsersByIds(memberIds, orgId);
        return groupUsers.stream()
                .map(userEntity -> new UserNameSuggestionDto(userEntity.getUserId(), userEntity.getUserName()))  // Map to UserNameSuggestionDto
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Location addLocation(LocationDto locationDto, String orgId) {
        String schema = TenantUtil.getCurrentTenant();
        Location locationModel = locationEntityMapper.toModel(locationDto);
        try {
            locationModel.setOrgId(orgId);
            if (locationModel.isDefault()) {
                locationRepository.resetDefaultLocation(orgId);
            }
            LocationEntity savedEntity = userAdapter.addLocation(locationModel);
            int roleId = UserRole.SUPERADMIN.getHierarchyLevel();
            List<UserEntity> user = userAdapter.findUserByOrgIdAndRoleId(orgId, roleId);
            if (user == null) {
                throw new RuntimeException("No User found under the role Superadmin for Logged Organization");
            }
            log.info("User List in role Superadmin:{}", user);
            log.info("Adding user to newly created location");
            LocationEntity locations = locationRepository.findById(savedEntity.getLocationId())
                    .orElseThrow(() -> new NoSuchElementException("Location not found with ID: " + savedEntity.getLocationId()));
            List<UserLocationEntity> userLocationEntities = new ArrayList<>();
            for(UserEntity u : user) {
                UserLocationEntity userLocation = new UserLocationEntity();
                userLocation.setUser(u);
                userLocation.setLocation(locations);
                userLocationEntities.add(userLocation);
                log.info("USERLOCATION:{}", userLocation);
            }
            userAdapter.saveUserLocation(userLocationEntities);
            if (isRedisEnabled) {
                CacheEventPublisherUtil.syncReloadThenPublish(
                        publisher,
                        cacheKeyConfig.getLocation(),
                        orgId,
                        schema,
                        cacheReloadHandlerRegistry
                );
                log.info("LocationCacheReloadEvent published after location Added");
            }else {
                log.info("Redis is not enabled or RedisTemplate is null. Skipping cache add location reload.");
            }
            return locationEntityMapper.toDto(savedEntity);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Location '" + locationModel.getName() + "' already exists in this organization");
        }
    }

    @Override
    public List<LocationDto> getUserLocation(String userId) {

        UserEntity existingUser = userAdapter.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserLocationEntity> userLocation = userAdapter.findUserLocationByUserId(userId);

        if (userLocation.isEmpty()) {
            throw new CommonExceptionHandler.NoUserLocationAssignedException(existingUser.getUserName());
        }

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
            Resource resource = new ClassPathResource("templates/Sample_Template.csv");

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Sample_Template.csv");
            return ResponseEntity.ok().headers(headers).contentLength(resource.contentLength())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @Override
    public Privilege addPrivileges(Privilege privilegeModel, String orgId) {
        String schema = TenantUtil.getCurrentTenant();
        PrivilegeEntity privilegeEntity = userEntityMapper.toEntity(privilegeModel);
        PrivilegeEntity privilege = userAdapter.addPrivilege(privilegeEntity);
        if (isRedisEnabled) {
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getRoleprivilege(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("Privilege CacheReloadEvent published after Privilege Creation");
        }else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache add privilege reload.");
        }
        return userEntityMapper.toModel(privilege);
    }

    @Override
    public RolePrivilege addRolwisePrivileges(RolePrivilege rolePrivilege, String orgId) {
        String schema = TenantUtil.getCurrentTenant();
        RoleEntity role = userAdapter.findRoleById(rolePrivilege.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        PrivilegeEntity privilegeEntity = userAdapter.findPrivilegeById(rolePrivilege.getPrivilegeId())
                .orElseThrow(() -> new RuntimeException("Privilege not found"));

        if (rolePrivilege.isType()) {
            boolean exists = role.getPrivilegeMappings().stream()
                    .anyMatch(mapping -> mapping.getPrivilege().equals(privilegeEntity));
            if (!exists) {
                RolePrivilegeMapEntity mapping = new RolePrivilegeMapEntity();
                mapping.setRole(role);
                mapping.setPrivilege(privilegeEntity);
                mapping.setEnabled(true);
                mapping.setCreatedAt(LocalDateTime.now());
                role.getPrivilegeMappings().add(mapping);
            }
        } else {
            role.getPrivilegeMappings().removeIf(mapping -> mapping.getPrivilege().equals(privilegeEntity));
        }

        userAdapter.saveRole(role);

        if (isRedisEnabled) {
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getRoleprivilege(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("RolewisePrivilege CacheReloadEvent published after Role wise privilege creation");
        } else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache add rolewise privilege reload.");
        }
        return rolePrivilege;
    }

    @Override
    public ApiResponse updateLocation(String orgId, LocationList location) {
        String schema = TenantUtil.getCurrentTenant();
        List<Long> locationIds = location.getLocationId();
        if (locationIds == null || locationIds.isEmpty()) {
            throw new RuntimeException("No location IDs provided");
        }
        if (location.isDefault() && location.getLocationId().size() > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only one Location can be marked as Default"
            );
        }
        Long defaultLocationId = location.isDefault() ? locationIds.get(0) : null;
        if (location.isDefault()) {
            locationRepository.resetDefaultLocation(orgId, defaultLocationId);
        }
        List<LocationEntity> updatedEntities = new ArrayList<>();
        int count = 0;
        for (Long id : locationIds) {
            LocationEntity entity = userAdapter.findLocationById(id, orgId);
            if (entity == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No Location Found for the provided Location Id");
            }
            if (location.getName() != null) entity.setName(location.getName());
            if (location.getAddress() != null) entity.setAddress(location.getAddress());
            if (location.getLatitude() != null) entity.setLatitude(location.getLatitude());
            if (location.getLongitude() != null) entity.setLongitude(location.getLongitude());
            if (location.getRadius() != null) entity.setRadius(location.getRadius());
            entity.setDefault(location.isDefault() && id.equals(defaultLocationId));
            OrganizationEntity organization = new OrganizationEntity();
            organization.setOrganizationId(orgId);
            entity.setOrganizationEntity(organization);
            updatedEntities.add(entity);
            count++;
        }

        List<LocationEntity> savedEntities = userAdapter.updateMultipleLocations(updatedEntities);
        if (isRedisEnabled) {
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getLocation(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("LocationCacheReloadEvent published after bulk update");
        }else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache update Location reload.");
        }
        String message = String.format("%d locations updated successfully", count);
        return new ApiResponse(200, message, null);
    }

    @Override
    public void deleteLocation(LocationListDto locationDto, String orgId) {
        String schema = TenantUtil.getCurrentTenant();
        List<Long> locationIdList = locationDto.getLocationId();

        boolean exist = locationRepository.existsBylocationIdInAndOrganizationEntity_OrganizationId(locationIdList, orgId);
        if (!exist) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Location not found or access denied");
        }

        Optional<LocationEntity> defaultLocationExist = userAdapter.findAllDefaultLocationById(locationIdList, orgId);
        if (defaultLocationExist.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete default location");
        }

        LocationEntity defaultLocation = userAdapter.findDefaultLocationByOrgId(orgId);
        if (defaultLocation == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No default location exists for this organization");
        }

        Long defaultLocationId = defaultLocation.getLocationId();

        List<GroupEntity> groupsToUpdate = userAdapter.findByLocation_LocationIdIn(locationIdList);
        for (GroupEntity group : groupsToUpdate) {
            if (!group.getLocationEntity().getLocationId().equals(defaultLocationId)) {
                group.setLocationEntity(defaultLocation);
            }
        }
        userAdapter.saveAllGroups(groupsToUpdate);

        List<UserLocationEntity> userLocationsToUpdate = userAdapter.findUserLocationByLocationId(locationIdList);

        log.info("User-Location entries to process: {}", userLocationsToUpdate.size());
        if (userLocationsToUpdate.isEmpty()) return;

        List<UserLocationEntity> userLocationsToDelete = new ArrayList<>();
        List<UserLocationEntity> newUserLocationsToInsert = new ArrayList<>();

        for (UserLocationEntity userLoc : userLocationsToUpdate) {
            String userId = userLoc.getUser().getUserId();
            Long currentLocationId = userLoc.getLocation().getLocationId();
            log.info("User Current Location: {}", currentLocationId);
            if (currentLocationId.equals(defaultLocationId)) {
                userLocationsToDelete.add(userLoc);
                continue;
            }

            boolean hasDefaultLocation = userAdapter
                    .findUserLocationByUserId(userId)
                    .stream()
                    .map(loc -> loc.getLocation().getLocationId())
                    .anyMatch(id -> id.equals(defaultLocationId));

            if (!hasDefaultLocation) {
                UserLocationEntity newUserLoc = new UserLocationEntity();
                newUserLoc.setUser(userLoc.getUser());
                newUserLoc.setLocation(defaultLocation);
                newUserLocationsToInsert.add(newUserLoc);
            }
            userLocationsToDelete.add(userLoc);
        }

        if (!newUserLocationsToInsert.isEmpty()) {
            userAdapter.saveAllUserLocation(newUserLocationsToInsert);
        }

        userAdapter.deleteAllUserLocations(userLocationsToDelete);

        log.info("Successfully reassigned {} users to default location and deleted {} old mappings.",
                newUserLocationsToInsert.size(), userLocationsToDelete.size());

        userAdapter.deleteLocation(locationIdList, orgId);
        if (isRedisEnabled) {
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getLocation(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("Location deleted and references updated. Cache reloaded.");
        }else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache delete location reload.");
        }
    }

    @Override
    public String findGroupName(Long requestedGroupId) {
        return teamRepository.findGroupNameByGroupId(requestedGroupId);
    }

    @Override
    public List<UserResponseDto> getInactiveUsers(String orgId, String role) {
        String schema = TenantUtil.getCurrentTenant();
        log.info("Current location tenant:{}", schema);
        String userCacheKey = cacheKeyUtil.getInactiveMemberKey(orgId,schema);
        String roleField = role.toLowerCase();
        log.info("roleField:{}", roleField);
        try {
            if (redisTemplate != null) {
                Object cachedObj = redisTemplate.opsForHash().get(userCacheKey, roleField);
                if (cachedObj != null) {
                    log.info("Cache hit for orgId={}, role={}", orgId, role);
                    return (List<UserResponseDto>) cachedObj;
                }
            } else {
                log.warn("RedisTemplate is null, skipping cache fetch for key: {}, field: {}", userCacheKey, roleField);
            }

            log.info("Cache miss for orgId={}, role={}. Loading from DB...", orgId, role);
            Map<String, List<UserResponseDto>> roleMap = cacheLoaderService.loadAllInactiveUsers(orgId,schema).get();
            log.info("rolemap:{}", roleMap);
            String loggedRole = roleField.toUpperCase();
            List<UserResponseDto> fallbackList = roleMap.get(loggedRole);
            if (fallbackList != null && !fallbackList.isEmpty()) {
                log.info("Returning response from DB for All Users");
                return fallbackList;
            }

        } catch (Exception e) {
            log.error("Failed to fetch users for orgId={}, role={}. Error: {}", orgId, role, e.getMessage(), e);
        }

        throw new ResponseStatusException(HttpStatus.OK, "No Inactive Users found for this organization");
    }

    @Override
    public List<EditUserDto> updateIsActive(EditUser editUser, String orgId){
        String schema = TenantUtil.getCurrentTenant();
        List<String> userIds = editUser.getUserId();
        if(userIds == null || userIds.isEmpty()){
            throw new RuntimeException("No userIds provided");
        }
        List<UserEntity> userEntityList = new ArrayList<>();
        for(String id : userIds){
            UserEntity userEntities = userAdapter.findUserByOrgIdAndUserId(orgId, id);
            if (userEntities == null){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No user found for the provided userId");
            }

            userEntities.setActive(editUser.isActive());
            userEntityList.add(userEntities);
        }
        List<UserEntity> user = userAdapter.save(userEntityList);
        userFaceRepository.deleteAllById(editUser.getUserId());
        List<EditUserDto> dto = userDtoMapper.toDto(user);
        if (isRedisEnabled) {
            CacheEventPublisherUtil.syncReloadThenPublish(
                    publisher,
                    cacheKeyConfig.getInactiveUsers(),
                    orgId,
                    schema,
                    cacheReloadHandlerRegistry
            );
            log.info("InactiveUserCacheReloadEvent published after Active update");
        }else {
            log.info("Redis is not enabled or RedisTemplate is null. Skipping cache update Inactive User reload.");
        }
        return dto;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ApiResponse createSuperAdminUser(Organization organization, String organizationId, String schemaName) {
        try {
            log.info("Creating SuperAdmin in schema: {} for OrgId {}", schemaName, organizationId);

            if (isBlank(organization.getUserName()) ||
                    isBlank(organization.getMobile()) ||
                    isBlank(organization.getEmail())) {
                throw new CommonExceptionHandler.BadRequestException("Mandatory fields must not be null");
            }
            log.info("find user by mobile number");
            userAdapter.findByMobileNumber(organization.getMobile()).ifPresent(user -> {
                throw new CommonExceptionHandler.DuplicateUserException("Mobile number already in use.");
            });
            log.info("find user by email");
            userAdapter.findByEmail(organization.getEmail()).ifPresent(user -> {
                throw new CommonExceptionHandler.DuplicateUserException("Email already in use.");
            });
            log.info("find user by role");
            RoleEntity role = roleRepository.findById((long) UserRole.SUPERADMIN.getHierarchyLevel())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            log.info("Role founded");
            UserEntity entity = new UserEntity();
            entity.setUserName(organization.getUserName());
            entity.setEmail(organization.getEmail());
            entity.setMobileNumber(organization.getMobile());
            entity.setOrganizationId(organizationId);

            String customUserId = idGenerationService.generateNextUserId(organizationId);
            entity.setUserId(customUserId);
            entity.setRole(role);
            entity.setDateOfJoining(LocalDate.now());

            String defaultPassword = PasswordUtil.generateDefaultPassword();
            entity.setPassword(PasswordUtil.encryptPassword(defaultPassword));
            entity.setDefaultPassword(true);
            entity.setActive(true);
            entity.setCreatedAt(LocalDateTime.now());

            log.info("Save superadmin");
            UserEntity savedUser = userAdapter.saveUser(entity);

            String planId = userAdapter.findByPlan();
            SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
            subscriptionEntity.setSubId(idGenerationService.generateNextSubscriptionId(organizationId));
            subscriptionEntity.setOrgId(organizationId);
            subscriptionEntity.setPlanId(planId);
            subscriptionEntity.setSchemaName(schemaName);
            subscriptionEntity.setStatus(OrganizationStatusEnum.ACTIVE.getDisplayValue());
            subscriptionEntity.setStartDate(LocalDateTime.now());
            subscriptionEntity.setEndDate(LocalDateTime.now().plusDays(7));
            subscriptionEntity.setSubscribedUsers(subscribedUsers);
            log.info("Save subscription for created Organization");
            SubscriptionEntity saveSubscription = userAdapter.saveSubscription(subscriptionEntity);

            emailHelper.sendAccountCreationEmail(
                    savedUser.getEmail(), savedUser.getUserName(), defaultPassword, true
            );

            log.info("SuperAdmin created successfully in schema.users for org {}", organizationId);
            return new ApiResponse(201, "SuperAdmin created successfully", userEntityMapper.toMiddleware(savedUser));

        } catch (Exception e) {
            log.error("Error creating SuperAdmin user: {}", e.getMessage(), e);
            throw new CommonExceptionHandler.InternalServerException("Failed to create SuperAdmin user. " + e.getMessage());
        }
    }

}
