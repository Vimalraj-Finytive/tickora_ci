package com.uniq.tms.tms_microservice.entity;

import com.uniq.tms.tms_microservice.enums.DayOfWeekEnum;
import jakarta.persistence.*;

@Entity
@Table(name = "flexible_work_schedule")
public class FlexibleWorkScheduleEntity {

    @Id
    @Column(name = "flexible_work_schedule_id")
    private String flexibleWorkScheduleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day", nullable = false)
    private DayOfWeekEnum day;

    @Column(name = "duration", nullable = false)
    private Double duration;

    @ManyToOne(optional = false)
    @JoinColumn(name = "work_schedule_id", nullable = false)
    private WorkScheduleEntity workScheduleEntity;

    public java.lang.String getFlexibleWorkScheduleId() {
        return flexibleWorkScheduleId;
    }

    public void setFlexibleWorkScheduleId(java.lang.String flexibleWorkScheduleId) {
        this.flexibleWorkScheduleId = flexibleWorkScheduleId;
    }

    public DayOfWeekEnum getDay() {
        return day;
    }

    public void setDay(DayOfWeekEnum day) {
        this.day = day;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public WorkScheduleEntity getWorkScheduleEntity() {
        return workScheduleEntity;
    }

    public void setWorkScheduleEntity(WorkScheduleEntity workScheduleEntity) {
        this.workScheduleEntity = workScheduleEntity;
    }
}

