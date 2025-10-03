package com.uniq.tms.tms_microservice.modules.organizationManagement.model;

public class Role {
    private Long roleId;
    private String name;
    private Long orgId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getOrgId() {return orgId;}

    public void setOrgId(Long orgId) {this.orgId = orgId;}
}
