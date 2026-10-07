ALTER TABLE auth_users
    ADD COLUMN provider_subject VARCHAR(255) NULL;

CREATE INDEX idx_auth_users_provider_subject
    ON auth_users(provider_subject);

ALTER TABLE auth_users
    ADD CONSTRAINT uk_auth_users_provider_subject
        UNIQUE (provider, provider_subject);