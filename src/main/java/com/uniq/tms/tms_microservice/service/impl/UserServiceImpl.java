package com.uniq.tms.tms_microservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.exceptions.CsvValidationException;
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
import com.uniq.tms.tms_microservice.mapper.LocationEntityMapper;
import com.uniq.tms.tms_microservice.mapper.RolePrivilegeMapper;
import com.uniq.tms.tms_microservice.mapper.UserDtoMapper;
import com.uniq.tms.tms_microservice.mapper.UserEntityMapper;
import com.uniq.tms.tms_microservice.model.*;
import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.entity.*;
import com.uniq.tms.tms_microservice.mapper.SecondaryDetailsMapper;
import com.uniq.tms.tms_microservice.model.Role;
import com.uniq.tms.tms_microservice.repository.LocationRepository;
import com.uniq.tms.tms_microservice.repository.OrganizationRepository;
import com.uniq.tms.tms_microservice.repository.RoleRepository;
import com.uniq.tms.tms_microservice.service.CacheLoaderService;
import com.uniq.tms.tms_microservice.service.UserService;
import com.uniq.tms.tms_microservice.util.EmailUtil;
import com.uniq.tms.tms_microservice.util.PasswordUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Validator;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.opencsv.CSVReader;

@Service
public class UserServiceImpl implements UserService {
    @Value("${csv.upload.dir}")
    private String uploadDir;
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
    private final Long STUDENT_ROLE_ID = 5l;
    private final WorkScheduleAapter workScheduleAdapter;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LocationEntityMapper locationEntityMapper;
    private final CacheLoaderService cacheLoaderService;
    private final ApplicationEventPublisher publisher;

    public UserServiceImpl(Validator validator, UserAdapter userAdapter, TimesheetAdapter timesheetAdapter, UserEntityMapper userEntityMapper, OrganizationRepository organizationRepository, RoleRepository roleRepository, LocationRepository locationRepository, EmailUtil emailUtil, UserDtoMapper userDtoMapper, ObjectMapper objectMapper, SecondaryDetailsMapper secondaryDetailsMapper, RedisTemplate<String, Object> redisTemplate, LocationEntityMapper locationEntityMapper, CacheLoaderService cacheLoaderService, ApplicationEventPublisher publisher, WorkScheduleAapter workScheduleAdapter) {
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
    }

    private static final  Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

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
    public List<Group> getAllTeam() {
        List<Group> teams = userAdapter.getAllTeams().stream().map(userEntityMapper::toMiddleware).toList();
        return teams;
    }

    @Override
    public List<Location> getAllLocation(Long orgId) {
        CachedData<Location> cachedData = (CachedData<Location>) redisTemplate.opsForValue().get(location);

        try {
            if (cachedData == null || cachedData.getData() == null) {
                log.info("Cache missing for key: locations, DB Called");
                // Load and cache if missing
                cacheLoaderService.loadLocationTable();
                return List.of();
            }

            log.info("Cache hit for key: locations Cache called");
            return cachedData.getData().stream()
                    .filter(location -> location.getOrgId() != null && location.getOrgId().equals(orgId))
                    .toList();

        } catch (Exception e) {
            log.error("Error loading data from cache: {}", e.getMessage(), e);
            return List.of();
        }

    }

