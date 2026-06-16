# 模块说明

本项目（fast-admin）是雨轩快速开发平台 V2，采用 Maven 多模块结构。以下介绍各模块的职责。

---

## fast-framework

**框架基础层**，不依赖任何业务模块，为所有上层模块提供通用能力。

| 能力 | 内容 |
|------|------|
| 实体基类 | `BaseEntity`（id + 审计字段 + 逻辑删除）、`BaseQuery` |
| 服务/Mapper基类 | `BaseService`、`BaseMapper`，封装常用 CRUD |
| 查询注解 | `@QueryField` + `ValueType`，自动拼装 `QueryWrapper` |
| 全局异常 | `BizException`、`GlobalExceptionHandler` |
| 统一响应 | `Rs`（Result）、`Ps`（分页）、`HttpCode` |
| Excel 工具 | `@Excel` 注解、`ExcelUtil`、字典翻译 `DictResolver` |
| ID 生成 | `CustomIdGenerator`（基于 KSUID） |
| Web 配置 | `WebConfig`、`Knife4jConfig`、`TraceIdFilter` |
| 工具类 | `IdUtil`、`PasswordUtil`、`RedisUtil`、`ServletUtil`、`AuditContextHolder` |
| 数据库配置 | `MyBatisConfig`、`SqlSlowLogInterceptor`（慢 SQL 日志） |

---

## fast-system

**系统功能层**，依赖 `fast-framework`，实现后台管理平台的通用系统功能。

| 子模块 | 内容 |
|--------|------|
| 认证（auth） | 登录/登出、Sa-Token 安全配置、`AuditContextFilter`（注入当前用户） |
| 用户（user） | 用户 CRUD、密码修改、个人信息、角色分配 |
| 角色（role） | 角色 CRUD、菜单权限绑定 |
| 部门（dept） | 部门树形结构管理 |
| 菜单（menu） | 菜单/权限码 CRUD，返回路由树 |
| 字典（dict） | 字典类型 + 字典数据管理，与 Excel 导入导出联动 |
| 配置（config） | 系统参数键值对管理 |
| 文件（file） | 文件上传/下载，支持本地、阿里云 OSS、S3、FTP、SFTP 五种存储后端，可在线切换 |
| 定时任务（job） | 基于 Quartz 的任务调度，含执行日志、禁并发执行支持 |
| 操作日志（log） | `@OperationLog` AOP 注解，自动记录操作人、接口、耗时；登录日志单独存储 |
| 在线用户（online） | 查看当前登录会话，支持强制下线 |
| 服务监控（monitor） | 基于 OSHI 采集 CPU、内存、JVM、磁盘信息 |

---

## fast-biz-simple

**业务模块模板**，以 `Demo` 为示例展示标准业务模块的完整写法。

- 包含 `entity / dto / query / mapper / service / controller` 完整分层
- 作为新业务模块的起点：复制本目录 → 改 artifactId → 改包名 → 注册到根 pom
- 详细步骤见 [fast-biz-simple/README.md](fast-biz-simple/README.md)

---

## fast-ai

**AI 能力层**，依赖 `fast-framework`，基于 Spring AI 2.0 接入大模型能力。

| 子模块 | 内容 |
|--------|------|
| 模型配置（model） | 管理多个 AI 模型配置（provider、baseUrl、apiKey、模型名），支持连通性探测 |
| MCP 服务器（mcp） | 管理 MCP（Model Context Protocol）服务器配置，用于扩展模型工具能力 |
| AI 工具（tool） | 管理自定义工具配置，`AiToolCallbackService` 动态注册 Tool Callback，`AiToolExecutionService` 执行工具调用 |
| 对话代理（agent） | 多会话 AI 对话，SSE 流式返回；`AiChatClientFactory` 按配置动态构建 ChatClient；持久化会话历史 |

---

## fast-application

**启动入口**，仅含 `FastApplication.java` 和配置文件，负责将所有模块组装为可运行的 Spring Boot 应用。

- 聚合依赖：引入 `fast-system`、`fast-ai`、`fast-biz-simple` 等所有模块
- 配置拆分（位于 `src/main/resources/config/`）：

  | 配置文件 | 说明 |
  |----------|------|
  | `application-database.yml` | 数据源 + HikariCP |
  | `application-redis.yml` | Redis 连接 |
  | `application-auth.yml` | Sa-Token 参数 |
  | `application-mybatis.yml` | MyBatis-Plus |
  | `application-quartz.yml` | Quartz 定时任务 |
  | `application-upload.yml` | 文件存储 |
  | `application-logging.yml` | 日志级别 |

---

## fast-admin-ui

**前端工程**，pnpm monorepo 结构，为本后端提供配套管理界面。

---

## 模块依赖关系

```
fast-application
    ├── fast-system
    │       └── fast-framework
    ├── fast-ai
    │       └── fast-framework
    └── fast-biz-simple
            └── fast-framework
```
