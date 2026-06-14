-- schema.sql
-- Database creation
CREATE DATABASE IF NOT EXISTS powerwatch;
USE powerwatch;

-- Drop tables in order of dependencies if they exist
DROP TABLE IF EXISTS outage_history;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS outage_reports;
DROP TABLE IF EXISTS technicians;
DROP TABLE IF EXISTS infrastructures;
DROP TABLE IF EXISTS users;

-- 1. users table
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);

-- 2. technicians table
CREATE TABLE technicians (
    technician_id INT AUTO_INCREMENT PRIMARY KEY,
    technician_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    team_name VARCHAR(50) NOT NULL,
    availability_status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
);

-- 3. outage_reports table
CREATE TABLE outage_reports (
    outage_id INT AUTO_INCREMENT PRIMARY KEY,
    area_name VARCHAR(100) NOT NULL,
    outage_type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    cause VARCHAR(100) NOT NULL,
    report_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estimated_restore_time TIMESTAMP NULL,
    actual_restore_time TIMESTAMP NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'REPORTED',
    assigned_technician_id INT NULL,
    FOREIGN KEY (assigned_technician_id) REFERENCES technicians(technician_id) ON DELETE SET NULL
);

-- 4. notifications table
CREATE TABLE notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    outage_id INT NULL,
    message TEXT NOT NULL,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (outage_id) REFERENCES outage_reports(outage_id) ON DELETE SET NULL
);

-- 5. outage_history table
CREATE TABLE outage_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    outage_id INT NOT NULL,
    old_status VARCHAR(30) NOT NULL,
    new_status VARCHAR(30) NOT NULL,
    updated_by VARCHAR(50) NOT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (outage_id) REFERENCES outage_reports(outage_id) ON DELETE CASCADE
);

-- 6. infrastructures table (Critical Infrastructures)
CREATE TABLE infrastructures (
    infrastructure_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    area_name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL
);

-- Insert seed users
-- Credentials: admin/admin, operator/operator
INSERT INTO users (username, password, role) VALUES 
('admin', 'admin', 'ADMIN'),
('operator', 'operator', 'OPERATOR');

-- Insert seed critical infrastructures
INSERT INTO infrastructures (name, area_name, type) VALUES
('Mirpur General Hospital', 'Mirpur', 'Hospital'),
('Uttara Police Headquarters', 'Uttara', 'Police Station'),
('Dhanmondi Fire Station', 'Dhanmondi', 'Fire Station'),
('Banani Medical Center', 'Banani', 'Hospital'),
('Gulshan Emergency Clinic', 'Gulshan', 'Hospital');

-- Insert seed technicians
INSERT INTO technicians (technician_name, phone, team_name, availability_status) VALUES
('John Doe', '+8801711111111', 'Team Alpha', 'AVAILABLE'),
('Jane Smith', '+8801822222222', 'Team Beta', 'AVAILABLE'),
('Bob Johnson', '+8801933333333', 'Team Gamma', 'BUSY'),
('Alice Brown', '+8801544444444', 'Team Delta', 'AVAILABLE');

-- Insert seed historical outages (Resolved) to populate predictions and statistics
-- Outage 1: Transformer Failure in Mirpur resolved in 2.5 hours
INSERT INTO outage_reports (area_name, outage_type, priority, cause, report_time, estimated_restore_time, actual_restore_time, status, assigned_technician_id) VALUES
('Mirpur', 'Emergency', 'CRITICAL', 'Transformer Failure', '2026-06-12 10:00:00', '2026-06-12 13:00:00', '2026-06-12 12:30:00', 'POWER_RESTORED', 3);

-- Outage 2: Transformer Failure in Mirpur resolved in 2 hours
INSERT INTO outage_reports (area_name, outage_type, priority, cause, report_time, estimated_restore_time, actual_restore_time, status, assigned_technician_id) VALUES
('Mirpur', 'Unscheduled', 'HIGH', 'Transformer Failure', '2026-06-13 14:00:00', '2026-06-13 16:30:00', '2026-06-13 16:00:00', 'POWER_RESTORED', 3);

-- Outage 3: Weather damage in Uttara resolved in 4 hours
INSERT INTO outage_reports (area_name, outage_type, priority, cause, report_time, estimated_restore_time, actual_restore_time, status, assigned_technician_id) VALUES
('Uttara', 'Emergency', 'CRITICAL', 'Weather', '2026-06-11 08:00:00', '2026-06-11 13:00:00', '2026-06-11 12:00:00', 'POWER_RESTORED', 3);

