# fast-admin

> 雨轩快速开发平台 —— 基于 Spring Boot 3 的后台管理脚手架，内置 AI 运营助手。

![Java](https://img.shields.io/badge/Java-25-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![Spring AI](https://img.shields.io/badge/Spring_AI-2.0-6DB33F?logo=spring&logoColor=white)
![MyBatis Plus](https://img.shields.io/badge/MyBatis_Plus-3.5-blue)
![Sa-Token](https://img.shields.io/badge/Sa--Token-1.x-orange)
![License](https://img.shields.io/badge/License-MIT-green)

## 功能特性

- **权限体系**：Sa-Token 登录鉴权 + RBAC 角色权限 + 部门数据隔离 + 在线用户管理
- **系统管理**：用户 / 角色 / 菜单 / 部门 / 字典 / 定时任务 / 操作日志 / 登录日志
- **服务监控**：CPU / 内存 / JVM / 磁盘实时监控（OSHI）
- **文件存储**：本地 / 阿里云 OSS / AWS S3 / FTP / SFTP 多策略
- **AI 运营助手**：Spring AI 2.0，支持多模型配置切换、自定义 Tool、MCP 服务接入，SQL 工具内置敏感字段防护
- **Docker 部署**：开箱即用的 `docker-compose`，前后端分离镜像

## 技术栈

| 层次          | 选型                                                       |
| ------------- | ---------------------------------------------------------- |
| 语言 / 运行时 | JDK 25                                                     |
| Web 框架      | Spring Boot 3.5                                            |
| 鉴权          | Sa-Token                                                   |
| ORM           | MyBatis-Plus 3.5 + MyBatis-Plus-Join                       |
| 数据库        | MySQL 8.4（兼容 PostgreSQL）                               |
| 缓存          | Redis 6（Lettuce）                                         |
| AI            | Spring AI 2.0，兼容 OpenAI / Anthropic / OpenAI-Compatible |
| 定时任务      | Quartz                                                     |
| API 文档      | Knife4j（OpenAPI 3）                                       |
| Excel         | EasyExcel                                                  |
| ID 生成       | KSUID                                                      |
| 加密          | jBCrypt                                                    |
| 前端          | Vue 3 + Vben Admin 5                                       |

## 模块

| 模块                                  | 说明                                                                                            |
| ------------------------------------- | ----------------------------------------------------------------------------------------------- |
| [`fast-framework`](fast-framework/)   | 框架基础：BaseEntity / BaseService / 全局异常 / TraceId / Excel / 代码生成                      |
| [`fast-system`](fast-system/)         | 系统模块：用户、角色、部门、菜单、权限、字典、文件、定时任务、操作/登录日志、服务监控、在线用户 |
| [`fast-ai`](fast-ai/)                 | AI 模块：模型配置、对话、Tool 调用、MCP 接入、工具调用日志                                      |
| [`fast-biz-simple`](fast-biz-simple/) | 业务模块模板（复制即用，[使用说明](fast-biz-simple/README.md)）                                 |
| [`fast-application`](fast-application/) | 启动入口与配置聚合                                                                            |
| [`fast-admin-ui`](fast-admin-ui/)     | 前端工程                                                                                        |

## 快速开始

前置：JDK 25、MySQL 8+、Redis 6+、Maven 3.9+。可用 [mise](https://mise.jdx.dev) 一键安装：`mise install`。

```bash
# 1. 建库
mysql -uroot -p -e "CREATE DATABASE fast_admin DEFAULT CHARSET utf8mb4;"

# 2. 修改本地连接
#    fast-application/src/main/resources/config/application-database.yml
#    fast-application/src/main/resources/config/application-redis.yml

# 3. 启动
mvn -pl fast-application -am spring-boot:run
```

启动后：API 文档 → http://localhost:8080/doc.html

### Docker 部署

```bash
cd deploy
cp .env.example .env   # 按需修改
docker compose up -d
```

## 配置

`fast-application/src/main/resources/config/` 按职能拆分：

```
application-database.yml   数据源 + Hikari
application-redis.yml      Redis 连接
application-auth.yml       Sa-Token
application-mybatis.yml    MyBatis-Plus
application-quartz.yml     Quartz 定时任务
application-upload.yml     文件存储
application-logging.yml    日志
```

## 进度

### 已完成

- [x] Spring Boot 3 + JDK 25 工程基础
- [x] Sa-Token 登录鉴权 + RBAC 权限体系
- [x] 用户 / 角色 / 菜单 / 部门 / 字典管理
- [x] 定时任务（Quartz）+ 执行日志
- [x] 操作日志 / 登录日志
- [x] 服务监控（CPU / 内存 / JVM / 磁盘）
- [x] 文件存储多策略（本地 / OSS / S3 / FTP / SFTP）
- [x] EasyExcel 注解式导入导出
- [x] Knife4j OpenAPI 文档
- [x] Docker Compose 一键部署
- [x] AI 运营助手（Spring AI 2.0）
- [x] AI 多模型配置管理（OpenAI / Anthropic / OpenAI-Compatible）
- [x] AI 自定义 Tool（SQL 查询 / HTTP 调用）
- [x] AI MCP 服务接入
- [x] AI 工具调用日志
- [x] AI SQL 工具敏感字段防护（别名绕过拦截 + 结果层脱敏）
- [x] Demo 模式（只读演示环境）

### 计划中

- [ ] 消息中心（站内通知 / WebSocket 推送）
- [ ] 多租户支持
- [ ] 工作流 / 审批引擎
- [ ] OAuth2 / SSO 第三方登录
- [ ] 国际化（i18n）
- [ ] AI 知识库（RAG 向量检索）
- [ ] 移动端适配