    @Override
    public ApiResponse bulkCreateUsers(MultipartFile file, Long orgId) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        if (contentType == null || fileName == null) {
            throw new RuntimeException("Invalid file input.");
        }
        if (fileName.endsWith(".csv") || contentType.equals("text/csv") || contentType.equals("application/vnd.ms-excel")) {
            // Process CSV
            try {
                String filePath = saveCsvToLocal(file);
                return processCsvFileFromPath(filePath, orgId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            throw new RuntimeException("Unsupported file type. Only CSV files are supported.");
        }
    }

    public ApiResponse processCsvFileFromPath(String filePath, Long orgId) {
        File file = new File(filePath);
        if (!file.exists()) throw new RuntimeException("CSV file not found: " + filePath);

        try (InputStream inputStream = new FileInputStream(file)) {
            return processCsvFile(inputStream, file.getName(), orgId);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read local CSV file: " + e.getMessage(), e);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    public ApiResponse processCsvFile(InputStream inputStream, String originalFileName, Long orgId) {
        logger.info("Starting processing for file: {}", originalFileName);
        long overallStart = System.currentTimeMillis();

        List<UserEntity> userEntities = new ArrayList<>();
        List<SecondaryDetailsEntity> secondaryDetailsEntities = new ArrayList<>();
        List<EmailData> emailRequests = new ArrayList<>();
        List<String> successList = new ArrayList<>();
        Map<UserEntity, Long> userGroupMappings = new HashMap<>();

        int uploadedCount = 0, skippedCount = 0;

        try {
            // Preload reference data
            long preloadStart = System.currentTimeMillis();
            Set<String> existingMobiles = userAdapter.getAllMobileNumbers();
            Set<String> existingEmails = userAdapter.getAllEmails();
            Set<String> existingSecEmail = userAdapter.getAllSecondaryEmail();
            Set<String> existingSecMobile = userAdapter.getAllSecondaryMobile();
            Map<String, Long> roleNameToIdMap = userAdapter.getRoleNameIdMap();
            Map<String, Long> locationNameToIdMap = userAdapter.getLocationNameToIdMap();
            Map<String, Long> groupNameToIdMap = userAdapter.getGroupNameIdMap();
            logger.info("Preloaded reference data in {} ms", (System.currentTimeMillis() - preloadStart));

            // CSV Header structure definition
            List<String> expectedHeaders = List.of(
                    "username", "email", "mobilenumber", "rolename", "locationname", "dateofjoining",
                    "secondaryusername", "secondarymobile", "secondaryemail", "relation", "groupname"
            );

            // Read CSV
            long readCsvStart = System.currentTimeMillis();
            try (InputStreamReader reader = new InputStreamReader(inputStream);
                 CSVReader csvReader = new CSVReader(reader)) {

                String[] headerRow = csvReader.readNext();
                if (headerRow == null) throw new RuntimeException("CSV file missing header row.");
                validateFixedHeaders(headerRow, expectedHeaders);

                // Header index constants
                final int USERNAME_IDX = 0;
                final int EMAIL_IDX = 1;
                final int MOBILE_IDX = 2;
                final int ROLENAME_IDX = 3;
                final int LOCATIONNAME_IDX = 4;
                final int DOJ_IDX = 5;
                final int SEC_USERNAME_IDX = 6;
                final int SEC_MOBILE_IDX = 7;
                final int SEC_EMAIL_IDX = 8;
                final int RELATION_IDX = 9;
                final int GROUPNAME_IDX = 10;

                String[] row;
                while ((row = csvReader.readNext()) != null) {
                    if (row.length < expectedHeaders.size()) {
                        skippedCount++;
                        continue;
                    }

                    // Duplicate checks
                    String mobileNumber = row[MOBILE_IDX].trim();
                    if (existingMobiles.contains(mobileNumber)) { skippedCount++; continue; }

                    String email = row[EMAIL_IDX].trim();
                    if (existingEmails.contains(email)) { skippedCount++; continue; }

                    // Role & Location validation
                    String roleName = row[ROLENAME_IDX].trim();
                    Long roleId = roleNameToIdMap.get(roleName.toLowerCase());
                    if (roleId == null) { skippedCount++; continue; }

                    String locationName = row[LOCATIONNAME_IDX].trim();
                    Long locationId = locationNameToIdMap.get(locationName.toLowerCase());

                    String dojValue = row[DOJ_IDX].trim();
                    if (dojValue.isEmpty()) { skippedCount++; continue; }

                    // Prepare UserDto
                    UserDto userDto = new UserDto();
                    userDto.setUserName(row[USERNAME_IDX].trim());
                    userDto.setEmail(email);
                    userDto.setMobileNumber(mobileNumber);
                    userDto.setRoleId(roleId);
                    userDto.setLocationId(locationId);
                    userDto.setRegisterUser(true);
                    userDto.setDateOfJoining(parseDate(dojValue));

                    String groupName = row[GROUPNAME_IDX].trim();
                    Long groupId = groupName.isEmpty() ? null : groupNameToIdMap.get(groupName.toLowerCase());

                    if (!validator.validate(userDto).isEmpty()) { skippedCount++; continue; }

                    // Create UserEntity
                    String generatedPass = PasswordUtil.generateDefaultPassword();
                    UserEntity userEntity = createUserEntity(userDto, orgId, generatedPass);
                    userEntities.add(userEntity);
                    emailRequests.add(new EmailData(userEntity.getEmail(), userEntity.getUserName(), generatedPass, userEntity.isRegisterUser()));

                    if (groupId != null) {
                        userGroupMappings.put(userEntity, groupId);
                    }
                    // If student — add secondary details
                    if (roleId.equals(STUDENT_ROLE_ID)) {
                        String secMobile = row[SEC_MOBILE_IDX].trim();
                        if(existingSecMobile.contains(secMobile)){skippedCount++;continue;}
                        String secEmail = row[SEC_EMAIL_IDX].trim();
                        if(existingSecEmail.contains(secEmail)){skippedCount++;continue;}

                        SecondaryDetailsEntity secondaryDetails = createSecondaryDetails(
                                userEntity,
                                secMobile,
                                secEmail,
                                row[SEC_USERNAME_IDX].trim(),
                                row[RELATION_IDX].trim()
                        );
                        existingSecMobile.add(secMobile);
                        existingSecEmail.add(secEmail);
                        secondaryDetailsEntities.add(secondaryDetails);
                    }
                    userEntities.add(userEntity);
                    emailRequests.add(new EmailData(userEntity.getEmail(), userEntity.getUserName(), generatedPass, userEntity.isRegisterUser()));

                    existingMobiles.add(mobileNumber);
                    existingEmails.add(email);

                    successList.add(userDto.getUserName());
                    uploadedCount++;
                }
            }
            logger.info("CSV parsed and users prepared in {} ms", (System.currentTimeMillis() - readCsvStart));

            // Save Users
            long saveUserStart = System.currentTimeMillis();
            List<UserEntity> savedUsers = userAdapter.saveAllUsers(userEntities);
            logger.info("Saved users in {} ms", (System.currentTimeMillis() - saveUserStart));

            // Save Secondary Details
            long saveSecondaryStart = System.currentTimeMillis();
            userAdapter.saveAllSecondaryDetails(secondaryDetailsEntities);
            logger.info("Saved secondary details in {} ms", (System.currentTimeMillis() - saveSecondaryStart));

            // Save UserGroups
            long saveGroupStart = System.currentTimeMillis();
            List<UserGroupEntity> userGroupEntities = new ArrayList<>();
            for (UserEntity user : savedUsers) {
                Long groupId = userGroupMappings.get(user);
                if (groupId != null) {
                    UserGroupEntity userGroup = new UserGroupEntity();
                    userGroup.setUser(user);
                    userGroup.setGroup(new GroupEntity(groupId));
                    userGroup.setType("Member");
                    userGroupEntities.add(userGroup);
                }
            }
            userAdapter.saveAllUserGroups(userGroupEntities);
            logger.info("Saved user groups in {} ms", (System.currentTimeMillis() - saveGroupStart));

            // Send emails asynchronously
            Long saveEmailStart = System.currentTimeMillis();
            sendEmailsAsync(emailRequests);
            logger.info("Send Mail to Users in {} ms",System.currentTimeMillis() - saveEmailStart);

        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Failed to process CSV: " + e.getMessage(), e);
        }

        logger.info("Total uploaded users: {}", uploadedCount);
        logger.info("Total skipped users: {}", skippedCount);
        logger.info("Overall CSV processing took {} ms", (System.currentTimeMillis() - overallStart));

        Map<String, Object> result = new HashMap<>();
        result.put("success", successList);
        logger.info("Completed processing for file: {}", originalFileName);
        return new ApiResponse(200, "Bulk CSV upload completed", result);
    }
    public String saveCsvToLocal(MultipartFile file) throws IOException {
        Files.createDirectories(Paths.get(uploadDir));

        String originalFilename = file.getOriginalFilename();
        String baseName = FilenameUtils.getBaseName(originalFilename);
        String extension = FilenameUtils.getExtension(originalFilename);
        File destFile = new File(uploadDir + originalFilename);

        int count = 1;
        while (destFile.exists()) {
            destFile = new File(uploadDir + baseName + "(" + count + ")." + extension);
            count++;
        }

        file.transferTo(destFile);
        return destFile.getAbsolutePath();
    }

    @Scheduled(cron = "0 0 2 * * ?") // 2 AM every day
    public void deleteOldCsvFiles() {
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            logger.warn("Upload directory does not exist: {}", uploadDir);
            return;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                long diff = System.currentTimeMillis() - file.lastModified();
                if (diff > TimeUnit.DAYS.toMillis(1)) {
                    boolean deleted = file.delete();
                    if (deleted) {
                        logger.info("Deleted file: {}", file.getName());
                    } else {
                        logger.error("Failed to delete file: {}", file.getName());
                    }
                }
            }
        }
    }

