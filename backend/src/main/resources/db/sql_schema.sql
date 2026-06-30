-- gen_random_uuid() ist seit PG13 im Kern, diese Zeile ist nur zur Sicherheit
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1. employees ZUERST, weil users darauf verweist
CREATE TABLE employees (
    id               uuid    PRIMARY KEY DEFAULT gen_random_uuid(),
    personnel_number varchar UNIQUE,
    name             varchar NOT NULL,
    email            varchar UNIQUE,
    phone            varchar UNIQUE,
    address          text,
    department       varchar NOT NULL
                         CHECK (department IN ('MARKETING', 'ACCOUNTING', 'HR', 'DEVELOPMENT', 'MANAGEMENT')),
    active           boolean NOT NULL DEFAULT true,
    notes            text,
    image_url        varchar
);

-- 2. users (verweist auf employees)
CREATE TABLE users (
    id                 uuid       PRIMARY KEY DEFAULT gen_random_uuid(),
    github_id          varchar    NOT NULL UNIQUE,
    employee_id        uuid       UNIQUE REFERENCES employees(id) ON DELETE SET NULL,
    username           varchar    NOT NULL UNIQUE,
    name               varchar    NOT NULL,
    avatar_url         varchar,
    github_url         varchar,
    role               varchar    NOT NULL DEFAULT 'VIEWER'
                                      CHECK (role IN ('VIEWER', 'USER', 'ADMIN')),
    preferred_language varchar(2) DEFAULT 'de',
    created_at         timestamp  NOT NULL DEFAULT now(),
    last_login_at      timestamp
);

-- 3. locations
CREATE TABLE locations (
    id      uuid    PRIMARY KEY DEFAULT gen_random_uuid(),
    name    varchar NOT NULL UNIQUE,
    address text,
    phone   varchar,
    email   varchar,
    notes   text
);

-- 4. devices (verweist auf locations)
CREATE TABLE devices (
    id               uuid    PRIMARY KEY DEFAULT gen_random_uuid(),
    type             varchar NOT NULL
                         CHECK (type IN ('LAPTOP', 'PHONE', 'TABLET', 'MONITOR', 'ACCESSORY', 'OTHER')),
    manufacturer     varchar,
    model_name       varchar,
    serial_number    varchar UNIQUE,
    inventory_number varchar UNIQUE,
    purchase_date    date,
    status           varchar NOT NULL DEFAULT 'AVAILABLE'
                         CHECK (status IN ('AVAILABLE', 'ASSIGNED', 'IN_REPAIR', 'RETIRED')),
    defective        boolean NOT NULL DEFAULT false,
    location_id      uuid    REFERENCES locations(id),
    notes            text
);

-- 5. assignments (verweist auf devices + employees)
CREATE TABLE assignments (
    id                           uuid    PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id                    uuid    NOT NULL REFERENCES devices(id),
    employee_id                  uuid    NOT NULL REFERENCES employees(id),
    handed_out_by                uuid    REFERENCES employees(id) ON DELETE SET NULL,
    assigned_date                date    NOT NULL,
    returned_date                date,
    condition_out                text,
    condition_in                 text,
    notes                        text,
    copy_handed_to_employee      boolean NOT NULL DEFAULT false,
    copy_filed_in_personnel_file boolean NOT NULL DEFAULT false
);

-- 6. device_files (verweist auf devices)
CREATE TABLE device_files (
    id          uuid      PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id   uuid      NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    file_url    varchar   NOT NULL,
    file_type   varchar,
    uploaded_at timestamp NOT NULL DEFAULT now()
);

-- 7. assignment_files (verweist auf assignments)
CREATE TABLE assignment_files (
    id            uuid      PRIMARY KEY DEFAULT gen_random_uuid(),
    assignment_id uuid      NOT NULL REFERENCES assignments(id) ON DELETE CASCADE,
    file_url      varchar   NOT NULL,
    file_type     varchar,
    uploaded_at   timestamp NOT NULL DEFAULT now()
);

-- Indizes für die Historien- und Datei-Abfragen
CREATE INDEX idx_assignments_device          ON assignments(device_id);
CREATE INDEX idx_assignments_employee        ON assignments(employee_id);
CREATE INDEX idx_device_files_device         ON device_files(device_id);
CREATE INDEX idx_assignment_files_assignment ON assignment_files(assignment_id);