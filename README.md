# cream-login-api (`cream-login-api`)

独立 Spring Boot **3.4** / Java **17** 工程，为奶油风登录前端提供 REST API。

## 运行

```bash
mvn spring-boot:run
# 或
mvn -q -DskipTests package && java -jar target/cream-login-api-*.jar
```

## 配置

- **`src/main/resources/application.yml`**：`SPRING_DATASOURCE_*`、HTTP 端口（默认 **9080**，可用 **`SERVER_PORT`** 覆盖）、`app.demo-user.password`。
- **前端静态资源路径**（用于校验 `public/branding` 等文件是否存在）：  
  - `app.frontend-assets.base-path`，或环境变量 **`FRONTEND_ASSET_BASE`**（指向 **`my-first-front-project`** 根目录）。默认 **`../my-first-front-project`**（与前端仓库同级时）。

## Docker

```bash
docker build -t cream-login-api .
```

本地 MySQL 容器见根目录 **`docker-compose.yml`**。若应用从宿主机连 published 端口时出现 **`Access denied for user 'root'@'172.x.x.x'`**，说明库里还没有允许远程网段的 `root`：可执行  
`docker exec -it cream-login-mysql mysql -uroot -proot -e "CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY 'root'; GRANT ALL ON *.* TO 'root'@'%' WITH GRANT OPTION; FLUSH PRIVILEGES;"`  
或 **`docker compose down -v`** 后重新 `up`（会清空数据卷，仅开发可接受）。

## Demo 账号

首次启动种子：`demo@example.com` / `password123`（可用 **`DEMO_USER_PASSWORD`** 覆盖）。

## 与前端仓库关系

前端位于同级目录 **`my-first-front-project`**（各自独立 Git）。联调步骤见该仓库内 **`docs/CROSS_REPO_DEV.md`**。
