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
    split_time TIME NOT NULL,
    is_auto_clock_out BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_work_schedule_type
        FOREIGN KEY (work_schedule_type)
        REFERENCES ${schemaName}.work_schedule_type(type_id),
    CONSTRAINT fk_organization
        FOREIGN KEY (organization_id)
        REFERENCES public.organization(organization_id)
);

-- ===========================================================
-- Table: calendar
-- ===========================================================
--changeset system:create-calendar-table
CREATE TABLE IF NOT EXISTS calendar (
    id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(255),
    is_default BOOLEAN,
    is_active BOOLEAN,
    country_id VARCHAR(10),
        CONSTRAINT fk_country
           FOREIGN KEY (country_id)
           REFERENCES public.country(id)
);

-- ===========================================================
-- Table: calendar_details
-- ===========================================================
--changeset system:create-calendar-details-table
CREATE TABLE IF NOT EXISTS calendar_details (
    id VARCHAR(10) PRIMARY KEY,
    date DATE,
    name VARCHAR(255),
    year VARCHAR(10),
    calendar_id VARCHAR(10) NOT NULL,
    CONSTRAINT fk_calendar_details_calendar
        FOREIGN KEY (calendar_id) REFERENCES calendar (id)
        ON DELETE CASCADE
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
    calendar_id VARCHAR(20) ,
    CONSTRAINT fk_role
        FOREIGN KEY (role_id)
        REFERENCES ${schemaName}.role(role_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_work_schedule
        FOREIGN KEY (work_schedule_id)
        REFERENCES ${schemaName}.work_schedule(work_schedule_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_calendar
            FOREIGN KEY (calendar_id)
            REFERENCES ${schemaName}.calendar(id)
            ON DELETE RESTRICT
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
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
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
    start_time_duration TIME,
    end_time_duration TIME,
    total_over_time TIME,
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
    last_payment_id INTEGER,
    last_payroll_id VARCHAR(20)
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
-- Table: payroll
-- ===========================================================
--changeset system:create-payroll
CREATE TABLE IF NOT EXISTS ${schemaName}.payroll (
    id VARCHAR(255) PRIMARY KEY,
    payroll_name VARCHAR(255) NOT NULL,
    yearly_salary NUMERIC(19,2) NOT NULL,
    monthly_salary NUMERIC(19,2) NOT NULL,
    pf NUMERIC(19,2) NOT NULL,
    others NUMERIC(19,2) NOT NULL,
    overtime_amount NUMERIC(19,2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

--===========================================================
-- Table: payroll_history
-- ===========================================================
--changeset system:create-payroll-history
CREATE TABLE IF NOT EXISTS ${schemaName}.payroll_history (
    id BIGSERIAL PRIMARY KEY,
    action_at TIMESTAMP NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    action_by VARCHAR(100) NOT NULL,
    payroll_id VARCHAR(50) NOT NULL,

    CONSTRAINT fk_payroll_history
        FOREIGN KEY (payroll_id)
        REFERENCES ${schemaName}.payroll(id)
        ON DELETE CASCADE
);

-- ===========================================================
-- Table: payroll_setting
-- ===========================================================
--changeset system:create-payroll-setting
CREATE TABLE IF NOT EXISTS ${schemaName}.payroll_settings (
    id BIGSERIAL PRIMARY KEY,
    payroll_calculation VARCHAR(100) NOT NULL,
    is_overtime BOOLEAN NOT NULL
);

--changeset system:insert-payroll-setting
INSERT INTO ${schemaName}.payroll_settings (id, payroll_calculation, is_overtime)
SELECT * FROM (VALUES
    (1, 'DAYS', false)
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM ${schemaName}.payroll_settings);

--===========================================================
-- Table: user_payroll
-- ===========================================================
--changeset system:create-user-payroll
CREATE TABLE IF NOT EXISTS ${schemaName}.user_payroll (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL,
    payroll_id VARCHAR(50) NOT NULL,

    CONSTRAINT fk_users
        FOREIGN KEY (user_id)
        REFERENCES ${schemaName}.users(user_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_payroll
        FOREIGN KEY (payroll_id)
        REFERENCES ${schemaName}.payroll(id)
        ON DELETE CASCADE
);

-- ===========================================================
-- Table: user_payroll_amount
-- ===========================================================
--changeset system:create-user-payroll-amount
CREATE TABLE IF NOT EXISTS ${schemaName}.user_payroll_amount (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL,
    payroll_id VARCHAR(50) NOT NULL,
    month VARCHAR(20) NOT NULL,
    unpaid_leave_deduction NUMERIC(19,2) NOT NULL,
    regular_hrs NUMERIC(19,2) NOT NULL,
    regular_days INTEGER NOT NULL,
    overtime_hrs NUMERIC(19,2) NOT NULL,
    total_hrs NUMERIC(19,2) NOT NULL,
    regular_payroll_amount NUMERIC(19,2) NOT NULL,
    overtime_payroll_amount NUMERIC(19,2) NOT NULL,
    total_payroll_amount NUMERIC(19,2) NOT NULL,
    total_amount NUMERIC(19,2),
    payroll_status VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    notes VARCHAR(500),
        CONSTRAINT fk_users
        FOREIGN KEY (user_id)
        REFERENCES ${schemaName}.users(user_id)
        ON DELETE SET NULL,
        CONSTRAINT fk_payroll
        FOREIGN KEY (payroll_id)
        REFERENCES ${schemaName}.payroll(id)
        ON DELETE CASCADE
);

-- ===========================================================
-- Table: user_payroll_history
-- ===========================================================
--changeset system:create-user-payroll-history

CREATE TABLE IF NOT EXISTS ${schemaName}.user_payroll_history (
    id BIGSERIAL PRIMARY KEY,
    action_at TIMESTAMP,
    action_type VARCHAR(100),
    action_by VARCHAR(100),
    user_id VARCHAR(20) NOT NULL,
    user_payroll_amount_id BIGINT NOT NULL,

    CONSTRAINT fk_user_payroll_history_amount
        FOREIGN KEY (user_payroll_amount_id)
        REFERENCES ${schemaName}.user_payroll_amount(id)
        ON DELETE CASCADE,

   CONSTRAINT fk_user_history_user
         FOREIGN KEY (user_id)
         REFERENCES ${schemaName}.users(user_id)
         ON DELETE CASCADE
);

-- ===========================================================
-- Table: payment
-- ===========================================================
--changeset system:create-payment
CREATE TABLE payment (
    payment_id VARCHAR(20) PRIMARY KEY,
    order_id VARCHAR(30) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    billing_period VARCHAR(50) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    schema_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
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
-- Table: subscription_mapping
-- ===========================================================
--changeset system:create-subscription_mapping
CREATE TABLE subscription_mapping (
    subscription_mapping_id SERIAL PRIMARY KEY,
    subscription_id VARCHAR(20) NOT NULL,
    payment_id VARCHAR(20) NOT NULL,

    CONSTRAINT fk_mapping_subscription FOREIGN KEY (subscription_id)
        REFERENCES subscription(subscription_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_mapping_payment FOREIGN KEY (payment_id)
        REFERENCES payment(payment_id)
        ON DELETE CASCADE
);

-- ===========================================================
-- Table: timeoff_policies
-- ===========================================================
--changeset system:create-timeoff-policies-table
CREATE TABLE IF NOT EXISTS timeoff_policies (
    policy_id VARCHAR(20) PRIMARY KEY,
    policy_name VARCHAR(255) NOT NULL,
    compensation VARCHAR(10) CHECK (compensation IN ('PAID','UNPAID')),
    accrual_type VARCHAR(10) CHECK (accrual_type IN ('MONTHLY','ANNUALLY')),
    validity_start_date DATE,
    validity_end_date DATE,
    accrual_start_date DATE,
    reset_frequency VARCHAR(10) CHECK (reset_frequency IN ('MONTHLY','ANNUALLY')),
    entitled_units INT,
    entitled_hours INT,
    entitled_type VARCHAR(10) CHECK (entitled_type IN ('DAY','HOURS','HALF_DAY')),
    is_active BOOLEAN,
    max_carry_forward_units INT,
    is_carry_forward BOOLEAN,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================================================
-- Table: user_policies
-- ===========================================================
--changeset system:create-user-policies-table
CREATE TABLE IF NOT EXISTS user_policies (
    id BIGSERIAL PRIMARY KEY,
    policy_id VARCHAR(20) NOT NULL,
    user_id VARCHAR(20) NOT NULL,
    valid_from DATE,
    valid_to DATE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_policies_policy
        FOREIGN KEY (policy_id) REFERENCES timeoff_policies(policy_id)
        ON DELETE CASCADE,

           CONSTRAINT fk_user_policies_user
                FOREIGN KEY (user_id)
                REFERENCES users(user_id)
                ON DELETE CASCADE
);

-- ===========================================================
-- Table: timeoff_request
-- ===========================================================
-- changeset system:create-timeoff-request-table
CREATE TABLE IF NOT EXISTS timeoff_request (
    timeoff_request_id BIGSERIAL PRIMARY KEY,
    policy_id VARCHAR(20) NOT NULL,
    user_id VARCHAR(20) NOT NULL,
    request_date DATE NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    units_requested INT,
    hours_requested INT,
    status VARCHAR(20) CHECK (status IN ('APPROVED','PENDING','REJECTED','CANCELLED')),
    reason VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_timeoff_request_policy
        FOREIGN KEY (policy_id) REFERENCES timeoff_policies(policy_id)
        ON DELETE CASCADE
);

-- ===========================================================
-- Table: users_request_mapping
-- ===========================================================
-- changeset system:create-users-request-mapping-table
CREATE TABLE IF NOT EXISTS ${schemaName}.users_request_mapping (
    id BIGSERIAL PRIMARY KEY,
    viewer_id VARCHAR(20) NOT NULL,
    requester_id VARCHAR(20) NOT NULL,
    timeoff_request_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL
);

-- ===========================================================
-- Table: timeoff_request_history
-- ===========================================================
-- changeset system:create-timeoff-request-history-table
CREATE TABLE IF NOT EXISTS timeoff_request_history (
    id BIGSERIAL PRIMARY KEY,
    timeoff_request_id BIGINT NOT NULL,
    user_id VARCHAR(20) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    action_by VARCHAR(50) NOT NULL,
    action_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_request
        FOREIGN KEY (timeoff_request_id) REFERENCES timeoff_request(timeoff_request_id)
        ON DELETE CASCADE
);

-- ===========================================================
-- Table: leave_balance
-- ===========================================================
--changeset system:create-leave-balance-table
CREATE TABLE IF NOT EXISTS leave_balance (
    leave_balance_id BIGSERIAL PRIMARY KEY,
    policy_id VARCHAR(20) NOT NULL,
    user_id VARCHAR(20) NOT NULL,
    period_start_date DATE,
    period_end DATE,
    total_units DOUBLE PRECISION,
    expired_units DOUBLE PRECISION DEFAULT 0.0,
    leave_taken_units DOUBLE PRECISION DEFAULT 0.0,
    balance_units DOUBLE PRECISION,
    next_accrual_date DATE,
    last_accrual_date DATE,
    carry_forward_units DOUBLE PRECISION DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_leave_balance_policy
        FOREIGN KEY (policy_id) REFERENCES timeoff_policies(policy_id)
        ON DELETE CASCADE,
   CONSTRAINT fk_leave_balance_user
            FOREIGN KEY (user_id) REFERENCES users(user_id)
            ON DELETE CASCADE

);

-- ===========================================================
-- Table: monthly_accrual_tracking
-- ===========================================================
--changeset system:create-monthly-summary-table
CREATE TABLE IF NOT EXISTS monthly_summary (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL,
    policy_id VARCHAR(20) NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    total_leaves_taken INT DEFAULT 0,
    paid_leaves_taken INT DEFAULT 0,
    unpaid_leaves_taken INT DEFAULT 0,
    total_units_available INT DEFAULT 0,
    balance_units INT DEFAULT 0,
    half_day_units INT DEFAULT 0,
    full_day_units INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_monthly_user_summary_policy_id
        FOREIGN KEY (policy_id) REFERENCES timeoff_policies(policy_id)
        ON DELETE CASCADE

);

-- ===========================================================
-- Indexes for timesheet queries (fetchMainTimesheets)
-- ===========================================================

--changeset system:create-timesheet-date-user-index
CREATE INDEX IF NOT EXISTS idx_timesheet_date_user
    ON ${schemaName}.timesheet (date, user_id);

--changeset system:create-timesheet-status-index
CREATE INDEX IF NOT EXISTS idx_timesheet_status
    ON ${schemaName}.timesheet (status_id);

--changeset system:create-users-doj-index
CREATE INDEX IF NOT EXISTS idx_users_date_of_joining
    ON ${schemaName}.users (date_of_joining);

--changeset system:create-users-work-schedule-index
CREATE INDEX IF NOT EXISTS idx_users_work_schedule
    ON ${schemaName}.users (work_schedule_id);

--changeset system:create-users-userid-index
CREATE INDEX IF NOT EXISTS idx_users_userid
    ON ${schemaName}.users (user_id);

-- ===========================================================
-- Indexes for user group queries (fetchUserGroups)
-- ===========================================================

--changeset system:create-user-group-userid-index
CREATE INDEX IF NOT EXISTS idx_user_group_userid
    ON ${schemaName}.user_group (user_id);

--changeset system:create-org-groups-groupid-index
CREATE INDEX IF NOT EXISTS idx_org_groups_groupid
    ON ${schemaName}.org_groups (group_id);

-- ===========================================================
-- Indexes for timesheet history queries (fetchTimesheetHistory)
-- ===========================================================

--changeset system:create-timesheet-history-timesheetid-logtime
CREATE INDEX IF NOT EXISTS idx_timesheet_history_timesheetid_logtime
    ON ${schemaName}.timesheet_history (timesheet_id, log_time);

--changeset system:create-timesheet-history-locationid
CREATE INDEX IF NOT EXISTS idx_timesheet_history_locationid
    ON ${schemaName}.timesheet_history (location_id);

-- ===========================================================
-- Indexes for WorkSchedule queries (findAllSchedulesWithUsers)
-- ===========================================================

--changeset system:create-work-schedule-is-active-index
CREATE INDEX IF NOT EXISTS idx_work_schedule_is_active
    ON ${schemaName}.work_schedule (is_active);

--changeset system:create-work-schedule-id-index
CREATE INDEX IF NOT EXISTS idx_work_schedule_id
    ON ${schemaName}.work_schedule (work_schedule_id);

--changeset system:create-fixed-work-schedule-work-schedule-id-index
CREATE INDEX IF NOT EXISTS idx_fixed_work_schedule_ws_id
    ON ${schemaName}.fixed_work_schedule (work_schedule_id);

--changeset system:create-flexible-work-schedule-work-schedule-id-index
CREATE INDEX IF NOT EXISTS idx_flexible_work_schedule_ws_id
    ON ${schemaName}.flexible_work_schedule (work_schedule_id);

--changeset system:create-weekly-work-schedule-work-schedule-id-index
CREATE INDEX IF NOT EXISTS idx_weekly_work_schedule_ws_id
    ON ${schemaName}.weekly_work_schedule (work_schedule_id);

--changeset system:create-fetch-main-timesheets-function endDelimiter://
CREATE OR REPLACE FUNCTION ${schemaName}.fetch_main_timesheets(
    p_start_date DATE,
    p_end_date DATE DEFAULT NULL,
    p_user_ids VARCHAR[] DEFAULT NULL
)
RETURNS TABLE (
    id BIGINT,
    date DATE,
    userId VARCHAR,
    userName VARCHAR,
    mobileNumber VARCHAR,
    roleName VARCHAR,
    workScheduleName VARCHAR,
    firstClockIn TIME,
    lastClockOut TIME,
    trackedHours TIME,
    regularHours TIME,
    start_time_duration TIME,
    end_time_duration TIME,
    total_over_time TIME,
    status VARCHAR
)
LANGUAGE plpgsql
AS $BODY$
BEGIN
    RETURN QUERY
    SELECT
        t.id,
        t.date,
        u.user_id,
        u.user_name,
        u.mobile_number,
        r.name,
        ws.work_schedule_name,
        t.first_clock_in,
        t.last_clock_out,
        t.tracked_hours,
        t.regular_hours,
        t.start_time_duration,
        t.end_time_duration,
        t.total_over_time,
        ts.status_name
    FROM ${schemaName}.timesheet t
    JOIN ${schemaName}.users u ON t.user_id = u.user_id
    LEFT JOIN ${schemaName}.role r ON u.role_id = r.role_id
    LEFT JOIN ${schemaName}.work_schedule ws ON u.work_schedule_id = ws.work_schedule_id
    LEFT JOIN ${schemaName}.timesheet_status ts ON t.status_id = ts.status_id
    WHERE t.date BETWEEN p_start_date AND COALESCE(p_end_date, CURRENT_DATE)
      AND (p_user_ids IS NULL OR array_length(p_user_ids, 1) = 0 OR u.user_id = ANY(p_user_ids))
    ORDER BY t.date ASC;
END;
$BODY$;

--changeset system:create-fetch_timesheet_history endDelimiter://
CREATE OR REPLACE FUNCTION ${schemaName}.fetch_timesheet_history(
    p_timesheet_ids BIGINT[]
)
RETURNS TABLE (
    timesheetId BIGINT,
    timesheetHistoryId BIGINT,
    locationName VARCHAR,
    logTime TIME,
    logType VARCHAR,
    logFrom VARCHAR
)
LANGUAGE plpgsql
AS $func$
BEGIN
    RETURN QUERY
    SELECT
        th.timesheet_id,
        th.id,
        loc.name,
        th.log_time,
        th.log_type,
        th.log_from
    FROM ${schemaName}.timesheet_history th
    LEFT JOIN ${schemaName}.location loc ON th.location_id = loc.location_id
    WHERE th.timesheet_id = ANY(p_timesheet_ids)
    ORDER BY th.log_time;
END;
$func$;

--changeset system:create-fetch-user_groups endDelimiter://
CREATE OR REPLACE FUNCTION ${schemaName}.fetch_user_groups(
    p_user_ids VARCHAR[]
)
RETURNS TABLE (
    userId VARCHAR,
    groupNames TEXT
)
LANGUAGE plpgsql
AS $func$
BEGIN
    RETURN QUERY
    SELECT
        u.user_id,
        STRING_AGG(DISTINCT g.group_name, ', ')
    FROM ${schemaName}.users u
    LEFT JOIN ${schemaName}.user_group ug ON u.user_id = ug.user_id
    LEFT JOIN ${schemaName}.org_groups g ON ug.group_id = g.group_id
    WHERE u.user_id = ANY(p_user_ids)
    GROUP BY u.user_id;
END;
$func$;

--changeset system:create-fetch_work_schedules_with_users endDelimiter://
CREATE OR REPLACE FUNCTION ${schemaName}.fetch_work_schedules_with_users(
    p_user_ids VARCHAR[]
)
RETURNS TABLE (
    work_schedule_id VARCHAR,
    work_schedule_name VARCHAR,
    is_default BOOLEAN,
    is_active BOOLEAN,
    work_schedule_type VARCHAR,
    split_time TIME,
    is_auto_clock_out BOOLEAN,
    organization_id VARCHAR
)
LANGUAGE plpgsql
AS $func$
BEGIN
    RETURN QUERY
    SELECT DISTINCT ws.work_schedule_id,
           ws.work_schedule_name,
           ws.is_default,
           ws.is_active,
           ws.work_schedule_type,
           ws.split_time,
           ws.is_auto_clock_out,
           ws.organization_id
    FROM ${schemaName}.work_schedule ws
    JOIN ${schemaName}.users u ON ws.work_schedule_id = u.work_schedule_id
    WHERE u.user_id = ANY(p_user_ids) AND ws.is_active = TRUE;
END;
$func$;

--changeset system:create-fetch_fixed_schedules_by_user_ids endDelimiter://
CREATE OR REPLACE FUNCTION ${schemaName}.fetch_fixed_schedules_by_user_ids(
    p_user_ids VARCHAR[]
)
RETURNS TABLE (
    fixed_work_schedule_id VARCHAR,
    day VARCHAR,
    start_time TIME,
    end_time TIME,
    duration DOUBLE PRECISION,
    work_schedule_id VARCHAR
)
LANGUAGE plpgsql
AS $func$
BEGIN
    RETURN QUERY
    SELECT f.fixed_work_schedule_id,
           f.day,
           f.start_time,
           f.end_time,
           f.duration,
           f.work_schedule_id
    FROM ${schemaName}.fixed_work_schedule f
    JOIN ${schemaName}.users u ON f.work_schedule_id = u.work_schedule_id
    WHERE u.user_id = ANY(p_user_ids);
END;
$func$;

--changeset system:create-fetch_flexible_schedules_by_user_ids endDelimiter://
CREATE OR REPLACE FUNCTION ${schemaName}.fetch_flexible_schedules_by_user_ids(
    p_user_ids VARCHAR[]
)
RETURNS TABLE (
    flexible_work_schedule_id VARCHAR,
    day VARCHAR,
    duration DOUBLE PRECISION,
    work_schedule_id VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT f.flexible_work_schedule_id,
           f.day,
           f.duration,
           f.work_schedule_id
    FROM ${schemaName}.flexible_work_schedule f
    JOIN ${schemaName}.users u ON f.work_schedule_id = u.work_schedule_id
    WHERE u.user_id = ANY(p_user_ids);
END;
$$;

--changeset system:create-fetch-main-timesheets-users endDelimiter://
CREATE OR REPLACE FUNCTION ${schemaName}.fetch_main_timesheets_users(
    p_start_date DATE,
    p_end_date DATE DEFAULT NULL,
    p_user_ids VARCHAR[] DEFAULT NULL
)
RETURNS TABLE (
        id BIGINT,
        userId VARCHAR,
        date DATE,
        userName VARCHAR,
        firstClockIn TIME,
        lastClockOut TIME,
        trackedHours TIME,
        regularHours TIME,
        status VARCHAR
)
LANGUAGE plpgsql
AS $BODY$
BEGIN
    RETURN QUERY
    SELECT
                t.id,
                u.user_id AS userId,
                t.date,
                u.user_name AS userName,
                t.first_clock_in AS firstClockIn,
                t.last_clock_out AS lastClockOut,
                t.tracked_hours AS trackedHours,
                t.regular_hours AS regularHours,
                ts.status_name AS status
    FROM ${schemaName}.timesheet t
    JOIN ${schemaName}.users u ON t.user_id = u.user_id
    LEFT JOIN ${schemaName}.timesheet_status ts ON t.status_id = ts.status_id
    WHERE t.date BETWEEN p_start_date AND COALESCE(p_end_date, CURRENT_DATE)
      AND (p_user_ids IS NULL OR array_length(p_user_ids, 1) = 0 OR u.user_id = ANY(p_user_ids))
    ORDER BY t.date ASC;
END;
$BODY$;

--changeset system:create-log-user-payroll-history-function endDelimiter://
CREATE OR REPLACE FUNCTION ${schemaName}.log_user_payroll_history()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO ${schemaName}.user_payroll_history (
            action_at,
            action_type,
            action_by,
            user_id,
            user_payroll_amount_id
        ) VALUES (
            NOW(),
            'CREATE',
            CURRENT_USER,
            NEW.user_id,
            NEW.id
        );

    ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO ${schemaName}.user_payroll_history (
            action_at,
            action_type,
            action_by,
            user_id,
            user_payroll_amount_id
        ) VALUES (
            NOW(),
            'UPDATE',
            CURRENT_USER,
            NEW.user_id,
            NEW.id
        );

    ELSIF (TG_OP = 'DELETE') THEN
        INSERT INTO ${schemaName}.user_payroll_history (
            action_at,
            action_type,
            action_by,
            user_id,
            user_payroll_amount_id
        ) VALUES (
            NOW(),
            'DELETE',
            CURRENT_USER,
            OLD.user_id,
            OLD.id
        );
    END IF;

    RETURN NULL; -- AFTER trigger
END;
$$ LANGUAGE plpgsql;

--changeset system:create-trg-user-payroll-history endDelimiter://
CREATE TRIGGER trg_user_payroll_history
AFTER INSERT OR UPDATE OR DELETE ON ${schemaName}.user_payroll_amount
FOR EACH ROW
EXECUTE FUNCTION ${schemaName}.log_user_payroll_history();

--changeset system:create-log-payroll-history-function endDelimiter://
CREATE OR REPLACE FUNCTION ${schemaName}.log_payroll_history()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO ${schemaName}.payroll_history (
            action_at,
            action_type,
            action_by,
            payroll_id
        ) VALUES (
            NOW(),
            'CREATE',
            CURRENT_USER,
            NEW.id
        );

    ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO ${schemaName}.payroll_history (
            action_at,
            action_type,
            action_by,
            payroll_id
        ) VALUES (
            NOW(),
            'UPDATE',
            CURRENT_USER,
            NEW.id
        );

    ELSIF (TG_OP = 'DELETE') THEN
        INSERT INTO ${schemaName}.payroll_history (
            action_at,
            action_type,
            action_by,
            payroll_id
        ) VALUES (
            NOW(),
            'DELETE',
            CURRENT_USER,
            OLD.id
        );
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

--changeset system:create-trg-payroll-history endDelimiter://
CREATE TRIGGER trg_payroll_history
AFTER INSERT OR UPDATE OR DELETE ON ${schemaName}.payroll
FOR EACH ROW
EXECUTE FUNCTION ${schemaName}.log_payroll_history();

--changeset system:create-log-timeoff-request-history-function endDelimiter://
CREATE OR REPLACE FUNCTION ${schemaName}.log_timeoff_request_history()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO ${schemaName}.timeoff_request_history (
            timeoff_request_id,
            user_id,
            action_type,
            action_by,
            action_at
        ) VALUES (
            NEW.timeoff_request_id,
            NEW.user_id,
            'CREATE',
            CURRENT_USER,
            NOW()
        );

    ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO ${schemaName}.timeoff_request_history (
            timeoff_request_id,
            user_id,
            action_type,
            action_by,
            action_at
        ) VALUES (
            NEW.timeoff_request_id,
            NEW.user_id,
            'UPDATE',
            CURRENT_USER,
            NOW()
        );

    ELSIF (TG_OP = 'DELETE') THEN
        INSERT INTO ${schemaName}.timeoff_request_history (
            timeoff_request_id,
            user_id,
            action_type,
            action_by,
            action_at
        ) VALUES (
            OLD.timeoff_request_id,
            OLD.user_id,
            'DELETE',
            CURRENT_USER,
            NOW()
        );
    END IF;

    RETURN NULL; -- AFTER trigger
END;
$$ LANGUAGE plpgsql;

--changeset system:create-trg-timeoff-request-history endDelimiter://
CREATE TRIGGER trg_timeoff_request_history
AFTER INSERT OR UPDATE OR DELETE ON ${schemaName}.timeoff_request
FOR EACH ROW
EXECUTE FUNCTION ${schemaName}.log_timeoff_request_history();


