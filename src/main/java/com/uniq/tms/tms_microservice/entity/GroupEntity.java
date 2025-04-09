package com.uniq.tms.tms_microservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "group_table")
public class GroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "group_name", nullable = false, length = 255)
    private String groupName;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organizationEntity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "managers_id", columnDefinition = "jsonb")
    private List<Long> managerIds;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private LocationEntity locationEntity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "group_members_id", columnDefinition = "jsonb")
    private List<Long> groupMemberIds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "supervisors_id")
    private List<Long> supervisorsId;

    @Column(name = "work_schedule_id")
    private Long workScheduleId;

    public Long getWorkScheduleId() {
        return workScheduleId;
    }

    public void setWorkScheduleId(Long workScheduleId) {
        this.workScheduleId = workScheduleId;
    }

    public List<Long> getSupervisorsId() {
        return supervisorsId;
    }

    public void setSupervisorsId(List<Long> supervisorsId) {
        this.supervisorsId = supervisorsId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<Long> getManagerIds() {
        return managerIds;
    }

    public void setManagerIds(List<Long> managerIds) {
        this.managerIds = managerIds;
    }

    public LocationEntity getLocationEntity() {
        return locationEntity;
    }

    public void setLocationEntity(LocationEntity locationEntity) {
        this.locationEntity = locationEntity;
        if (!locationEntity.getGroups().contains(this)) {
            locationEntity.getGroups().add(this);
        }
    }

    public List<Long> getGroupMemberIds() {
        return groupMemberIds;
    }

    public void setGroupMemberIds(List<Long> groupMemberIds) {
        this.groupMemberIds = groupMemberIds;
    }

    public OrganizationEntity getOrganizationEntity() {
        return organizationEntity;
    }

    public void setOrganizationEntity(OrganizationEntity organizationEntity) {
        this.organizationEntity = organizationEntity;
        if (!organizationEntity.getGroups().contains(this)) {
            organizationEntity.getGroups().add(this);
        }
    }
}

