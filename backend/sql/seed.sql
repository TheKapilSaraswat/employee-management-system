-- seed.sql — optional reference data for Employee Management System.
-- Applied with backend/scripts/seed.sh. Passwords below are BCrypt hashes of
-- "ChangeMe123!" (placeholder). The app seeds the ADMIN user automatically from
-- ADMIN_EMAIL / ADMIN_PASSWORD on first start, so this is only needed for demo
-- departments/employees.

INSERT INTO departments (id, name, description, created_at)
VALUES
    ('dept-eng', 'Engineering', 'Software development and infrastructure', NOW()),
    ('dept-hr',  'Human Resources', 'People operations and hiring', NOW()),
    ('dept-sales', 'Sales', 'Revenue and account management', NOW())
ON CONFLICT (id) DO NOTHING;

-- Leave this commented until you have real BCrypt hashes to insert:
-- INSERT INTO users (id, email, password, name, role, created_at) VALUES
--     ('admin-demo', 'admin@example.com', '$2a$10$PLACEHOLDER_HASH', 'Demo Admin', 'ADMIN', NOW());
