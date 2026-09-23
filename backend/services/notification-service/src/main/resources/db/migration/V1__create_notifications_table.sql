CREATE TABLE notifications (
                               id BIGINT NOT NULL AUTO_INCREMENT,

                               user_id BIGINT NOT NULL,

                               type VARCHAR(50) NOT NULL,

                               title VARCHAR(200) NOT NULL,

                               message VARCHAR(500) NOT NULL,

                               reference_type VARCHAR(50),

                               reference_id BIGINT,

                               is_read BOOLEAN NOT NULL DEFAULT FALSE,

                               created_at DATETIME(6) NOT NULL,

                               PRIMARY KEY (id),

                               INDEX idx_notifications_user_id (user_id),

                               INDEX idx_notifications_user_read (user_id, is_read),

                               INDEX idx_notifications_created_at (created_at),

                               INDEX idx_notifications_reference (
        reference_type,
        reference_id
    )
);