# CLAUDE.md

本文件为 Claude Code 提供本项目的背景信息。

## 项目介绍

`cream-login-api` 是一个独立的 **Spring Boot 3.4 / Java 17** 后端工程，为「奶油风登录」前端（同级目录 `my-first-front-project`，独立 Git 仓库）提供 REST API。

技术栈：

- Spring Boot 3.4（Web、Data JPA、Security、Validation）
- MySQL（`mysql-connector-j`）
- JWT 认证（`io.jsonwebtoken` jjwt 0.12.6）
- Maven 构建，HTTP 默认端口 **9080**（可用 `SERVER_PORT` 覆盖）

主要能力：用户注册/登录/找回密码（JWT + 身份证三要素校验）、菜品（Dish）、积分奖励与任务（Reward / RewardTask / 每日任务 / 兑换记录）、品牌信息（Branding）等。

代码位于 `src/main/java/com/creamlogin/app/`，按 `web`（Controller）、`service`、`repository`、`domain`、`security`、`config` 分层。

在每次提交到远程后，本地连接到阿里云服务器进行更新部署
