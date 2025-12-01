package com.uniq.tms.tms_microservice.modules.leavemanagement.entity;

import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.ViewerType;
import jakarta.persistence.*;

@Entity
@Table(name = "users_request_mapping")
public class UsersRequestMappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "viewer_id", length = 20, nullable = false)
    private String viewerId;

    @Column(name = "requester_id", length = 20, nullable = false)
    private String requesterId;

    @Column(name = "timeoff_request_id", length = 20, nullable = false)
    private Long timeOffRequestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private ViewerType type;

    public UsersRequestMappingEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getViewerId() {
        return viewerId;
    }

    public void setViewerId(String viewerId) {
        this.viewerId = viewerId;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public Long getTimeOffRequestId() {
        return timeOffRequestId;
    }

    public void setTimeOffRequestId(Long timeOffRequestId) {
        this.timeOffRequestId = timeOffRequestId;
    }

    public ViewerType getType() {
        return type;
    }

    public void setType(ViewerType type) {
        this.type = type;
    }
}
