package com.uniq.tms.tms_microservice.mapper;



import com.uniq.tms.tms_microservice.dto.AddGroupDto;
import com.uniq.tms.tms_microservice.dto.AddMemberDto;
import com.uniq.tms.tms_microservice.dto.GroupDto;
import com.uniq.tms.tms_microservice.dto.GroupResponseDto;
import com.uniq.tms.tms_microservice.dto.LocationDto;
import com.uniq.tms.tms_microservice.dto.RoleDto;
import com.uniq.tms.tms_microservice.dto.UserDto;
import com.uniq.tms.tms_microservice.model.AddGroup;
import com.uniq.tms.tms_microservice.model.Group;
import com.uniq.tms.tms_microservice.model.GroupResponse;
import com.uniq.tms.tms_microservice.model.Location;
import com.uniq.tms.tms_microservice.model.Member;
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
    AddGroup toMiddleware(AddGroupDto dto);
    Member toMiddleware(AddMemberDto addMemberDto);
    GroupResponseDto toDto(GroupResponse groupResponse);
    UserDto toDto(User user);
    GroupDto toDto(Group group);
}