    private UserEntity createUserEntity(UserDto userDto, Long orgId,String generatedPass) {
        UserEntity entity = userEntityMapper.toEntity(userDtoMapper.toMiddleware(userDto));
        entity.setOrganizationId(orgId);
        entity.setPassword(PasswordUtil.encryptPassword(generatedPass));
        entity.setDefaultPassword(true);
        entity.setActive(true);
        entity.setCreatedAt(LocalDateTime.now());

        if(userDto.getGroupId()!=null) {
            createUserGroup(new UserGroup(userDto.getGroupId(), userDto.getUserId(), "Member"), orgId);
        }
        return entity;
    }

    private SecondaryDetailsEntity createSecondaryDetails(UserEntity user, String secMobile, String secEmail, String secUserName, String relation) {
        SecondaryDetailsEntity entity = new SecondaryDetailsEntity();
        entity.setUser(user);
        entity.setMobile(secMobile);
        if(secEmail.isEmpty())entity.setEmail(null);
        else entity.setEmail(secEmail);
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
        for (EmailData emailData : emailRequests) {
                    emailUtil.sendAccountCreationEmail(emailData.getEmail(), emailData.getUserName(), emailData.getGeneratedPass(), emailData.isNewUser());
            }
    }

    @Override
    public ApiResponse createUser(UserDto userDto, Long organizationId) {
        log.info("Evicting cache for orgId: {}", organizationId);
        User usermiddleware = userDtoMapper.toMiddleware(userDto);
        UserEntity entity = userEntityMapper.toEntity(usermiddleware);
        entity.setOrganizationId(organizationId);

        if (usermiddleware.getRoleId() == null) {
            throw new IllegalArgumentException("roleId must not be null");
        }

        RoleEntity role = roleRepository.findById(usermiddleware.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + usermiddleware.getRoleId()));

