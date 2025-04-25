package com.uniq.tms.tms_microservice.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organizationEntity;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<UserEntity> users;

    @ManyToMany
    @JoinTable(name = "role_privilege_map", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "privilege_id"))
    private Set<PrivilegeEntity> privilegeEntities = new HashSet<>();

    public Set<PrivilegeEntity> getPrivilegeEntities() {
        return privilegeEntities;
    }

    public void setPrivilegeEntities(Set<PrivilegeEntity> privilegeEntities) {
        this.privilegeEntities = privilegeEntities;
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

    public OrganizationEntity getOrganizationEntity() {
        return organizationEntity;
    }

    public void setOrganizationEntity(OrganizationEntity organizationEntity) {
        this.organizationEntity = organizationEntity;
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
        this.organizationEntity = organizationEntity;
        this.users = users;
    }
}
