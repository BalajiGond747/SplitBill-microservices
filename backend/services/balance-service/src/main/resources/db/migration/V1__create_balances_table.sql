CREATE TABLE balances (
                          id BIGINT NOT NULL AUTO_INCREMENT,

                          group_id BIGINT NOT NULL,

                          from_user_id BIGINT NOT NULL,

                          to_user_id BIGINT NOT NULL,

                          amount DECIMAL(19, 2) NOT NULL,

                          created_at DATETIME(6) NOT NULL,

                          updated_at DATETIME(6) NOT NULL,

                          PRIMARY KEY (id),

                          CONSTRAINT chk_balances_different_users
                              CHECK (from_user_id <> to_user_id),

                          CONSTRAINT chk_balances_positive_amount
                              CHECK (amount > 0),

                          CONSTRAINT uk_balances_group_users
                              UNIQUE (group_id, from_user_id, to_user_id),

                          INDEX idx_balances_group_id (group_id),

                          INDEX idx_balances_from_user_id (from_user_id),

                          INDEX idx_balances_to_user_id (to_user_id)
);