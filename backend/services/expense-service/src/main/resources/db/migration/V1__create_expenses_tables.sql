CREATE TABLE expenses (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          group_id BIGINT NOT NULL,
                          paid_by BIGINT NOT NULL,
                          amount DECIMAL(19, 2) NOT NULL,
                          description VARCHAR(255) NOT NULL,
                          expense_date DATE NOT NULL,
                          split_type VARCHAR(20) NOT NULL,
                          created_at DATETIME(6) NOT NULL,
                          updated_at DATETIME(6) NOT NULL,

                          PRIMARY KEY (id),

                          INDEX idx_expenses_group_id (group_id),
                          INDEX idx_expenses_paid_by (paid_by),
                          INDEX idx_expenses_expense_date (expense_date),
                          INDEX idx_expenses_split_type (split_type)
);

CREATE TABLE expense_splits (
                                id BIGINT NOT NULL AUTO_INCREMENT,
                                expense_id BIGINT NOT NULL,
                                user_id BIGINT NOT NULL,
                                amount DECIMAL(19, 2) NOT NULL,
                                percentage DECIMAL(7, 4),

                                PRIMARY KEY (id),

                                CONSTRAINT fk_expense_splits_expense
                                    FOREIGN KEY (expense_id)
                                        REFERENCES expenses(id)
                                        ON DELETE CASCADE,

                                INDEX idx_expense_splits_expense_id (expense_id),
                                INDEX idx_expense_splits_user_id (user_id)
);