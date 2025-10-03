package com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity;

import com.uniq.tms.tms_microservice.modules.workScheduleManagement.enums.DayOfWeekEnum;
import jakarta.persistence.*;
import java.sql.Time;

@Entity
@Table(name = "fixed_work_schedule")
public class FixedWorkScheduleEntity {

    @Id
    @Column(name = "fixed_work_schedule_id")
    private String fixedWorkScheduleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day", nullable = false)
    private DayOfWeekEnum day;

    @Column(name = "start_time", nullable = false)
    private Time startTime;

    @Column(name = "end_time", nullable = false)
    private Time endTime;

    @Column(name = "duration", nullable = false)
    private Double duration;

    @ManyToOne(optional = false)
    @JoinColumn(name = "work_schedule_id", nullable = false)
    private WorkScheduleEntity workScheduleEntity;

    public String getFixedWorkScheduleId() {
        return fixedWorkScheduleId;
    }

    public void setFixedWorkScheduleId(java.lang.String fixedWorkScheduleId) {
        this.fixedWorkScheduleId = fixedWorkScheduleId;
    }

    public DayOfWeekEnum getDay() {
        return day;
    }

    public void setDay(DayOfWeekEnum day) {
        this.day = day;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public WorkScheduleEntity getWorkScheduleEntity() {
        return workScheduleEntity;
    }

    public void setWorkScheduleEntity(WorkScheduleEntity workScheduleEntity) {
        this.workScheduleEntity = workScheduleEntity;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }
}
