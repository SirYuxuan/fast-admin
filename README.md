# fast-admin

雨轩快速开发平台 V2 —— 基于 Spring Boot 3 + Sa-Token + MyBatis-Plus 的后台管理脚手架，配套 Vue 前端 [`fast-admin-ui`](fast-admin-ui/)。

## 技术栈

- **JDK 25**，Spring Boot 3.5.11
- **Sa-Token** 鉴权（登录、权限、在线用户）
- **MyBatis-Plus** + MyBatis-Plus-Join，MySQL 9 / PostgreSQL 42 双驱动
- **Redis** (Lettuce) 缓存与会话
- **Quartz** 定时任务（含执行日志）
- **Knife4j** OpenAPI 文档
- **EasyExcel** 注解式导入导出
- **OSHI** 服务器监控（CPU/内存/JVM/磁盘）
- 文件存储：本地 / 阿里云 OSS / S3 / FTP / SFTP
- ID 生成：KSUID；密码加密：jBCrypt

## 模块

| 模块                                  | 说明                                                                                            |
| ------------------------------------- | ----------------------------------------------------------------------------------------------- |
| [`fast-framework`](fast-framework/)   | 框架基础：BaseEntity / BaseService / 全局异常 / TraceId / Excel / 代码生成                      |
| [`fast-system`](fast-system/)         | 系统模块：用户、角色、部门、菜单、权限、字典、文件、定时任务、操作/登录日志、服务监控、在线用户 |
| [`fast-biz-simple`](fast-biz-simple/) | 业务模块模板（复制即用，[使用说明](fast-biz-simple/README.md)）                                 |
| `fast-application`                    | 启动入口与配置聚合                                                                              |
| [`fast-admin-ui`](fast-admin-ui/)     | 前端工程                                                                                        |

## 快速开始

前置：JDK 25、MySQL 8+、Redis 6+、Maven 3.9+。可用 [mise](https://mise.jdx.dev) 直接装：`mise install`。

```bash
# 1. 建库并执行升级脚本
mysql -uroot -p -e "CREATE DATABASE fast_admin DEFAULT CHARSET utf8mb4;"

# 2. 修改本地连接（默认 localhost:3306 / root / root）
#    fast-application/src/main/resources/config/application-database.yml
#    fast-application/src/main/resources/config/application-redis.yml

# 3. 启动
mvn -pl fast-application -am spring-boot:run
```

启动后：

- API 文档：http://localhost:8080/doc.html

## 配置

`fast-application/src/main/resources/config/` 按职能拆分 profile：

```
application-database.yml   数据源 + Hikari
application-redis.yml      Redis 连接
application-auth.yml       Sa-Token
application-mybatis.yml    MyBatis-Plus
application-quartz.yml     Quartz 定时任务
application-upload.yml     文件存储
application-logging.yml    日志
```

## 安全提示

仓库内所有连接配置均为 **localhost demo 凭据**，仅用于本地开发。生产环境请通过环境变量或外部配置中心注入，**切勿提交真实密码到 Git**。
