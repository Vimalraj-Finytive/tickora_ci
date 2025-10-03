package com.uniq.tms.tms_microservice.modules.userManagement.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "secondary_details")
public class SecondaryDetailsEntity {

    @Id
    private String id;

    @Column(name = "secondary_user_name", nullable = false)
    private String userName;

    @Column(name = "mobile", nullable = false, length = 10)
    private String mobile;

    @Column(name = "email")
    private String email;

    @Column(name = "relation", nullable = false)
    private String relation;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    public SecondaryDetailsEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
}