-- Outage 4: Maintenance in Dhanmondi resolved in 1.5 hours
INSERT INTO outage_reports (area_name, outage_type, priority, cause, report_time, estimated_restore_time, actual_restore_time, status, assigned_technician_id) VALUES
('Dhanmondi', 'Scheduled', 'LOW', 'Maintenance', '2026-06-10 09:00:00', '2026-06-10 11:00:00', '2026-06-10 10:30:00', 'POWER_RESTORED', 3);

-- Outage 5: Grid Overload in Gulshan resolved in 3 hours
INSERT INTO outage_reports (area_name, outage_type, priority, cause, report_time, estimated_restore_time, actual_restore_time, status, assigned_technician_id) VALUES
('Gulshan', 'Unscheduled', 'HIGH', 'Grid Overload', '2026-06-08 18:00:00', '2026-06-08 21:30:00', '2026-06-08 21:00:00', 'POWER_RESTORED', 3);

-- Outage 6: Equipment Failure in Banani resolved in 3.5 hours
INSERT INTO outage_reports (area_name, outage_type, priority, cause, report_time, estimated_restore_time, actual_restore_time, status, assigned_technician_id) VALUES
('Banani', 'Emergency', 'CRITICAL', 'Equipment Failure', '2026-06-05 13:00:00', '2026-06-05 17:00:00', '2026-06-05 16:30:00', 'POWER_RESTORED', 3);

-- Outage 7: Equipment Failure in Mirpur resolved in 2.5 hours
INSERT INTO outage_reports (area_name, outage_type, priority, cause, report_time, estimated_restore_time, actual_restore_time, status, assigned_technician_id) VALUES
('Mirpur', 'Unscheduled', 'HIGH', 'Equipment Failure', '2026-06-04 11:00:00', '2026-06-04 14:00:00', '2026-06-04 13:30:00', 'POWER_RESTORED', 3);

-- Seed history logs for completed outages
INSERT INTO outage_history (outage_id, old_status, new_status, updated_by, update_time) VALUES
(1, 'REPORTED', 'TECHNICIAN_ASSIGNED', 'admin', '2026-06-12 10:05:00'),
(1, 'TECHNICIAN_ASSIGNED', 'REPAIR_STARTED', 'admin', '2026-06-12 10:30:00'),
(1, 'REPAIR_STARTED', 'POWER_RESTORED', 'admin', '2026-06-12 12:30:00'),
(2, 'REPORTED', 'POWER_RESTORED', 'admin', '2026-06-13 16:00:00'),
(3, 'REPORTED', 'TECHNICIAN_ASSIGNED', 'admin', '2026-06-11 08:15:00'),
(3, 'TECHNICIAN_ASSIGNED', 'POWER_RESTORED', 'admin', '2026-06-11 12:00:00');

-- Seed current Active Outages
-- Active 1: Mirpur, Transformer Failure (Auto-crit because Mirpur General Hospital is in Mirpur)
INSERT INTO outage_reports (area_name, outage_type, priority, cause, report_time, estimated_restore_time, status, assigned_technician_id) VALUES
('Mirpur', 'Emergency', 'CRITICAL', 'Transformer Failure', '2026-06-15 03:00:00', '2026-06-15 05:15:00', 'REPORTED', NULL);

-- Active 2: Uttara, Weather (Auto-crit because Uttara Police HQ is in Uttara)
INSERT INTO outage_reports (area_name, outage_type, priority, cause, report_time, estimated_restore_time, status, assigned_technician_id) VALUES
('Uttara', 'Emergency', 'CRITICAL', 'Weather', '2026-06-15 02:00:00', '2026-06-15 06:00:00', 'TECHNICIAN_ASSIGNED', 3);

-- Seed notification logs for active outages
INSERT INTO notifications (outage_id, message, created_time) VALUES
(8, 'CRITICAL ALERT: New outage reported in Mirpur (Hospital Area Affected). Cause: Transformer Failure.', '2026-06-15 03:00:00'),
(9, 'CRITICAL ALERT: New outage reported in Uttara (Police Station Area Affected). Cause: Weather.', '2026-06-15 02:00:00'),
(9, 'Technician Bob Johnson (Team Gamma) assigned to outage #9 in Uttara.', '2026-06-15 02:05:00');
