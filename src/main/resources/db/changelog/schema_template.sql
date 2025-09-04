--liquibase formatted sql

-- ===========================================================
-- Table: privilege
-- ===========================================================
--changeset system:create-privilege-table
CREATE TABLE IF NOT EXISTS ${schemaName}.privilege (
    privilege_id BIGSERIAL PRIMARY KEY,
    privilege_name VARCHAR,
    constant_name VARCHAR
);

--changeset system:insert-privilege-data
INSERT INTO ${schemaName}.privilege (privilege_id, constant_name, privilege_name)
SELECT * FROM (VALUES
    (1, 'REGISTER_ORGANIZATION', 'Register Organization'),
    (2, 'CREATE_NEW_MEMBER', 'Create new member'),
    (3, 'LIST_OF_MEMBER', 'List of member'),
    (4, 'EDIT_MEMBER_PROFILE', 'Edit member profile'),
    (5, 'EDIT_MEMBER_ROLE', 'Edit Member Role'),
    (6, 'DELETE_MEMBER', 'Delete member'),
    (7, 'CREATE_NEW_GROUP', 'Create new group'),
    (8, 'ADD_GROUP_MEMBER', 'Add group member'),
    (9, 'EDIT_GROUP', 'Edit group'),
    (10, 'DELETE_GROUP', 'Delete group'),
    (11, 'REMOVE_GROUP_MEMBER', 'Remove group member'),
    (12, 'CREATE_WORK_SCHEDULE', 'Create work schedule'),
    (13, 'VIEW_SCHEDULE', 'View schedule'),
    (14, 'CREATE_NEW_LOCATION', 'Create new location'),
    (15, 'VIEW_LOCATIONS', 'View locations'),
    (16, 'CHECK_IN_AND_OUT', 'Check in and check out'),
    (17, 'STUDENT_ATTENDANCE', 'Student attendance'),
    (18, 'CAN_SEE_OWN_TIMESHEET', 'Can see own timesheet'),
    (19, 'EDIT_TIMESHEET', 'Edit Timesheet'),
    (20, 'CAN_SEE_ALL_TIMESHEETS', 'Can see all timesheets'),
    (21, 'CAN_SEE_GROUP_LEVEL_TIMESHEETS', 'Can see group-level timesheets'),
    (22, 'LOGIN_VIA_EMAIL', 'Login_Via_Email'),
    (23, 'LOGIN_VIA_MOBILE', 'Login_Via_Mobile'),
    (24, 'HAVE_SECONDARY_DETAILS', 'Secondary Details'),
    (25, 'CAN_SEE_ALL_GROUPS', 'Can see all groups'),
    (26, 'CAN_SEE_SUPERVISING_GROUPS', 'Can see supervising groups')
) AS tmp(privilege_id, constant_name, privilege_name)
WHERE NOT EXISTS (SELECT 1 FROM ${schemaName}.privilege);

-- ===========================================================
-- Table: role
-- ===========================================================
--changeset system:create-role-table
CREATE TABLE IF NOT EXISTS ${schemaName}.role (
    role_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    hierarchy_level INTEGER
);

--changeset system:insert-role-data
INSERT INTO ${schemaName}.role (hierarchy_level, name)
SELECT * FROM (
  VALUES
    (1, 'SuperAdmin'),
    (2, 'Admin'),
    (3, 'Manager'),
    (4, 'Staff'),
    (5, 'Student')
) AS tmp(hierarchy_level, name)
WHERE
  (
    '${orgType}' = 'ORGTY001'
    AND tmp.name IN ('SuperAdmin', 'Admin', 'Manager', 'Staff', 'Student')
  )
  OR
  (
    '${orgType}' = 'ORGTY002'
    AND tmp.name IN ('SuperAdmin', 'Admin', 'Manager', 'Staff')
  )
  AND NOT EXISTS (SELECT 1 FROM ${schemaName}.role);

