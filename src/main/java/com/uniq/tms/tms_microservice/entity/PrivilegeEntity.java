package com.uniq.tms.tms_microservice.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "privilege")
public class PrivilegeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "privilege_id")
    private Long privilegeId;
    @Column(name = "privilege_name")
    private String name;
    @Column(name = "Constant_name")
    private String staticName;
    @ManyToMany(mappedBy = "privilegeEntities", fetch = FetchType.LAZY)
    private Set<RoleEntity> roles= new HashSet<>();

    public Set<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleEntity> roles) {
        this.roles = roles;
    }

    public Long getPrivilegeId() {
        return privilegeId;
    }

    public void setPrivilegeId(Long privilegeId) {
        this.privilegeId = privilegeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStaticName() {
        return staticName;
    }

    public void setStaticName(String staticName) {
        this.staticName = staticName;
    }
}
