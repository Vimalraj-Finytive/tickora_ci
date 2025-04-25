package com.uniq.tms.tms_microservice.mapper;

import com.uniq.tms.tms_microservice.dto.Privilege;
import com.uniq.tms.tms_microservice.dto.Role;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RolePrivilegeMapper {

    private static final Map<Role, Set<Privilege>> ROLE_PRIVILEGE_MAP = new HashMap<>();

    static {
        ROLE_PRIVILEGE_MAP.put(Role.SUPERADMIN, EnumSet.allOf(Privilege.class));

        ROLE_PRIVILEGE_MAP.put(Role.ADMIN, EnumSet.of(
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
                Privilege.CHECK_IN_AND_OUT
        ));

        ROLE_PRIVILEGE_MAP.put(Role.MANAGER, EnumSet.of(
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
                Privilege.EDIT_GROUP
        ));

        ROLE_PRIVILEGE_MAP.put(Role.STAFF, EnumSet.of(
                Privilege.CAN_SEE_OWN_TIMESHEET,
                Privilege.VIEW_SCHEDULE,
                Privilege.VIEW_LOCATIONS,
                Privilege.STUDENT_ATTENDANCE,
                Privilege.CHECK_IN_AND_OUT,
                Privilege.CAN_SEE_GROUP_LEVEL_TIMESHEETS
                ));

        ROLE_PRIVILEGE_MAP.put(Role.STUDENT, EnumSet.of(
                Privilege.VIEW_SCHEDULE,
                Privilege.VIEW_LOCATIONS,
                Privilege.CAN_SEE_OWN_TIMESHEET
        ));
    }

    public static Set<Privilege> getPrivilegesForRole(Role role) {
        return ROLE_PRIVILEGE_MAP.getOrDefault(role, Collections.emptySet());
    }

    public static boolean hasPrivilege(Role role, Privilege privilege) {
        return getPrivilegesForRole(role).contains(privilege);
    }
}