        entity.setRole(role);
        String defaultPassword = PasswordUtil.generateDefaultPassword();
        String encryptedPassword = PasswordUtil.encryptPassword(defaultPassword);
        entity.setPassword(encryptedPassword);
        entity.setDefaultPassword(true);
        entity.setActive(true);
        entity.setCreatedAt(LocalDateTime.now());

        UserEntity saveEntity = userAdapter.saveUser(entity);
        boolean isNewUser = saveEntity.isDefaultPassword();
        emailUtil.sendAccountCreationEmail(usermiddleware.getEmail(), usermiddleware.getUserName(), defaultPassword, isNewUser);
        // if we want to add member to group
        if(userDto.getGroupId()!=null) {
            createUserGroup(new UserGroup(userDto.getGroupId(), saveEntity.getUserId(), "Member"), organizationId);
        }
        User finalUser = userEntityMapper.toMiddleware(saveEntity);
        return new ApiResponse(201,"Successfully saved user", finalUser);
    }

    @Override
    public User updateUser(CreateUserDto updates, Long orgId, Long userId) {

        UserEntity existingUser = userAdapter.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!existingUser.getOrganizationId().equals(orgId)) {
            throw new RuntimeException("Unauthorized");
        }

        UserDto userDto = updates.getUser();
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
            existingUser.setRegisterUser(userDto.isRegisterUser());
            if(userDto.getLocationId() != null){
                existingUser.setLocationId(userDto.getLocationId());
            }
            if(userDto.getDateOfJoining() != null){
                existingUser.setDateOfJoining(userDto.getDateOfJoining());
            }

            //If user is edit the table also edit the secondary table...
            if(existingUser.getRole().getRoleId() == STUDENT_ROLE_ID){
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
                        existingSecondaryUser.setEmail(secondaryDetails.getEmail());
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
        return fetchFreshUsers(orgId, role);
    }

    private List<UserResponseDto> fetchFreshUsers(Long orgId, String role) {
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
                    String mergedGroups = existing.getGroupName();
                    if (!mergedGroups.contains(user.getGroupName())) {
                        mergedGroups += ", " + user.getGroupName();
                        existing.setGroupName(mergedGroups);
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
        userAdapter.deactivateUserById(userId);
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
            WorkScheduleEntity defaultWs = workScheduleAdapter.findDefaultActiveSchedule();
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
    public boolean createSecondaryUser(SecondaryDetailsDto secondaryDetailsDto,UserEntity savedUser) {
        SecondaryDetails secondaryDetails = secondaryDetailsMapper.toMiddleware(secondaryDetailsDto);
        SecondaryDetailsEntity secondaryDetailsEntity = secondaryDetailsMapper.toEntity(secondaryDetails);
        secondaryDetailsEntity.setUser(savedUser);

            if(secondaryDetailsDto.getEmail()!=null && !secondaryDetailsDto.getEmail().isEmpty()){

                secondaryDetailsEntity.setEmail(secondaryDetailsDto.getEmail());
            }else{
        secondaryDetailsEntity.setEmail(null);}
        SecondaryDetailsEntity savedSD = userAdapter.saveSecondaryDetails(secondaryDetailsEntity);

        if(savedSD==null){return false;}
        return true;
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
        LocationEntity location = userAdapter.findLocationById(user.getLocationId());
        if (location == null) {
            throw new NullPointerException("Location not found.");
        }
        LocationDto locationDto = userDtoMapper.toDto(location);

        // Map groups if present
        List<UserGroupProfileDto> groupDtos = userGroups.isEmpty()
                ? Collections.emptyList()
                : userGroups.stream()
                .map(userDtoMapper::toGroupsDto)
                .collect(Collectors.toList());

        // Construct response
        return new UserProfileResponse(
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getRole().getName(),
                user.getDateOfJoining(),
                locationDto,
                groupDtos
        );
    }

    public List<GroupResponseDto> getAllGroups(Long orgId, Long userId) throws JsonProcessingException {

        UserEntity currentUser = userAdapter.getUserById(userId);
        String roleName = currentUser.getRole().getName().toUpperCase();
        boolean canSeeAllGroups = RolePrivilegeMapper.hasPrivilege(RoleName.valueOf(roleName), Privilege.CAN_SEE_ALL_GROUPS);
        boolean canSeeSupervisingGroups = RolePrivilegeMapper.hasPrivilege(RoleName.valueOf(roleName), Privilege.CAN_SEE_SUPERVISING_GROUPS);

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
            // If user has CAN_SEE_SUPERVISING_GROUPS, only add groups where user is supervisor
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
    public void deleteGroup(Long groupId) {
        userAdapter.deleteByGroupId(groupId);
        userAdapter.deleteGroup(groupId);
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
                .filter(ug -> ug.getType().equalsIgnoreCase("Member"))  // Only members
                .map(ug -> ug.getUser().getUserId())
                .filter(id -> !id.equals(userIdFromToken))  // Exclude logged-in user
                .toList();

        if (memberIds == null || memberIds.isEmpty()) {
            return Collections.emptyList();  // If no members found, return empty
        }

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

        // Save to DB — triggers @PostPersist in listener
        LocationEntity savedEntity = userAdapter.addLocation(locationModel);

        //  No manual Redis cache update here

        log.info("Publishing LocationCacheReloadEvent");
        publisher.publishEvent(new LocationCacheReloadEvent());
        log.info("LocationCacheReloadEvent published");
        return locationEntityMapper.toDto(savedEntity);
    }
}
