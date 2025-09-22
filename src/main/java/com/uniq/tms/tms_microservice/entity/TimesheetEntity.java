package com.uniq.tms.tms_microservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "timesheet")
public class TimesheetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private UserEntity user;
    private LocalDate date;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime firstClockIn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime lastClockOut;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime trackedHours;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime regularHours;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime totalBreakHours;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "timesheet")
    public List<TimesheetHistoryEntity> timesheetHistoryEntities;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private TimesheetStatusEntity status;

    public List<TimesheetHistoryEntity> getTimesheetHistoryEntities() {
        return timesheetHistoryEntities;
    }

    public void setTimesheetHistoryEntities(List<TimesheetHistoryEntity> timesheetHistoryEntities) {
        this.timesheetHistoryEntities = timesheetHistoryEntities;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getFirstClockIn() {
        return firstClockIn;
    }

    public void setFirstClockIn(LocalTime firstClockIn) {
        this.firstClockIn = firstClockIn;
    }

    public LocalTime getLastClockOut() {
        return lastClockOut;
    }

    public void setLastClockOut(LocalTime lastClockOut) {
        this.lastClockOut = lastClockOut;
    }

    public LocalTime getTrackedHours() {
        return trackedHours;
    }

    public void setTrackedHours(LocalTime trackedHours) {
        this.trackedHours = trackedHours;
    }

    public LocalTime getRegularHours() {
        return regularHours;
    }

    public void setRegularHours(LocalTime regularHours) {
        this.regularHours = regularHours;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalTime getTotalBreakHours() {
        return totalBreakHours;
    }

    public void setTotalBreakHours(LocalTime totalBreakHours) {
        this.totalBreakHours = totalBreakHours;
    }

    public TimesheetStatusEntity getStatus() {
        return status;
    }

    public void setStatus(TimesheetStatusEntity status) {
        this.status = status;
    }
}
