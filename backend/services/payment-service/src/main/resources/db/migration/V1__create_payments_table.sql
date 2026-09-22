CREATE TABLE payments (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          user_id BIGINT NOT NULL,
                          group_id BIGINT,
                          settlement_id BIGINT,
                          razorpay_order_id VARCHAR(100) NOT NULL,
                          razorpay_payment_id VARCHAR(100),
                          razorpay_signature VARCHAR(255),
                          amount DECIMAL(19, 2) NOT NULL,
                          currency VARCHAR(10) NOT NULL,
                          status VARCHAR(30) NOT NULL,
                          description VARCHAR(500),
                          created_at DATETIME(6) NOT NULL,
                          updated_at DATETIME(6) NOT NULL,

                          PRIMARY KEY (id),

                          CONSTRAINT uk_payments_razorpay_order_id
                              UNIQUE (razorpay_order_id),

                          CONSTRAINT uk_payments_razorpay_payment_id
                              UNIQUE (razorpay_payment_id),

                          CONSTRAINT chk_payments_positive_amount
                              CHECK (amount > 0),

                          INDEX idx_payments_user_id (user_id),
                          INDEX idx_payments_group_id (group_id),
                          INDEX idx_payments_settlement_id (settlement_id),
                          INDEX idx_payments_status (status)
);