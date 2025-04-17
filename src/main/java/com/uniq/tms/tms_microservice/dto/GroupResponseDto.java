package com.uniq.tms.tms_microservice.dto;
import java.util.List;

public class GroupResponseDto {
    private Long groupId;
    private String groupName;
    private String location;
    private List<UserGroupDto> membersDetails;

    public GroupResponseDto() {}

    public GroupResponseDto(Long groupId, String groupName, String location,  List<UserGroupDto> membersDetails) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.location = location;
        this.membersDetails = membersDetails;
    }

    public List<UserGroupDto> getMembersDetails() {return membersDetails;}

    public void setMembersDetails(List<UserGroupDto> membersDetails) {this.membersDetails = membersDetails;}

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
