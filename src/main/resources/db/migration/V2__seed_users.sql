-- Seed admin user (password: admin123)
-- BCrypt hash generated with cost factor 10
INSERT INTO app_users (username, password, role)
VALUES ('admin', '$2a$10$EqKcp1WFKVQISheBxmHkkuFN0PBpFBBcwqHmYWOVlaMFsMMRaL7N6', 'ADMIN');

-- Seed regular user (password: user123)
INSERT INTO app_users (username, password, role)
VALUES ('user', '$2a$10$dI8C5nuT4d0JKflULGWKKe9wHUmFA/UIF1Qka0JxSaUPYtHVmrqLi', 'USER');
