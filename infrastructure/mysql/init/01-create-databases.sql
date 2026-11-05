CREATE DATABASE IF NOT EXISTS user_db
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS auth_db
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS group_db
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS expense_db
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS balance_db
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS settlement_db
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS payment_db
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS notification_db
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON auth_db.* TO 'splitbill'@'%';
GRANT ALL PRIVILEGES ON group_db.* TO 'splitbill'@'%';
GRANT ALL PRIVILEGES ON expense_db.* TO 'splitbill'@'%';
GRANT ALL PRIVILEGES ON balance_db.* TO 'splitbill'@'%';
GRANT ALL PRIVILEGES ON settlement_db.* TO 'splitbill'@'%';
GRANT ALL PRIVILEGES ON payment_db.* TO 'splitbill'@'%';
GRANT ALL PRIVILEGES ON notification_db.* TO 'splitbill'@'%';