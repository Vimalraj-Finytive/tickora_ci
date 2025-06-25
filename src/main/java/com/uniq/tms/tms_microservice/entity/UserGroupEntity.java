package com.uniq.tms.tms_microservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name="user_group")
public class UserGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @Column(name = "type", nullable=false)
    private String type = "Member";

    public UserGroupEntity() {
    }

    public UserGroupEntity(Long id, UserEntity user, GroupEntity group, String type) {
        this.id = id;
        this.user = user;
        this.group = group;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public GroupEntity getGroup() {
        return group;
    }

    public void setGroup(GroupEntity group) {
        this.group = group;
    }
}
