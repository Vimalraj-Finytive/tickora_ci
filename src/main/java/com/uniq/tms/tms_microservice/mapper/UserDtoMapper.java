package com.uniq.tms.tms_microservice.mapper;

import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.entity.LocationEntity;
import com.uniq.tms.tms_microservice.entity.UserGroupEntity;
import com.uniq.tms.tms_microservice.entity.WorkScheduleTypeEntity;
import com.uniq.tms.tms_microservice.model.*;
import com.uniq.tms.tms_microservice.model.Privilege;
import com.uniq.tms.tms_microservice.model.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserDtoMapper.CommonMapper.class})
public interface UserDtoMapper {

    RoleDto toDto(Role role);
    LocationDto toDto(Location location);
    @Mapping(target = "roleId" , source = "roleId")
    @Mapping(target = "workSchedule", source = "workSchedule")
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "active", ignore = true)
    User toMiddleware(UserDto dto);

    @Mapping(target = "groupId", source = "groupId")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "type" , source = "type")
    UserGroup toMiddleware(EditUserGroupDto editUserGroupDto);

    @Mapping(source = "groupName", target = "groupName")
    @Mapping(source = "locationId", target = "locationId")
    @Mapping(source = "supervisorsId", target = "supervisorsId")
    @Mapping(source = "type", target = "type")
    AddGroup toMiddleware(AddGroupDto dto);
    AddMember toMiddleware(AddMemberDto addMemberDto);
    UserDto toDto(User user);
    GroupDto toGroupDto(Group group);
    UserResponseDto toDto(UserResponse userResponse);
    LocationDto toDto(LocationEntity locationEntity);
    WorkScheduleDto toDto(WorkSchedule workSchedule);
    @Mapping(source = "group.groupId", target = "groupId")
    @Mapping(source = "group.groupName", target = "groupName")
    @Mapping(source = "group.locationEntity", target = "location")
    @Mapping(source = "group.workSchedule.scheduleName", target = "workSchedule")
    @Mapping(source = "type", target = "userType")
    UserGroupProfileDto toGroupsDto(UserGroupEntity entity);

    Privilege toModel(PrivilegeDto privilegeDto);

    PrivilegeDto toDto(Privilege savePrivilege);

    RolePrivilege toModel(RolePrivilegeDto rolePrivilegeDto);

    RolePrivilegeDto toDto(RolePrivilege rolePrivilege);

    LocationList toModel(LocationListDto locationDto);

    @Mapper(componentModel = "spring")
    interface CommonMapper {
        default String map(WorkScheduleTypeEntity entity) {
            return entity != null ? entity.getTypeId() : null;
        }

        default WorkScheduleTypeEntity map(String id) {
            if (id == null) return null;
            WorkScheduleTypeEntity type = new WorkScheduleTypeEntity();
            type.setTypeId(id);
            return type;
        }
    }

    Organization toModel(OrganizationDto organizationDto);

    OrganizationTypeDto toDto(OrganizationType organizationType);
}
