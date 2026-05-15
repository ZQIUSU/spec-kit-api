-- 授予 zqiusu 用户对 cream_login 库的全部权限
GRANT ALL PRIVILEGES ON cream_login.* TO 'zqiusu'@'%';
FLUSH PRIVILEGES;

-- 建表：users（JPA ddl-auto=update 也会自动建，这里显式声明保证列完整）
CREATE TABLE IF NOT EXISTS users (
  id                    BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username              VARCHAR(255) NOT NULL UNIQUE,
  email                 VARCHAR(255) NOT NULL UNIQUE,
  password_hash         VARCHAR(255) NOT NULL,
  password_recovery_enc VARCHAR(512) NOT NULL DEFAULT '',
  real_name             VARCHAR(64)  NOT NULL DEFAULT '',
  id_card_number        VARCHAR(18)  NOT NULL DEFAULT '',
  created_at            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
