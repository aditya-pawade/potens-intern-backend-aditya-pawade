-- Seed admin user (password: admin123)
-- BCrypt hash generated with cost factor 10
INSERT INTO app_users (username, password, role)
VALUES ('admin', '$2a$10$.L/f/jiBUszs3IwT6sfAk.wrOfmos.QX8x4JyOQhbpZDt33CQuc3O', 'ADMIN');

-- Seed regular user (password: user123)
INSERT INTO app_users (username, password, role)
VALUES ('user', '$2a$10$XphG03INpzzjSSyCjW8/ouiWlb79fWup2K66oVXTo29RvQUONVBUW', 'USER');
