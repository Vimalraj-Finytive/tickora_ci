package com.uniq.tms.tms_microservice.modules.organizationManagement.entity;

import com.uniq.tms.tms_microservice.modules.userManagement.entity.GroupEntity;
import com.uniq.tms.tms_microservice.modules.locationManagement.entity.LocationEntity;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.WorkScheduleEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organization", schema = "public")
public class OrganizationEntity {

    @Id
    @Column(name = "organization_id")
    private String organizationId;

    @Column(name = "org_name", nullable = false, length = 255)
    private String orgName;

    @Column(name = "org_type", nullable = false, length = 100)
    private String orgType;

    @Column(name = "org_size", nullable = false)
    private Integer orgSize;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "schema_name", nullable = false, unique = true)
    private String schemaName;

    @Column(name = "time_zone", nullable = false, length = 100)
    private String timeZone;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, updatable = false)
    private LocalDateTime updatedAt;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @OneToMany(mappedBy = "organizationEntity", cascade = {}, orphanRemoval = true)
    private List<LocationEntity> locations = new ArrayList<>();

    @OneToMany(mappedBy = "organizationEntity", cascade = {}, orphanRemoval = true)
    private List<GroupEntity> groups = new ArrayList<>();

    @OneToMany(mappedBy = "organizationEntity", cascade = {}, orphanRemoval = true)
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

    public List<WorkScheduleEntity> getWorkSchedule() {
        return workSchedule;
    }

    public void setWorkSchedule(List<WorkScheduleEntity> workSchedule) {
        this.workSchedule = workSchedule;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public Integer getOrgSize() {
        return orgSize;
    }

    public void setOrgSize(Integer orgSize) {
        this.orgSize = orgSize;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
