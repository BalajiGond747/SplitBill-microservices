CREATE TABLE group_participants (
                                    id BIGINT NOT NULL AUTO_INCREMENT,
                                    group_id BIGINT NOT NULL,
                                    user_id BIGINT NOT NULL,
                                    active BOOLEAN NOT NULL DEFAULT TRUE,
                                    created_at DATETIME(6) NOT NULL,
                                    updated_at DATETIME(6) NOT NULL,

                                    PRIMARY KEY (id),

                                    CONSTRAINT uk_group_participant_group_user
                                        UNIQUE (group_id, user_id),

                                    INDEX idx_group_participants_group (group_id),
                                    INDEX idx_group_participants_user (user_id)
);