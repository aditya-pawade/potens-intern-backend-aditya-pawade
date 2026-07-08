CREATE TABLE IF NOT EXISTS schemes (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(200) NOT NULL UNIQUE,
    description         TEXT NOT NULL,
    category            VARCHAR(100) NOT NULL,
    ministry            VARCHAR(200) NOT NULL,
    benefit_amount      VARCHAR(100),
    benefit_description TEXT,
    eligibility_summary TEXT,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_schemes_category (category),
    INDEX idx_schemes_is_active (is_active)
);

CREATE TABLE IF NOT EXISTS scheme_rules (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    scheme_id    BIGINT NOT NULL,
    attribute    VARCHAR(50) NOT NULL,
    rule_type    VARCHAR(50) NOT NULL,
    value        VARCHAR(100),
    min_value    VARCHAR(100),
    max_value    VARCHAR(100),
    weight       INT NOT NULL,
    is_mandatory BOOLEAN NOT NULL DEFAULT FALSE,
    description  VARCHAR(500) NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_scheme_rules_scheme_id (scheme_id),
    FOREIGN KEY (scheme_id) REFERENCES schemes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS app_users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
