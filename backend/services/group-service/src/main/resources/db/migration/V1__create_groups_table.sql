CREATE TABLE expense_groups (
                                id BIGINT NOT NULL AUTO_INCREMENT,
                                name VARCHAR(100) NOT NULL,
                                description VARCHAR(500),
                                created_by BIGINT NOT NULL,
                                active BOOLEAN NOT NULL DEFAULT TRUE,
                                created_at DATETIME NOT NULL,
                                updated_at DATETIME NOT NULL,

                                PRIMARY KEY (id),

                                INDEX idx_groups_created_by (created_by),
                                INDEX idx_groups_active (active)
);