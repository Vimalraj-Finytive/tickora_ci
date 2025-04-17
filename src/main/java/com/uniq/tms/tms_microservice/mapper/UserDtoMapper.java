package com.uniq.tms.tms_microservice.mapper;

import com.uniq.tms.tms_microservice.dto.AddGroupDto;
import com.uniq.tms.tms_microservice.dto.AddMemberDto;
import com.uniq.tms.tms_microservice.dto.GroupDto;
import com.uniq.tms.tms_microservice.dto.LocationDto;
import com.uniq.tms.tms_microservice.dto.RoleDto;
import com.uniq.tms.tms_microservice.dto.UserDto;
import com.uniq.tms.tms_microservice.model.AddGroup;
import com.uniq.tms.tms_microservice.model.AddMember;
import com.uniq.tms.tms_microservice.model.Group;
import com.uniq.tms.tms_microservice.model.Location;
import com.uniq.tms.tms_microservice.model.Role;
import com.uniq.tms.tms_microservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {

    RoleDto toDto(Role role);
    LocationDto toDto(Location location);
    @Mapping(target = "roleId" , source = "roleId")
    User toMiddleware(UserDto dto);
    @Mapping(source = "groupName", target = "groupName")
    @Mapping(source = "locationId", target = "locationId")
    @Mapping(source = "supervisorsId", target = "supervisorsId")
    @Mapping(source = "type", target = "type")
    AddGroup toMiddleware(AddGroupDto dto);
    AddMember toMiddleware(AddMemberDto addMemberDto);
    UserDto toDto(User user);
    GroupDto toGroupDto(Group group);
}

