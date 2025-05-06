package com.uniq.tms.tms_microservice.mapper;

import com.uniq.tms.tms_microservice.dto.*;
import com.uniq.tms.tms_microservice.model.*;
import com.uniq.tms.tms_microservice.model.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserDtoMapper {

    RoleDto toDto(Role role);
    LocationDto toDto(Location location);
    @Mapping(target = "roleId" , source = "roleId")
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
}
