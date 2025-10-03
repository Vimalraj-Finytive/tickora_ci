package com.uniq.tms.tms_microservice.modules.timesheetManagement.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_embedding")
public class UserFaceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "face_id")
    private Long faceId;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "embeddings")
    private String embeddings;

    public Long getFaceId() {
        return faceId;
    }

    public void setFaceId(Long faceId) {
        this.faceId = faceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmbeddings() {
        return embeddings;
    }

    public void setEmbeddings(String embeddings) {
        this.embeddings = embeddings;
    }
}
