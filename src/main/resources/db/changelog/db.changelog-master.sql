CREATE SEQUENCE IF NOT EXISTS org_id_seq START WITH 001 INCREMENT BY 1;

--Table: Organization Type
CREATE TABLE IF NOT EXISTS org_type (
    org_type_id VARCHAR(20) PRIMARY KEY,
    org_type_name VARCHAR(255)
);

--Table: Organization Type - Default Data
INSERT INTO org_type (org_type_id, org_type_name)
SELECT * FROM (VALUES
    ('ORGTY001', 'Academic'),
    ('ORGTY002', 'Commercial')
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM org_type);

--Table:Organization
CREATE TABLE IF NOT EXISTS public.organization (
    organization_id VARCHAR(20) PRIMARY KEY,
    org_name VARCHAR(100) NOT NULL,
    org_type VARCHAR(10) REFERENCES org_type(org_type_id),
    org_size INTEGER NOT NULL,
    country VARCHAR(100) NOT NULL,
    schema_name VARCHAR(255) NOT NULL UNIQUE,
    time_zone VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

--Table: User_Org Map
CREATE TABLE IF NOT EXISTS public.user_schema_mapping (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    mobile_number VARCHAR(20) UNIQUE NOT NULL,
    org_id VARCHAR(20) NOT NULL,
    schema_name VARCHAR(255) NOT NULL,
    CONSTRAINT fk_schema_org FOREIGN KEY (org_id)
        REFERENCES public.organization(organization_id)
    );

--Table : Plan
CREATE TABLE IF NOT EXISTS public.plan (
    plan_id VARCHAR(10) PRIMARY KEY,
    plan_name VARCHAR(255) NOT NULL,
    price_per_user NUMERIC(10, 2) NOT NULL,
    billing_cycle VARCHAR(50) NOT NULL,
    is_default BOOLEAN NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

--Table : Plan - Default Data
INSERT INTO plan (plan_id, plan_name, price_per_user, billing_cycle, is_default)
VALUES
('PL01', 'Basic plan', 0.00, 'basic','true'),
('PL02', 'Essential plan', 12.00, '12 month','false'),
('PL03', 'Standard plan', 11.00, '24 month','false'),
('PL04', 'Premium plan', 9.00, '36 month','false')
ON CONFLICT (plan_id) DO NOTHING;

--Table : country
CREATE TABLE IF NOT EXISTS public.country (
    id VARCHAR(10) PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL
);

--Table : country - Default Countries
INSERT INTO country(id, code, name)
VALUES
('CT001','IN','India')
ON CONFLICT (id) DO NOTHING;

--Table: public_holiday
CREATE TABLE IF NOT EXISTS public.public_holiday (
    id VARCHAR(10) PRIMARY KEY,
    date DATE,
    name VARCHAR(255),
    year VARCHAR(10),
    country_code VARCHAR(10) NOT NULL,
    CONSTRAINT fk_public_holiday_country
        FOREIGN KEY (country_code) REFERENCES country (code)
        ON DELETE CASCADE
);
