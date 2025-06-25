package com.uniq.tms.tms_microservice.dto;

public enum UserRole {
    SUPERADMIN(1), ADMIN(2), MANAGER(3), STAFF(4), STUDENT(5);

    private final int hierarchyLevel;

    UserRole(int hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    public int getHierarchyLevel(){
        return hierarchyLevel;
    }

    public static int getLevel(String roleName) {
        try{
            return UserRole.valueOf(roleName.toUpperCase()).getHierarchyLevel();
        }catch (IllegalArgumentException e){
            return Integer.MAX_VALUE;
        }
    }
}
