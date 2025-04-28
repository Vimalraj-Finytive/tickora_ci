package com.uniq.tms.tms_microservice.mapper;

import com.uniq.tms.tms_microservice.dto.Privilege;
import com.uniq.tms.tms_microservice.dto.RoleName;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RolePrivilegeMapper {

    private static final Map<RoleName, Set<Privilege>> ROLE_PRIVILEGE_MAP = new HashMap<>();

    static {
        ROLE_PRIVILEGE_MAP.put(RoleName.SUPERADMIN, EnumSet.allOf(Privilege.class));

        ROLE_PRIVILEGE_MAP.put(RoleName.ADMIN, EnumSet.of(
                Privilege.CREATE_NEW_MEMBER,
                Privilege.LIST_OF_MEMBER,
                Privilege.EDIT_MEMBER_PROFILE,
                Privilege.EDIT_MEMBER_ROLE,
                Privilege.DELETE_MEMBER,
                Privilege.CREATE_NEW_GROUP,
                Privilege.ADD_GROUP_MEMBER,
                Privilege.REMOVE_GROUP_MEMBER,
                Privilege.DELETE_GROUP,
                Privilege.EDIT_GROUP,
                Privilege.CAN_SEE_GROUP_LEVEL_TIMESHEETS,
                Privilege.CAN_SEE_OWN_TIMESHEET,
                Privilege.EDIT_TIMESHEET,
                Privilege.VIEW_SCHEDULE,
                Privilege.CREATE_NEW_LOCATION,
                Privilege.VIEW_LOCATIONS,
                Privilege.CREATE_WORK_SCHEDULE,
                Privilege.STUDENT_ATTENDANCE,
                Privilege.CHECK_IN_AND_OUT,
                Privilege.CAN_SEE_SUPERVISING_GROUPS
        ));

        ROLE_PRIVILEGE_MAP.put(RoleName.MANAGER, EnumSet.of(
                Privilege.LIST_OF_MEMBER,
                Privilege.CREATE_NEW_GROUP,
                Privilege.ADD_GROUP_MEMBER,
                Privilege.REMOVE_GROUP_MEMBER,
                Privilege.CAN_SEE_GROUP_LEVEL_TIMESHEETS,
                Privilege.CAN_SEE_OWN_TIMESHEET,
                Privilege.VIEW_SCHEDULE,
                Privilege.VIEW_LOCATIONS,
                Privilege.EDIT_TIMESHEET,
                Privilege.STUDENT_ATTENDANCE,
                Privilege.CHECK_IN_AND_OUT,
                Privilege.DELETE_GROUP,
                Privilege.EDIT_GROUP,
                Privilege.CAN_SEE_SUPERVISING_GROUPS
        ));

        ROLE_PRIVILEGE_MAP.put(RoleName.STAFF, EnumSet.of(
                Privilege.CAN_SEE_OWN_TIMESHEET,
                Privilege.VIEW_SCHEDULE,
                Privilege.VIEW_LOCATIONS,
                Privilege.STUDENT_ATTENDANCE,
                Privilege.CHECK_IN_AND_OUT,
                Privilege.CAN_SEE_GROUP_LEVEL_TIMESHEETS,
                Privilege.CAN_SEE_SUPERVISING_GROUPS
        ));

        ROLE_PRIVILEGE_MAP.put(RoleName.STUDENT, EnumSet.of(
                Privilege.VIEW_SCHEDULE,
                Privilege.VIEW_LOCATIONS,
                Privilege.CAN_SEE_OWN_TIMESHEET
        ));
    }

    public static Set<Privilege> getPrivilegesForRole(RoleName roleName) {
        return ROLE_PRIVILEGE_MAP.getOrDefault(roleName, Collections.emptySet());
    }

    public static boolean hasPrivilege(RoleName roleName, Privilege privilege) {
        return getPrivilegesForRole(roleName).contains(privilege);
    }
}
