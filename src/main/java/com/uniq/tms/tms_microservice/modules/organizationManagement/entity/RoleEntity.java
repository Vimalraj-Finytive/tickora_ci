package com.uniq.tms.tms_microservice.modules.organizationManagement.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "role")
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "hierarchy_level")
    private Integer hierarchyLevel;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<UserEntity> users;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RolePrivilegeMapEntity> privilegeMappings = new ArrayList<>();

    public List<RolePrivilegeMapEntity> getPrivilegeMappings() {
        return privilegeMappings;
    }

    public void setPrivilegeMappings(List<RolePrivilegeMapEntity> privilegeMappings) {
        this.privilegeMappings = privilegeMappings;
    }

    public void setHierarchyLevel(Integer hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    public int getHierarchyLevel() {
        return hierarchyLevel;
    }

    public int setHierarchyLevel(int hierarchyLevel) {
        return this.hierarchyLevel = hierarchyLevel;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(List<UserEntity> users) {
        this.users = users;
    }

    public RoleEntity(Long roleId) {
        this.roleId = roleId;
    }

    public RoleEntity() {}

    public RoleEntity(Long roleId, String name, OrganizationEntity organizationEntity, List<UserEntity> users) {
        this.roleId = roleId;
        this.name = name;
        this.users = users;
    }
}
