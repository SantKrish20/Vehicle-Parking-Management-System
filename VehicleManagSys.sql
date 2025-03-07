CREATE DATABASE parking_management;
USE parking_management;
CREATE TABLE vehicles (
    vehicle_id INT AUTO_INCREMENT PRIMARY KEY,
    number_plate VARCHAR(20) NOT NULL,
    entry_time DATETIME NOT NULL,
    exit_time DATETIME DEFAULT NULL,
    parking_fee DECIMAL(10, 2) DEFAULT NULL
);