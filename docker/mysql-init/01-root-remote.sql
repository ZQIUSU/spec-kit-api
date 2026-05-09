-- 首次初始化数据目录时执行：允许从宿主机 / 其他容器经网桥 IP 使用 root 登录（与 MYSQL_ROOT_PASSWORD 一致）
CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY 'root';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
