--liquibase formatted sql

--changeset scathies:31.01.2024.create-table-users
CREATE TABLE users
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(128) UNIQUE NOT NULL,
    password VARCHAR(128)        NOT NULL
);