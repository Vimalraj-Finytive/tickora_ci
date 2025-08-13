package com.uniq.tms.tms_microservice.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organization")
public class OrganizationEntity {

    @Id
    @Column(name = "organization_id")
    private String organizationId;

    @Column(name = "org_name", nullable = false, length = 255)
    private String orgName;

    @Column(name = "org_type", nullable = false, length = 100)
    private String orgType;

    @OneToMany(mappedBy = "organizationEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LocationEntity> locations = new ArrayList<>();

    @OneToMany(mappedBy = "organizationEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoleEntity> roles = new ArrayList<>();

    @OneToMany(mappedBy = "organizationEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupEntity> teams = new ArrayList<>();

    @OneToMany(mappedBy = "organizationEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupEntity> groups = new ArrayList<>();

    @OneToMany(mappedBy = "organizationEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkScheduleEntity> workSchedule = new ArrayList<>();

    public List<GroupEntity> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupEntity> groups) {
        this.groups = groups;
    }

    public List<LocationEntity> getLocations() {
        return locations;
    }

    public void setLocations(List<LocationEntity> locations) {
        this.locations = locations;
    }

    public List<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleEntity> roles) {
        this.roles = roles;
    }

    public List<GroupEntity> getTeams() {
        return teams;
    }

    public void setTeams(List<GroupEntity> teams) {
        this.teams = teams;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrgType() {
        return orgType;
    }

    public void setOrgType(String orgType) {
        this.orgType = orgType;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public List<WorkScheduleEntity > getWorkSchedule() {
        return workSchedule;
    }

    public void setWorkSchedule(List<WorkScheduleEntity > workSchedule) {
        this.workSchedule = workSchedule;
    }
}
