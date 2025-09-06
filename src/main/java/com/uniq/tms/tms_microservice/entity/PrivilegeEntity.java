package com.uniq.tms.tms_microservice.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

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
    @OneToMany(mappedBy = "privilege", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RolePrivilegeMapEntity> roleMappings = new ArrayList<>();

    public List<RolePrivilegeMapEntity> getRoleMappings() {
        return roleMappings;
    }

    public void setRoleMappings(List<RolePrivilegeMapEntity> roleMappings) {
        this.roleMappings = roleMappings;
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
