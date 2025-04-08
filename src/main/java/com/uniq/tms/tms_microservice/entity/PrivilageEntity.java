package com.uniq.tms.tms_microservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "privilage")
public class PrivilageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "privilage_id")
    private Long privilageId;
    @Column(name = "privilage_name")
    private String name;

    @ManyToMany(mappedBy = "privilageEntities")
    private Set<RoleEntity> roles= new HashSet<>();

    public Set<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleEntity> roles) {
        this.roles = roles;
    }

    public Long getPrivilageId() {
        return privilageId;
    }

    public void setPrivilageId(Long privilageId) {
        this.privilageId = privilageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
