package com.uniq.tms.tms_microservice.modules.leavemanagement.entity;

import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.AccrualLeaveType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.Status;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "timeoff_request")
public class TimeoffRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "timeoff_request_id")
    private Long timeoffRequestId;

    @OneToMany(mappedBy = "timeoffRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeoffRequestHistoryEntity> historyRecords;

    @ManyToOne
    @JoinColumn(name = "policy_id")
    private TimeoffPolicyEntity policy;

    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", length = 20)
    private AccrualLeaveType leaveType;

    @Column(name = "units_requested")
    private Integer unitsRequested;

    @Column(name = "hours_requested")
    private Integer hoursRequested;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private Status status;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getTimeoffRequestId() {
        return timeoffRequestId;
    }

    public void setTimeoffRequestId(Long timeoffRequestId) {
        this.timeoffRequestId = timeoffRequestId;
    }

    public TimeoffPolicyEntity getPolicy() {
        return policy;
    }

    public void setPolicy(TimeoffPolicyEntity policy) {
        this.policy = policy;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public List<TimeoffRequestHistoryEntity> getHistoryRecords() {
        return historyRecords;
    }

    public void setHistoryRecords(List<TimeoffRequestHistoryEntity> historyRecords) {
        this.historyRecords = historyRecords;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public AccrualLeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(AccrualLeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public Integer getUnitsRequested() {
        return unitsRequested;
    }

    public void setUnitsRequested(Integer unitsRequested) {
        this.unitsRequested = unitsRequested;
    }

    public Integer getHoursRequested() {
        return hoursRequested;
    }

    public void setHoursRequested(Integer hoursRequested) {
        this.hoursRequested = hoursRequested;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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
}
