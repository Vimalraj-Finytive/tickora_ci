package com.uniq.tms.tms_microservice.modules.userManagement.mapper;

import com.uniq.tms.tms_microservice.modules.locationManagement.entity.UserLocationEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.UserHistoryResponseDto;
import com.uniq.tms.tms_microservice.modules.userManagement.dto.UserValidationDto;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.*;
import com.uniq.tms.tms_microservice.modules.userManagement.model.*;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserEntityMapper {

    @Mapping(target = "roleId", source = "role.roleId")
    @Mapping(target = "workSchedule", source = "workSchedule.scheduleName")
    @Mapping(target = "splitTimeEnabled", source = "splitTimeEnabled")
    User toMiddleware(UserEntity entity);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "defaultPassword", ignore = true)
    @Mapping(target = "role", expression = "java(user.getRoleId() != null ? new RoleEntity(user.getRoleId()) : null)")
    @Mapping(target = "workSchedule.scheduleName", source = "workSchedule")
    @Mapping(target = "workSchedule.type", ignore = true)
    @Mapping(target = "workSchedule.scheduleId", source = "workSchedule")
    @Mapping(target = "splitTimeEnabled", source = "splitTimeEnabled")
    UserEntity toEntity(User user);

    @Mapping(target = "workSchedule", ignore = true)
    @Mapping(target = "locationEntity", expression = "java(new LocationEntity(group.getLocationId()))")
    @Mapping(target = "groupId", ignore = true)
    @Mapping(target = "organizationEntity", ignore = true)
    GroupEntity toEntity(AddGroup group);

    @Mapping(target = "locationId", source = "locationEntity.locationId")
    @Mapping(target = "workScheduleId", source = "workSchedule.scheduleId")
    Group toMiddleware(GroupEntity entity);

    @Mapping(target = "type", ignore = true)
    @Mapping(target = "locationId", source = "locationEntity.locationId")
    @Mapping(target = "workScheduleId", source = "workSchedule.scheduleId")
    @Mapping(target = "supervisorsId", ignore = true)
    AddGroup toGroupMiddleware(GroupEntity entity);

    @Mapping(source = "group.groupId", target = "groupId")
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "type", target = "type")
    UserGroup toMiddleware(UserGroupEntity savedEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", expression = "java(new UserEntity(userGroup.getUserId()))")
    @Mapping(target = "group", expression = "java(new GroupEntity(userGroup.getGroupId()))")
    UserGroupEntity toEntity(UserGroup userGroup);

    @Mapping(target = "workSchedule.scheduleName", source = "workSchedule")
    @Mapping(target = "workSchedule.type", ignore = true)
    UserEntity toMiddleware(User user);

    UserValidationDto toDto(UserEntity user);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "activeStatus", expression = "java(com.uniq.tms.tms_microservice.modules.userManagement.enums.UserStatusTypeEnum.INACTIVE.getValue())")
    @Mapping(target = "comments", source = "comments")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    UserHistoryEntity toInactiveUserEntity(String userId, String comments);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "activeStatus", expression = "java(com.uniq.tms.tms_microservice.modules.userManagement.enums.UserStatusTypeEnum.ACTIVE.getValue())")
    @Mapping(target = "comments", source = "comments")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    UserHistoryEntity toActiveUserEntity(String userId, String comments);

    List<UserHistoryResponseDto> toHistoryDto(List<UserHistoryEntity> responseDtos);
    UserLocationEntity toEntity(BulkUserLocationModel model);

    BulkUserLocationModel toModel(UserLocationEntity entity);
}
