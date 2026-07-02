---
-- #%L
-- Data Query AppJars - Demo
-- %%
-- Copyright (C) 2026 AppJars
-- %%
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--      http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- #L%
---
-- Sample tables queried by the seeded query definitions (see SPECIFICATION.md section 14.1).
-- This script is idempotent: it runs on every startup against the persistent H2 database.

CREATE TABLE IF NOT EXISTS demo_orders (
    id INT PRIMARY KEY,
    customer_name VARCHAR(100),
    region VARCHAR(50),
    product VARCHAR(100),
    quantity INT,
    unit_price DECIMAL(10,2),
    order_date DATE,
    status VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS demo_employees (
    id INT PRIMARY KEY,
    name VARCHAR(100),
    department VARCHAR(50),
    hire_date DATE,
    salary DECIMAL(10,2),
    manager_id INT
);

CREATE TABLE IF NOT EXISTS demo_products (
    id INT PRIMARY KEY,
    name VARCHAR(100),
    category VARCHAR(50),
    price DECIMAL(10,2),
    stock_quantity INT
);

CREATE TABLE IF NOT EXISTS demo_exchange_rates (
    currency VARCHAR(3) PRIMARY KEY,
    rate DECIMAL(10,4)
);

MERGE INTO demo_orders KEY(id) VALUES
    (1,  'Acme Corp',        'North', 'Laptop',    3, 1200.00, DATE '2025-01-08', 'SHIPPED'),
    (2,  'Globex',           'South', 'Monitor',   5,  280.00, DATE '2025-01-15', 'SHIPPED'),
    (3,  'Initech',          'East',  'Keyboard', 10,   45.50, DATE '2025-01-22', 'SHIPPED'),
    (4,  'Umbrella LLC',     'West',  'Laptop',    2, 1150.00, DATE '2025-02-03', 'SHIPPED'),
    (5,  'Acme Corp',        'North', 'Docking Station', 4, 199.99, DATE '2025-02-11', 'SHIPPED'),
    (6,  'Stark Industries', 'East',  'Monitor',   8,  310.00, DATE '2025-02-19', 'CANCELLED'),
    (7,  'Wayne Enterprises','North', 'Headset',  12,   89.90, DATE '2025-03-02', 'SHIPPED'),
    (8,  'Globex',           'South', 'Laptop',    1, 1350.00, DATE '2025-03-09', 'SHIPPED'),
    (9,  'Initech',          'East',  'Webcam',    6,   75.00, DATE '2025-03-17', 'SHIPPED'),
    (10, 'Hooli',            'West',  'Monitor',   3,  295.00, DATE '2025-03-25', 'SHIPPED'),
    (11, 'Acme Corp',        'North', 'Keyboard',  7,   48.00, DATE '2025-04-04', 'SHIPPED'),
    (12, 'Umbrella LLC',     'West',  'Headset',   9,   92.50, DATE '2025-04-12', 'PENDING'),
    (13, 'Stark Industries', 'East',  'Laptop',    5, 1275.00, DATE '2025-04-20', 'SHIPPED'),
    (14, 'Wayne Enterprises','North', 'Monitor',   4,  305.00, DATE '2025-05-01', 'SHIPPED'),
    (15, 'Globex',           'South', 'Docking Station', 6, 210.00, DATE '2025-05-09', 'SHIPPED'),
    (16, 'Hooli',            'West',  'Webcam',    8,   72.00, DATE '2025-05-18', 'SHIPPED'),
    (17, 'Initech',          'East',  'Headset',   3,   95.00, DATE '2025-05-27', 'CANCELLED'),
    (18, 'Acme Corp',        'North', 'Laptop',    2, 1225.00, DATE '2025-06-05', 'SHIPPED'),
    (19, 'Umbrella LLC',     'West',  'Keyboard', 15,   42.75, DATE '2025-06-14', 'SHIPPED'),
    (20, 'Stark Industries', 'East',  'Docking Station', 5, 205.00, DATE '2025-06-23', 'SHIPPED'),
    (21, 'Wayne Enterprises','North', 'Webcam',    4,   78.50, DATE '2025-07-02', 'SHIPPED'),
    (22, 'Globex',           'South', 'Monitor',   7,  288.00, DATE '2025-07-11', 'SHIPPED'),
    (23, 'Hooli',            'West',  'Laptop',    3, 1195.00, DATE '2025-07-20', 'PENDING'),
    (24, 'Initech',          'East',  'Keyboard',  9,   46.25, DATE '2025-07-29', 'SHIPPED'),
    (25, 'Acme Corp',        'North', 'Headset',   6,   88.00, DATE '2025-08-07', 'SHIPPED'),
    (26, 'Umbrella LLC',     'West',  'Monitor',   5,  299.00, DATE '2025-08-16', 'SHIPPED'),
    (27, 'Stark Industries', 'East',  'Webcam',   10,   70.00, DATE '2025-08-25', 'SHIPPED'),
    (28, 'Wayne Enterprises','North', 'Docking Station', 3, 215.00, DATE '2025-09-03', 'SHIPPED'),
    (29, 'Globex',           'South', 'Keyboard', 12,   44.00, DATE '2025-09-12', 'CANCELLED'),
    (30, 'Hooli',            'West',  'Headset',   5,   91.00, DATE '2025-09-21', 'SHIPPED'),
    (31, 'Initech',          'East',  'Laptop',    4, 1310.00, DATE '2025-09-30', 'SHIPPED'),
    (32, 'Acme Corp',        'North', 'Monitor',   6,  292.00, DATE '2025-10-09', 'SHIPPED'),
    (33, 'Umbrella LLC',     'West',  'Webcam',    7,   74.50, DATE '2025-10-18', 'SHIPPED'),
    (34, 'Stark Industries', 'East',  'Keyboard',  8,   47.00, DATE '2025-10-27', 'SHIPPED'),
    (35, 'Wayne Enterprises','North', 'Laptop',    1, 1400.00, DATE '2025-11-05', 'PENDING'),
    (36, 'Globex',           'South', 'Headset',  10,   86.50, DATE '2025-11-14', 'SHIPPED'),
    (37, 'Hooli',            'West',  'Docking Station', 4, 208.00, DATE '2025-11-23', 'SHIPPED'),
    (38, 'Initech',          'East',  'Monitor',   5,  301.00, DATE '2025-12-02', 'SHIPPED'),
    (39, 'Acme Corp',        'North', 'Webcam',    9,   76.00, DATE '2025-12-11', 'SHIPPED'),
    (40, 'Umbrella LLC',     'West',  'Laptop',    2, 1260.00, DATE '2025-12-20', 'SHIPPED'),
    (41, 'Stark Industries', 'East',  'Headset',   6,   93.00, DATE '2026-01-06', 'SHIPPED'),
    (42, 'Wayne Enterprises','North', 'Keyboard', 11,   45.00, DATE '2026-01-15', 'SHIPPED'),
    (43, 'Globex',           'South', 'Webcam',    5,   71.50, DATE '2026-01-24', 'SHIPPED'),
    (44, 'Hooli',            'West',  'Monitor',   6,  297.00, DATE '2026-02-02', 'CANCELLED'),
    (45, 'Initech',          'East',  'Docking Station', 7, 212.00, DATE '2026-02-11', 'SHIPPED'),
    (46, 'Acme Corp',        'North', 'Laptop',    3, 1280.00, DATE '2026-02-20', 'SHIPPED'),
    (47, 'Umbrella LLC',     'West',  'Headset',   8,   90.00, DATE '2026-03-01', 'SHIPPED'),
    (48, 'Stark Industries', 'East',  'Monitor',   4,  308.00, DATE '2026-03-10', 'SHIPPED'),
    (49, 'Wayne Enterprises','North', 'Webcam',    6,   77.00, DATE '2026-03-19', 'PENDING'),
    (50, 'Globex',           'South', 'Laptop',    2, 1330.00, DATE '2026-03-28', 'SHIPPED'),
    (51, 'Hooli',            'West',  'Keyboard', 10,   43.50, DATE '2026-04-06', 'SHIPPED'),
    (52, 'Initech',          'East',  'Headset',   4,   94.50, DATE '2026-04-15', 'SHIPPED'),
    (53, 'Acme Corp',        'North', 'Docking Station', 5, 218.00, DATE '2026-04-24', 'SHIPPED'),
    (54, 'Umbrella LLC',     'West',  'Monitor',   3,  290.00, DATE '2026-05-03', 'SHIPPED'),
    (55, 'Stark Industries', 'East',  'Webcam',    8,   73.00, DATE '2026-05-12', 'SHIPPED'),
    (56, 'Wayne Enterprises','North', 'Headset',   7,   87.50, DATE '2026-05-21', 'SHIPPED'),
    (57, 'Globex',           'South', 'Keyboard',  9,   46.75, DATE '2026-05-30', 'CANCELLED'),
    (58, 'Hooli',            'West',  'Laptop',    4, 1240.00, DATE '2026-06-08', 'SHIPPED'),
    (59, 'Initech',          'East',  'Monitor',   6,  303.00, DATE '2026-06-17', 'SHIPPED'),
    (60, 'Acme Corp',        'North', 'Webcam',    5,   79.00, DATE '2026-06-26', 'PENDING');

MERGE INTO demo_employees KEY(id) VALUES
    (1,  'Alice Johnson',  'Engineering', DATE '2018-03-12', 98000.00, NULL),
    (2,  'Bob Martinez',   'Engineering', DATE '2020-07-01', 82000.00, 1),
    (3,  'Carol Chen',     'Engineering', DATE '2021-11-15', 76500.00, 1),
    (4,  'Daniel Novak',   'Engineering', DATE '2023-02-20', 68000.00, 1),
    (5,  'Elena Rossi',    'Sales',       DATE '2017-06-05', 91000.00, NULL),
    (6,  'Frank Miller',   'Sales',       DATE '2019-09-23', 72000.00, 5),
    (7,  'Grace Kim',      'Sales',       DATE '2022-04-18', 64500.00, 5),
    (8,  'Hugo Alvarez',   'Marketing',   DATE '2019-01-07', 78000.00, NULL),
    (9,  'Irene Petrova',  'Marketing',   DATE '2021-08-30', 66000.00, 8),
    (10, 'James O''Brien', 'HR',          DATE '2016-10-11', 85000.00, NULL),
    (11, 'Karen Silva',    'HR',          DATE '2020-12-02', 61000.00, 10),
    (12, 'Luis Fernandez', 'Engineering', DATE '2024-05-27', 59500.00, 2);

MERGE INTO demo_exchange_rates KEY(currency) VALUES
    ('USD', 1.0000),
    ('EUR', 0.9200),
    ('GBP', 0.7900);

MERGE INTO demo_products KEY(id) VALUES
    (1, 'Laptop',          'Computers',   1250.00, 35),
    (2, 'Monitor',         'Displays',     295.00, 80),
    (3, 'Keyboard',        'Peripherals',   45.50, 150),
    (4, 'Headset',         'Peripherals',   89.90, 95),
    (5, 'Webcam',          'Peripherals',   74.50, 120),
    (6, 'Docking Station', 'Accessories',  210.00, 60),
    (7, 'Mouse',           'Peripherals',   29.90, 200),
    (8, 'USB-C Hub',       'Accessories',   59.00, 110);
