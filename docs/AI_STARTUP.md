# AI 启动说明

这份文档给 AI/自动化代理使用，目标是把 fast-admin 的前后端在本机启动起来，并能验证是否成功。

## 项目结构

- 后端：Maven 多模块 Spring Boot 项目，启动模块是 `fast-application`。
- 前端：`fast-admin-ui`，pnpm monorepo，应用包名是 `fast-admin`。
- 初始化 SQL：`scripts/sql/fast_admin_init.sql`。

## 端口与默认账号

- 后端：http://localhost:8080
- API 文档：http://localhost:8080/doc.html
- 前端：http://localhost:5666
- 默认账号：`admin`
- 默认密码：`admin123`

## 必要环境

- JDK 25
- Maven 3.9+ 更合适；当前机器用 Maven 3.6.3 也能打包运行
- Node.js 22
- pnpm 10+
- MySQL 8+/9+
- Redis 6+
- Qdrant（AI 知识库 / RAG 向量库，当前使用 `http://100.115.97.59:6333`）

注意：当前仓库有 `mise.toml`：

```bash
mise exec -- java -version
mise exec -- node -v
```

如果直接 `java -version` 不是 25，不要直接启动后端，使用 `mise exec -- ...`。

## 数据库与 Redis

后端默认配置在：

- `fast-application/src/main/resources/config/application-database.yml`
- `fast-application/src/main/resources/config/application-redis.yml`

默认数据库连接是：

```text
jdbc:mysql://localhost:3306/fast_admin
username=root
password=root
```

默认 Redis 连接是：

```text
host=localhost
port=6379
database=3
password=
```

默认 AI 运维配置来自 `scripts/sql/ai_config.sql` 写入的 `sys_config`：

```text
ai.assistant.enabled=true
ai.assistant.require-permission=false
ai.mcp.client.enabled=true
ai.rag.qdrant.url=http://100.115.97.59:6333
ai.rag.qdrant.api-key=
ai.rag.collection-name=fast_admin_rag
ai.rag.embedding.base-url=
ai.rag.embedding.api-key=
ai.rag.embedding.model=text-embedding-3-small
```

可用以下接口验证后端是否能连接向量库：

```bash
curl http://localhost:8080/ai/rag/vector-store/status
curl http://localhost:8080/ai/rag/vector-store/collections
```

注意：Qdrant 集合创建需要先确定 embedding 模型维度；不要在未确定维度时手工创建 `fast_admin_rag` 集合。
系统首次写入向量时会根据 embedding 返回的维度自动创建集合。

RAG 知识库表和菜单迁移脚本：

```bash
mysql --protocol=tcp -h127.0.0.1 -P3307 -uroot < scripts/sql/ai_rag.sql
mysql --protocol=tcp -h127.0.0.1 -P3307 -uroot fast_admin < scripts/sql/ai_config.sql
```

当前 MVP 支持上传并解析 `txt/md/csv/json/xml/html/yml/yaml/log/doc/docx/ppt/pptx/xls/xlsx` 等文件；Office 文档由后端基于 Apache POI 本地提取文本后写入向量库。知识库支持配置切片长度、切片重叠和切片分隔符（默认 `\n\n`，保存值为 `\\n\\n`）。
AI 运维里的 `AI 配置` 页面会把助手、MCP、Qdrant、Embedding 配置写入 `sys_config`；AI 业务运行配置只读取 `sys_config`，项目不再保留 `application-ai.yml` 作为兜底。

如果本机 MySQL 不能用默认账号登录，可以用环境变量覆盖，不必改配置文件。

## 启动方式 A：已有可用 MySQL/Redis

1. 建库并导入 SQL：

```bash
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS fast_admin DEFAULT CHARSET utf8mb4;"
mysql -uroot -p fast_admin < scripts/sql/fast_admin_init.sql
```

2. 打包后端：

```bash
mise exec -- mvn -pl fast-application -am package -DskipTests
```

3. 启动后端：

```bash
SERVER_PORT=8080 \
SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3306/fast_admin?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true' \
SPRING_DATASOURCE_USERNAME=root \
SPRING_DATASOURCE_PASSWORD=root \
SPRING_DATA_REDIS_HOST=127.0.0.1 \
SPRING_DATA_REDIS_PORT=6379 \
SPRING_DATA_REDIS_DATABASE=3 \
mise exec -- java -jar fast-application/target/fast-application-0.0.1-dev.jar
```

