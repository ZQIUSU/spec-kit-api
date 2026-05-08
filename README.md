# cream-login-api (`cream-login-api`)

独立 Spring Boot **3.4** / Java **17** 工程，为奶油风登录前端提供 REST API。

## 运行

```bash
mvn spring-boot:run
# 或
mvn -q -DskipTests package && java -jar target/cream-login-api-*.jar
```

## 配置

- **`src/main/resources/application.yml`**：`SPRING_DATASOURCE_*`、端口、`app.demo-user.password`。
- **前端静态资源路径**（用于校验 `public/branding` 等文件是否存在）：  
  - `app.frontend-assets.base-path`，或环境变量 **`FRONTEND_ASSET_BASE`**（指向 **`my-first-front-project`** 根目录）。默认 **`../my-first-front-project`**（与前端仓库同级时）。

## Docker

```bash
docker build -t cream-login-api .
```

## Demo 账号

首次启动种子：`demo@example.com` / `password123`（可用 **`DEMO_USER_PASSWORD`** 覆盖）。

## 与前端仓库关系

前端位于同级目录 **`my-first-front-project`**（各自独立 Git）。联调步骤见该仓库内 **`docs/CROSS_REPO_DEV.md`**。
