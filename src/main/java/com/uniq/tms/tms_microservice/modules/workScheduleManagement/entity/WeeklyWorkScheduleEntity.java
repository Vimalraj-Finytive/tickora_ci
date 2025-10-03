package com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity;

import com.uniq.tms.tms_microservice.modules.workScheduleManagement.enums.DayOfWeekEnum;
import jakarta.persistence.*;

@Entity
@Table(name = "weekly_work_schedule")
public class WeeklyWorkScheduleEntity {

    @Id
    @Column(name = "weekly_work_schedule_id")
    private java.lang.String weeklyWorkScheduleId;

    @Column(name = "duration", nullable = false)
    private Double duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "start_day", nullable = false)
    private DayOfWeekEnum startDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "end_day", nullable = false)
    private DayOfWeekEnum endDay;

    @OneToOne
    @JoinColumn(name = "work_schedule_id", nullable = false, unique = true)
    private WorkScheduleEntity workScheduleEntity;

    public String getWeeklyWorkScheduleId() {
        return weeklyWorkScheduleId;
    }

    public void setWeeklyWorkScheduleId(String weeklyWorkScheduleId) {
        this.weeklyWorkScheduleId = weeklyWorkScheduleId;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public DayOfWeekEnum getStartDay() {
        return startDay;
    }

    public void setStartDay(DayOfWeekEnum startDay) {
        this.startDay = startDay;
    }

    public DayOfWeekEnum getEndDay() {
        return endDay;
    }

    public void setEndDay(DayOfWeekEnum endDay) {
        this.endDay = endDay;
    }

    public WorkScheduleEntity getWorkScheduleEntity() {
        return workScheduleEntity;
    }

    public void setWorkScheduleEntity(WorkScheduleEntity workScheduleEntity) {
        this.workScheduleEntity = workScheduleEntity;
    }
}

