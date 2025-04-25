package com.uniq.tms.tms_microservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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

    @ManyToOne
    @JoinColumn(name = "location_id")
    private LocationEntity locationEntity;

    @ManyToOne
    @JoinColumn(name = "work_schedule_id")
    private WorkScheduleEntity workSchedule;

    public GroupEntity() {
    }

    public GroupEntity(Long groupId){this.groupId=groupId;}

    public WorkScheduleEntity getWorkSchedule() {
        return workSchedule;
    }

    public void setWorkSchedule(WorkScheduleEntity workSchedule) {
        this.workSchedule = workSchedule;
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


    public LocationEntity getLocationEntity() {
        return locationEntity;
    }

    public void setLocationEntity(LocationEntity locationEntity) {
        this.locationEntity = locationEntity;
        if (!locationEntity.getGroups().contains(this)) {
            locationEntity.getGroups().add(this);
        }
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
