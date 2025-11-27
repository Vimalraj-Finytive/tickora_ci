package com.uniq.tms.tms_microservice.modules.leavemanagement.entity;

import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.Status;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "timeoff_request")
public class TimeOffRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "timeoff_request_id")
    private Long timeOffRequestId;

    @OneToMany(mappedBy = "timeOffRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeOffRequestHistoryEntity> historyRecords;

    @ManyToOne
    @JoinColumn(name = "policy_id")
    private TimeOffPolicyEntity policy;

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

    @Column(name = "units_requested")
    private Integer unitsRequested;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private Status status;

    @Column(name = "reason", length = 255)
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getTimeOffRequestId() {
        return timeOffRequestId;
    }

    public void setTimeOffRequestId(Long timeOffRequestId) {
        this.timeOffRequestId = timeOffRequestId;
    }

    public TimeOffPolicyEntity getPolicy() {
        return policy;
    }

    public void setPolicy(TimeOffPolicyEntity policy) {
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

    public List<TimeOffRequestHistoryEntity> getHistoryRecords() {
        return historyRecords;
    }

    public void setHistoryRecords(List<TimeOffRequestHistoryEntity> historyRecords) {
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

    public Integer getUnitsRequested() {
        return unitsRequested;
    }

    public void setUnitsRequested(Integer unitsRequested) {
        this.unitsRequested = unitsRequested;
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
