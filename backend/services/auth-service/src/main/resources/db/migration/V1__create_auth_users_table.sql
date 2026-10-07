CREATE TABLE auth_users (
                            id BIGINT NOT NULL AUTO_INCREMENT,
                            user_id BIGINT NOT NULL,
                            password VARCHAR(255) NOT NULL,
                            role VARCHAR(20) NOT NULL DEFAULT 'USER',
                            provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
                            enabled BOOLEAN NOT NULL DEFAULT TRUE,
                            created_at DATETIME NOT NULL,
                            updated_at DATETIME NOT NULL,

                            CONSTRAINT pk_auth_users PRIMARY KEY (id),
                            CONSTRAINT uk_auth_users_user_id UNIQUE (user_id),

                            INDEX idx_auth_users_user_id (user_id),
                            INDEX idx_auth_users_role (role),
                            INDEX idx_auth_users_provider (provider)
);