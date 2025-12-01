package com.uniq.tms.tms_microservice.modules.leavemanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "timeoff_request_history")
public class TimeOffRequestHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timeoff_request_id", nullable = false)
    private TimeOffRequestEntity timeOffRequest;

    @Column(name = "user_id", nullable = false, length = 20)
    private String userId;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "action_by", nullable = false, length = 50)
    private String actionBy;

    @Column(name = "action_at", nullable = false)
    private LocalDateTime actionAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TimeOffRequestEntity getTimeOffRequest() {
        return timeOffRequest;
    }

    public void setTimeOffRequest(TimeOffRequestEntity timeOffRequest) {
        this.timeOffRequest = timeOffRequest;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionBy() {
        return actionBy;
    }

    public void setActionBy(String actionBy) {
        this.actionBy = actionBy;
    }

    public LocalDateTime getActionAt() {
        return actionAt;
    }

    public void setActionAt(LocalDateTime actionAt) {
        this.actionAt = actionAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.actionAt == null) {
            this.actionAt = LocalDateTime.now();
        }
    }
}
