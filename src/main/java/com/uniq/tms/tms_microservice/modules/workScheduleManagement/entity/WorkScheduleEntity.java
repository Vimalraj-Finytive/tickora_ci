package com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import jakarta.persistence.*;

import java.sql.Time;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "work_schedule")
public class WorkScheduleEntity {

    @Id
    @Column(name = "work_schedule_id")
    private String scheduleId;

    @Column(name = "work_schedule_name", nullable = false, length = 100)
    private String scheduleName;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "split_time", nullable = false)
    private Time splitTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "work_schedule_type", referencedColumnName = "type_id")
    private WorkScheduleTypeEntity type;

    @ManyToOne(optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organizationEntity;

    @OneToMany(mappedBy = "workScheduleEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<FixedWorkScheduleEntity> fixedWorkSchedules;

    @OneToMany(mappedBy = "workScheduleEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<FlexibleWorkScheduleEntity> flexibleWorkSchedules;

    @OneToOne(mappedBy = "workScheduleEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private WeeklyWorkScheduleEntity weeklyWorkSchedule;

    @OneToMany(mappedBy = "workSchedule", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<UserEntity> users;

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public Boolean getDefault() {
        return isDefault;
    }

    public void setDefault(Boolean aDefault) {
        isDefault = aDefault;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public WorkScheduleTypeEntity getType() {
        return type;
    }

    public void setType(WorkScheduleTypeEntity type) {
        this.type = type;
    }

    public OrganizationEntity getOrganizationEntity() {
        return organizationEntity;
    }

    public void setOrganizationEntity(OrganizationEntity organizationEntity) {
        this.organizationEntity = organizationEntity;
    }

    public Set<FixedWorkScheduleEntity> getFixedWorkSchedules() {
        return fixedWorkSchedules;
    }

    public void setFixedWorkSchedules(Set<FixedWorkScheduleEntity> fixedWorkSchedules) {
        this.fixedWorkSchedules = fixedWorkSchedules;
    }

    public Set<FlexibleWorkScheduleEntity> getFlexibleWorkSchedules() {
        return flexibleWorkSchedules;
    }

    public void setFlexibleWorkSchedules(Set<FlexibleWorkScheduleEntity> flexibleWorkSchedules) {
        this.flexibleWorkSchedules = flexibleWorkSchedules;
    }

    public WeeklyWorkScheduleEntity getWeeklyWorkSchedule() {
        return weeklyWorkSchedule;
    }

    public void setWeeklyWorkSchedule(WeeklyWorkScheduleEntity weeklyWorkSchedule) {
        this.weeklyWorkSchedule = weeklyWorkSchedule;
    }

    public List<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(List<UserEntity> users) {
        this.users = users;
    }

    public Time getSplitTime() {
        return splitTime;
    }

    public void setSplitTime(Time splitTime) {
        this.splitTime = splitTime;
    }


}