-- ===========================================================
-- Table: role_privilege_map
-- ===========================================================
--changeset system:create-role-privilege-map
CREATE TABLE IF NOT EXISTS ${schemaName}.role_privilege_map (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    privilege_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    enabled BOOLEAN,
    CONSTRAINT fk_role
        FOREIGN KEY (role_id)
        REFERENCES ${schemaName}.role(role_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_privilege
        FOREIGN KEY (privilege_id)
        REFERENCES ${schemaName}.privilege(privilege_id)
        ON DELETE CASCADE
);

--changeset system:insert-role-privilege-map
INSERT INTO ${schemaName}.role_privilege_map (role_id, privilege_id, enabled, created_at)
SELECT r.role_id,
       p.privilege_id,
       FALSE,
       NOW()
FROM ${schemaName}.role r
CROSS JOIN ${schemaName}.privilege p
WHERE
  (
    '${orgType}' = 'ORGTY001'
    OR ('${orgType}' = 'ORGTY002' AND r.name <> 'Student')
  )
  AND NOT EXISTS (
      SELECT 1 FROM ${schemaName}.role_privilege_map rpm
      WHERE rpm.role_id = r.role_id
        AND rpm.privilege_id = p.privilege_id
  );

--changeset system:activate-role-privilege-defaults
UPDATE ${schemaName}.role_privilege_map
SET enabled = TRUE
WHERE (role_id, privilege_id) IN (
    -- SuperAdmin
    (1,1), (1,2), (1,3), (1,4), (1,5),
    (1,6), (1,7), (1,8), (1,9), (1,10),
    (1,11), (1,12), (1,13), (1,14), (1,15),
    (1,16), (1,17), (1,18), (1,19), (1,20),
    (1,21), (1,22), (1,25),

    -- Admin
    (2,2), (2,3), (2,4), (2,5), (2,6),
    (2,7), (2,8), (2,9), (2,10), (2,11),
    (2,12), (2,13), (2,14), (2,15), (2,16),
    (2,17), (2,18), (2,19), (2,21), (2,22), (2,26),

    -- Manager
    (3,3), (3,7), (3,8), (3,9), (3,10),
    (3,11), (3,13), (3,15), (3,16), (3,17),
    (3,18), (3,19), (3,21), (3,22), (3,26),

    -- Staff
    (4,13), (4,15), (4,16), (4,17), (4,18),
    (4,21), (4,22), (4,26),

    -- Student (Applied only if academic)
    (5,13), (5,15), (5,18), (5,23), (5,24))
AND (
    '${orgType}' = 'ORGTY001'
    OR role_id <> 5
);

-- ===========================================================
-- Table: work_schedule_type
-- ===========================================================
--changeset system:create-work-schedule-type
CREATE TABLE ${schemaName}.work_schedule_type (
    type_id VARCHAR(20) PRIMARY KEY,
    type VARCHAR NOT NULL UNIQUE
);

--changeset system:insert-work-schedule-type
INSERT INTO ${schemaName}.work_schedule_type (type_id, type)
SELECT * FROM (VALUES
    ('WSTY001', 'FIXED'),
    ('WSTY002', 'FLEXIBLE')
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM ${schemaName}.work_schedule_type);

-- ===========================================================
-- Table: work_schedule
-- ===========================================================
--changeset system:create-work-schedule
CREATE TABLE ${schemaName}.work_schedule (
    work_schedule_id VARCHAR PRIMARY KEY,
    work_schedule_name VARCHAR(100) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL,
    work_schedule_type VARCHAR NOT NULL,
    organization_id VARCHAR(20) NOT NULL,
    CONSTRAINT fk_work_schedule_type
        FOREIGN KEY (work_schedule_type)
        REFERENCES ${schemaName}.work_schedule_type(type_id),
    CONSTRAINT fk_organization
        FOREIGN KEY (organization_id)
        REFERENCES public.organization(organization_id)
);

-- ===========================================================
-- Table: users
-- ===========================================================
--changeset system:create-users
CREATE TABLE IF NOT EXISTS ${schemaName}.users (
    user_id VARCHAR(20) PRIMARY KEY,
    user_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    mobile_number VARCHAR(10) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    organization_id VARCHAR(20),
    role_id BIGINT NOT NULL,
    work_schedule_id VARCHAR,
    created_at TIMESTAMP NOT NULL,
    is_default_password BOOLEAN NOT NULL DEFAULT TRUE,
    date_of_joining DATE,
    is_register_user BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL,
    CONSTRAINT fk_role
        FOREIGN KEY (role_id)
        REFERENCES ${schemaName}.role(role_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_work_schedule
        FOREIGN KEY (work_schedule_id)
        REFERENCES ${schemaName}.work_schedule(work_schedule_id)
        ON DELETE SET NULL
);

-- ===========================================================
-- Table: location
-- ===========================================================
--changeset system:create-location
CREATE TABLE IF NOT EXISTS ${schemaName}.location (
    location_id BIGSERIAL PRIMARY KEY,
    organization_id VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    latitude VARCHAR(255) NOT NULL,
    longitude VARCHAR(255) NOT NULL,
    radius VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_location_organization
        FOREIGN KEY (organization_id)
        REFERENCES public.organization(organization_id)
        ON DELETE RESTRICT
);

-- ===========================================================
-- Table: org_groups
-- ===========================================================
--changeset system:create-org-groups
CREATE TABLE IF NOT EXISTS ${schemaName}.org_groups (
    group_id BIGSERIAL PRIMARY KEY,
    group_name VARCHAR(255) NOT NULL,
    organization_id VARCHAR(20) NOT NULL,
    location_id BIGINT,
    work_schedule_id VARCHAR,
    CONSTRAINT fk_group_organization
        FOREIGN KEY (organization_id)
        REFERENCES public.organization(organization_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_group_location
        FOREIGN KEY (location_id)
        REFERENCES ${schemaName}.location(location_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_group_work_schedule
        FOREIGN KEY (work_schedule_id)
        REFERENCES ${schemaName}.work_schedule(work_schedule_id)
        ON DELETE SET NULL
);

-- ===========================================================
-- Table: user_group
-- ===========================================================
--changeset system:create-user-group
CREATE TABLE IF NOT EXISTS ${schemaName}.user_group (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL,
    group_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'Member',
    CONSTRAINT fk_user_group_user
        FOREIGN KEY (user_id)
        REFERENCES ${schemaName}.users(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_group_org_groups
        FOREIGN KEY (group_id)
        REFERENCES ${schemaName}.org_groups(group_id)
        ON DELETE CASCADE
);

-- ===========================================================
-- Table: user_location
-- ===========================================================
--changeset system:create-user-location
CREATE TABLE IF NOT EXISTS ${schemaName}.user_location (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(20),
    location_id BIGINT,
    CONSTRAINT fk_user_location_user
        FOREIGN KEY (user_id)
        REFERENCES ${schemaName}.users(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_location_location
        FOREIGN KEY (location_id)
        REFERENCES ${schemaName}.location(location_id)
        ON DELETE CASCADE
);

-- ===========================================================
-- Table: secondary_details
-- ===========================================================
--changeset system:create-secondary-details
CREATE TABLE IF NOT EXISTS ${schemaName}.secondary_details (
    id VARCHAR(50) PRIMARY KEY,
    secondary_user_name VARCHAR(255) NOT NULL,
    mobile VARCHAR(10) NOT NULL,
    email VARCHAR(255),
    relation VARCHAR(255) NOT NULL,
    user_id VARCHAR(20) NOT NULL UNIQUE,
    CONSTRAINT fk_secondary_user
        FOREIGN KEY (user_id)
        REFERENCES ${schemaName}.users(user_id)
        ON DELETE CASCADE
);

-- ===========================================================
-- Table: timesheet_status
-- ===========================================================
--changeset system:create-timesheet-status
CREATE TABLE IF NOT EXISTS ${schemaName}.timesheet_status (
    status_id VARCHAR(50) PRIMARY KEY,
    status_name VARCHAR(255),
    is_editable BOOLEAN NOT NULL DEFAULT FALSE
);

--changeset system:insert-timesheet-status
INSERT INTO ${schemaName}.timesheet_status (status_id, status_name)
SELECT * FROM (VALUES
    ('TSS001', 'Present'),
    ('TSS002', 'Absent'),
    ('TSS003', 'Paid Leave'),
    ('TSS004', 'Not Marked'),
    ('TSS005', 'Holiday'),
    ('TSS006', 'Half Day'),
    ('TSS007', 'Permission')
) AS tmp(status_id, status_name)
WHERE NOT EXISTS (SELECT 1 FROM ${schemaName}.timesheet_status);

-- ===========================================================
-- Table: timesheet
-- ===========================================================
--changeset system:create-timesheet
CREATE TABLE IF NOT EXISTS ${schemaName}.timesheet (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(20),
    date DATE,
    first_clock_in TIME,
    last_clock_out TIME,
    tracked_hours TIME,
    regular_hours TIME,
    total_break_hours TIME,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    status_id VARCHAR(50),
    CONSTRAINT fk_timesheet_user
        FOREIGN KEY (user_id)
        REFERENCES ${schemaName}.users(user_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_timesheet_status
        FOREIGN KEY (status_id)
        REFERENCES ${schemaName}.timesheet_status(status_id)
        ON DELETE SET NULL
);

-- ===========================================================
-- Table: timesheet_history
-- ===========================================================
--changeset system:create-timesheet-history
CREATE TABLE IF NOT EXISTS ${schemaName}.timesheet_history (
    id BIGSERIAL PRIMARY KEY,
    timesheet_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    log_time TIME,
    log_type VARCHAR(50),
    log_from VARCHAR(50),
    logged_timestamp TIMESTAMP,
    CONSTRAINT fk_timesheet_history_timesheet
        FOREIGN KEY (timesheet_id)
        REFERENCES ${schemaName}.timesheet(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_timesheet_history_location
        FOREIGN KEY (location_id)
        REFERENCES ${schemaName}.location(location_id)
        ON DELETE SET NULL
);

-- ===========================================================
-- Table: blacklisted_tokens
-- ===========================================================
--changeset system:create-blacklisted-tokens
CREATE TABLE IF NOT EXISTS ${schemaName}.blacklisted_tokens (
    id BIGSERIAL PRIMARY KEY,
    token TEXT NOT NULL UNIQUE,
    user_id VARCHAR(20) NOT NULL,
    logged_out_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_blacklisted_user
        FOREIGN KEY (user_id)
        REFERENCES ${schemaName}.users(user_id)
        ON DELETE CASCADE
);

-- ===========================================================
-- Table: fixed_work_schedule
-- ===========================================================
--changeset system:create-fixed-work-schedule
CREATE TABLE IF NOT EXISTS ${schemaName}.fixed_work_schedule (
    fixed_work_schedule_id VARCHAR(50) PRIMARY KEY,
    day VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    duration DOUBLE PRECISION NOT NULL,
    work_schedule_id VARCHAR(50) NOT NULL,
    CONSTRAINT fk_fixed_work_schedule
        FOREIGN KEY (work_schedule_id)
        REFERENCES ${schemaName}.work_schedule(work_schedule_id)
        ON DELETE CASCADE
);

-- ===========================================================
-- Table: flexible_work_schedule
-- ===========================================================
--changeset system:create-flexible-work-schedule
CREATE TABLE IF NOT EXISTS ${schemaName}.flexible_work_schedule (
    flexible_work_schedule_id VARCHAR(50) PRIMARY KEY,
    day VARCHAR(20) NOT NULL,
    duration DOUBLE PRECISION NOT NULL,
    work_schedule_id VARCHAR(50) NOT NULL,
    CONSTRAINT fk_flexible_work_schedule
        FOREIGN KEY (work_schedule_id)
        REFERENCES ${schemaName}.work_schedule(work_schedule_id)
        ON DELETE CASCADE
);

-- ===========================================================
-- Table: weekly_work_schedule
-- ===========================================================
--changeset system:create-weekly-work-schedule
CREATE TABLE IF NOT EXISTS ${schemaName}.weekly_work_schedule (
    weekly_work_schedule_id VARCHAR(50) PRIMARY KEY,
    duration DOUBLE PRECISION NOT NULL,
    start_day VARCHAR(20) NOT NULL,
    end_day VARCHAR(20) NOT NULL,
    work_schedule_id VARCHAR(50) NOT NULL UNIQUE,
    CONSTRAINT fk_weekly_work_schedule
        FOREIGN KEY (work_schedule_id)
        REFERENCES ${schemaName}.work_schedule(work_schedule_id)
        ON DELETE CASCADE
);

-- ===========================================================
-- Table: org_user_sequence
-- ===========================================================
--changeset system:create-org-user-sequence
CREATE TABLE IF NOT EXISTS ${schemaName}.org_user_sequence (
    org_id VARCHAR(20) PRIMARY KEY,
    last_user_id INTEGER NOT NULL,
    last_secondary_user_id INTEGER,
    last_subscription_id INTEGER,
    last_payment_id INTEGER
);

-- ===========================================================
-- Table: user_embedding
-- ===========================================================
--changeset system:create-user-embedding
CREATE TABLE IF NOT EXISTS ${schemaName}.user_embedding (
    face_id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL UNIQUE,
    embeddings TEXT
);

-- ===========================================================
-- Table: subscription
-- ===========================================================
--changeset system:create-subscription
CREATE TABLE subscription (
    subscription_id VARCHAR(20) PRIMARY KEY,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    organization_id VARCHAR(20) NOT NULL,
    plan_id VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    subscribed_users INT NOT NULL,
    schema_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_subscription_plan FOREIGN KEY (plan_id)
        REFERENCES plan(plan_id)
        ON DELETE RESTRICT
);

-- ===========================================================
-- Table: payment
-- ===========================================================
--changeset system:create-payment
CREATE TABLE payment (
    payment_id VARCHAR(10) PRIMARY KEY,
    subscription_id VARCHAR(10) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    billing_period VARCHAR(50) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    schema_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_payment_subscription FOREIGN KEY (subscription_id)
        REFERENCES subscription(subscription_id)
        ON DELETE CASCADE
);

-- ===========================================================
-- Table: user_history
-- ===========================================================
--changeset system:create-user_history

CREATE TABLE user_history (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL,
    active_status VARCHAR(20) NOT NULL,
    is_updated BOOLEAN NOT NULL DEFAULT FALSE,
    comments TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
