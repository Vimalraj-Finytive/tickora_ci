package com.uniq.tms.tms_microservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uniq.tms.tms_microservice.enums.LogFrom;
import com.uniq.tms.tms_microservice.enums.LogType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "timesheet_history")
public class TimesheetHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "timesheet_id", referencedColumnName = "id", nullable = false)
    private TimesheetEntity timesheet;

    @Column(nullable = false)
    private Long locationId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime logTime;
    @Enumerated(EnumType.STRING)
    private LogType logType;
    @Enumerated(EnumType.STRING)
    private LogFrom logFrom;
    private LocalDateTime loggedTimestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TimesheetEntity getTimesheet() {
        return timesheet;
    }

    public void setTimesheet(TimesheetEntity timesheet) {
        this.timesheet = timesheet;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public LocalTime getLogTime() {
        return logTime;
    }

    public void setLogTime(LocalTime logTime) {
        this.logTime = logTime;
    }

    public LogType getLogType() {
        return logType;
    }

    public void setLogType(LogType logType) {
        this.logType = logType;
    }

    public LogFrom getLogFrom() {
        return logFrom;
    }

    public void setLogFrom(LogFrom logFrom) {
        this.logFrom = logFrom;
    }

    public LocalDateTime getLoggedTimestamp() {
        return loggedTimestamp;
    }

    public void setLoggedTimestamp(LocalDateTime loggedTimestamp) {
        this.loggedTimestamp = loggedTimestamp;
    }
}