4. 启动前端：

```bash
cd fast-admin-ui
pnpm dev --host 0.0.0.0
```

前端接口地址来自 `fast-admin-ui/apps/.env.development`：

```text
VITE_PORT=5666
VITE_GLOB_API_URL=http://localhost:8080/
```

## 启动方式 B：隔离临时 MySQL

当本机已有 MySQL 但不知道账号密码，或者不想碰本机数据库时，可以启动一个隔离 MySQL。当前机器已验证使用 `/tmp/fast-admin-runtime/mysql-data` 和端口 `3307`。

1. 初始化并启动临时 MySQL：

```bash
RUNTIME=/tmp/fast-admin-runtime
DATADIR="$RUNTIME/mysql-data"
mkdir -p "$RUNTIME"

if [ ! -d "$DATADIR/mysql" ]; then
  mysqld --initialize-insecure --datadir="$DATADIR" --log-error="$RUNTIME/mysql-init.log"
fi

mysqld \
  --datadir="$DATADIR" \
  --port=3307 \
  --bind-address=127.0.0.1 \
  --socket="$RUNTIME/mysql.sock" \
  --pid-file="$RUNTIME/mysql.pid" \
  --mysqlx=0 \
  --log-error="$RUNTIME/mysql.log"
```

2. 另开终端导入 SQL：

```bash
mysql --protocol=tcp -h127.0.0.1 -P3307 -uroot < scripts/sql/fast_admin_init.sql
```

3. 后端启动时覆盖数据源：

```bash
SERVER_PORT=8080 \
SPRING_DATASOURCE_URL='jdbc:mysql://127.0.0.1:3307/fast_admin?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true' \
SPRING_DATASOURCE_USERNAME=root \
SPRING_DATASOURCE_PASSWORD= \
SPRING_DATA_REDIS_HOST=127.0.0.1 \
SPRING_DATA_REDIS_PORT=6379 \
SPRING_DATA_REDIS_DATABASE=3 \
mise exec -- java -jar fast-application/target/fast-application-0.0.1-dev.jar
```

## 本机已验证的启动状态

本次启动使用：

- MySQL：`127.0.0.1:3307`，临时数据目录 `/tmp/fast-admin-runtime/mysql-data`
- Redis：`127.0.0.1:6379`，database `3`
- 后端：`java -jar fast-application/target/fast-application-0.0.1-dev.jar`
- 前端：`cd fast-admin-ui && pnpm dev --host 0.0.0.0`

验证结果：

```bash
curl -I http://localhost:8080/doc.html
curl -I http://localhost:5666/
curl -s -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' \
  http://localhost:8080/auth/login
```

期望：

- `doc.html` 返回 HTTP 200
- 前端首页返回 HTTP 200
- 登录接口返回 HTTP 200，响应 JSON 中 `code` 为 `0`

## 常见坑

- 不要在根模块直接执行 `mvn -pl fast-application -am spring-boot:run`。在当前 Maven 行为下，插件可能执行到根 `pom`，报 `Unable to find a suitable main class`。
- 更稳的流程是先 `mvn -pl fast-application -am package -DskipTests`，再 `java -jar fast-application/target/fast-application-0.0.1-dev.jar`。
- 如果 shell 默认 Java 是 8，后端会失败；必须用 JDK 25。
- Docker CLI 存在不代表 Docker daemon 已启动；先 `docker ps` 验证。

## 停止本机 launchctl 启动的服务

如果服务是用本机临时 launch agent 启动的，可以停止：

```bash
launchctl bootout "gui/$(id -u)" /tmp/fast-admin-runtime/cc.oofo.fast-admin.frontend.plist
launchctl bootout "gui/$(id -u)" /tmp/fast-admin-runtime/cc.oofo.fast-admin.backend.plist
launchctl bootout "gui/$(id -u)" /tmp/fast-admin-runtime/cc.oofo.fast-admin.mysql.plist
```

日志位置：

```text
/tmp/fast-admin-runtime/backend.out
/tmp/fast-admin-runtime/backend.err
/tmp/fast-admin-runtime/frontend.out
/tmp/fast-admin-runtime/frontend.err
/tmp/fast-admin-runtime/mysql.log
```
