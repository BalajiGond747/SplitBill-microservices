CREATE TABLE settlements (
                             id BIGINT NOT NULL AUTO_INCREMENT,
                             group_id BIGINT NOT NULL,
                             from_user_id BIGINT NOT NULL,
                             to_user_id BIGINT NOT NULL,
                             amount DECIMAL(19, 2) NOT NULL,
                             settlement_date DATETIME(6) NOT NULL,
                             note VARCHAR(500),
                             created_at DATETIME(6) NOT NULL,
                             updated_at DATETIME(6) NOT NULL,

                             PRIMARY KEY (id),

                             CONSTRAINT chk_settlements_different_users
                                 CHECK (from_user_id <> to_user_id),

                             CONSTRAINT chk_settlements_positive_amount
                                 CHECK (amount > 0),

                             INDEX idx_settlements_group_id (group_id),
                             INDEX idx_settlements_from_user_id (from_user_id),
                             INDEX idx_settlements_to_user_id (to_user_id),
                             INDEX idx_settlements_settlement_date (settlement_date)
);