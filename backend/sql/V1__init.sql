-- V1__init.sql — Employee Management System schema for PostgreSQL.
-- Applied manually with backend/scripts/migrate.sh (or psql) before first start.

CREATE TABLE IF NOT EXISTS users (
    id          VARCHAR(64) PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    name        VARCHAR(255),
    role        VARCHAR(50) DEFAULT 'EMPLOYEE',
    created_at  TIMESTAMP
);

CREATE TABLE IF NOT EXISTS departments (
    id          VARCHAR(64) PRIMARY KEY,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(1000),
    created_at  TIMESTAMP
);

CREATE TABLE IF NOT EXISTS employees (
    id            VARCHAR(64) PRIMARY KEY,
    employee_code VARCHAR(100) UNIQUE,
    first_name    VARCHAR(255) NOT NULL,
    last_name     VARCHAR(255),
    email         VARCHAR(255),
    phone         VARCHAR(50),
    department_id VARCHAR(64) REFERENCES departments(id),
    position      VARCHAR(255),
    salary        NUMERIC(15, 2),
    joining_date  DATE,
    status        VARCHAR(50) DEFAULT 'active',
    created_at    TIMESTAMP
);

CREATE TABLE IF NOT EXISTS attendances (
    id          VARCHAR(64) PRIMARY KEY,
    employee_id VARCHAR(64) NOT NULL REFERENCES employees(id),
    date        DATE NOT NULL,
    check_in    TIMESTAMP,
    check_out   TIMESTAMP,
    status      VARCHAR(50) DEFAULT 'present',
    notes       VARCHAR(1000),
    UNIQUE (employee_id, date)
);

CREATE TABLE IF NOT EXISTS leaves (
    id          VARCHAR(64) PRIMARY KEY,
    employee_id VARCHAR(64) NOT NULL REFERENCES employees(id),
    type        VARCHAR(50),
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    status      VARCHAR(50) DEFAULT 'pending',
    reason      VARCHAR(1000),
    approved_by VARCHAR(64),
    created_at  TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payrolls (
    id           VARCHAR(64) PRIMARY KEY,
    employee_id  VARCHAR(64) NOT NULL REFERENCES employees(id),
    pay_month    INT NOT NULL,
    pay_year     INT NOT NULL,
    base_salary  NUMERIC(15, 2),
    bonus        NUMERIC(15, 2),
    deductions   NUMERIC(15, 2),
    net_salary   NUMERIC(15, 2),
    status       VARCHAR(50) DEFAULT 'pending',
    processed_at TIMESTAMP,
    UNIQUE (employee_id, pay_month, pay_year)
);

CREATE INDEX IF NOT EXISTS idx_employees_department ON employees(department_id);
CREATE INDEX IF NOT EXISTS idx_attendances_date ON attendances(date);
CREATE INDEX IF NOT EXISTS idx_leaves_status ON leaves(status);
CREATE INDEX IF NOT EXISTS idx_payrolls_period ON payrolls(pay_year, pay_month);
