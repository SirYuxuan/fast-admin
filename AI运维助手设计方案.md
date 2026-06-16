# 后台 AI 运维助手 — 设计方案

> 状态：设计草案（待评审）
> 日期：2026-06-16
> 范围：单一子项目 —— 后台管理（`fast-admin-ui`）右下角浮动 AI 运维助手 v1

---

## 1. 背景与目标

在 `fast-admin-ui`（Vben v5 + Ant Design Vue）右下角增加一个**浮动按钮**，点击展开**对话面板**。用户用自然语言下达指令，由后端 **Agent** 调用一组「工具（tools）」完成后台运维操作（查数据 + 改数据），并支持接入**外部 MCP 服务**扩展能力。

### 目标（v1）
- 后台任意页面右下角常驻浮动入口 + 抽屉式对话面板，**流式**输出。
- Agent 能**读 + 写**后台业务数据（全自动，无逐步人工确认弹窗）。
- LLM 层**可切换**（默认 Claude，亦可切换 OpenAI 兼容模型如 DeepSeek/Qwen），基于 **Spring AI**。
- 工具来源 = **内置后台 tools** + **外部 MCP 服务（可配置 1 个或多个）**。
- 全量**审计**每一次工具调用；提供**总开关**与**权限隔离**护栏。

### 非目标（v1，YAGNI）
- 不把本项目能力反向暴露为 MCP Server。
- 不做多 Agent 编排 / 子 Agent。
- 不做语音、图片生成等多模态对话。
- 不做对话的复杂记忆/向量检索（仅按会话保存原始消息）。

---

## 2. 总体架构

```
┌────────────────────────── fast-admin-ui (Vben) ──────────────────────────┐
│  右下角 FAB 按钮 ──► 对话抽屉 (消息流 / 工具调用时间线 / 输入框)            │
│        │  SSE 流式 (POST /ai/agent/chat, 携带 Sa-Token)                    │
└────────┼──────────────────────────────────────────────────────────────────┘
         ▼
┌────────────────────── fast-application (Spring Boot 3.5 / Java 25) ───────┐
│  AgentController  ──► AgentChatService                                     │
│      │                   │                                                 │
│      │     ┌─────────────┴───────────────┐                                │
│      │     ▼                             ▼                                 │
│      │  Spring AI ChatClient        会话/审计持久化                         │
│      │   ├─ 内置 @Tool 工具集 ──► 现有 Service（带权限校验）               │
│      │   ├─ 外部 MCP 工具（Spring AI MCP Client，按 DB 配置动态连接）       │
│      │   └─ 模型 Provider（默认 Anthropic Claude，可切换 OpenAI 兼容）      │
└───────────────────────────────────────────────────────────────────────────┘
```

**编排方案：A（Spring AI 原生 ChatClient + 内置 tool-calling 循环）** —— 模型决定调用哪个工具、框架执行并回填、循环直到产出最终回答；内置工具与 MCP 工具合并进同一工具集。

---

## 3. 技术选型与版本

| 组件 | 选型 | 备注 |
|---|---|---|
| LLM 抽象 | **Spring AI 1.0.x** | `ChatClient` 统一抽象；与 Boot 3.5.11 的兼容性需在落地时验证（必要时取 1.0 最新补丁版） |
| 默认模型 | **`claude-opus-4-8`** | 通过 `spring-ai-starter-model-anthropic` 接入；tool-calling/agent 能力最成熟 |
| 备选模型 | OpenAI 兼容（DeepSeek / Qwen 等） | `spring-ai-starter-model-openai`，配置 `base-url` 指向兼容端点 |
| 工具调用 | Spring AI `@Tool` / `ToolCallback` | 内置工具用注解暴露 |
| MCP | **`spring-ai-starter-mcp-client`** | 连接外部 MCP 服务，工具自动并入 |
| 流式 | Spring MVC 返回 `Flux<ServerSentEvent>`（或 `SseEmitter`） | `ChatClient.stream()` 返回 Flux |
| 鉴权 | 复用现有 Sa-Token | 浮动助手访问需新权限码 `ai:assistant:use` |

> **依赖落点**：新增业务模块 `apps/ai/fast-biz-ai`（沿用 `apps/<域>/fast-biz-<域>` 既有结构），由 `fast-application` 依赖。包名 `cc.oofo.ai.*`，在 `@SpringBootApplication` 默认扫描范围内。

---

## 4. 数据模型（新增表）

| 表 | 用途 | 关键字段 |
|---|---|---|
| `ai_chat_session` | 对话会话 | id, user_id, title, provider, model, created_at |
| `ai_chat_message` | 会话消息 | id, session_id, role(user/assistant/tool), content, tool_calls_json, created_at |
| `ai_tool_call_log` | **工具调用审计** | id, session_id, message_id, operator_id, tool_name, source(builtin/mcp), arguments_json, result_json, success, error_msg, cost_ms, created_at |
| `ai_mcp_server` | **MCP 服务配置** | id, name, transport(stdio/sse/streamable-http), command/url, args_json/headers_json, enabled, remark |
| `ai_setting` | 全局设置 | active_provider, active_model, system_prompt, enabled(总开关) |

> 沿用项目 MyBatis-Plus 约定（`is_deleted`、审计字段、下划线↔驼峰映射）。`ai_setting` 也可考虑并入现有 `sys_config`，落地时二选一。

---

## 5. 工具与权限模型

### 内置工具（v1 起步集）
每个工具是 `cc.oofo.ai.tools.*` 下的 `@Tool` 方法，内部调用**现有 Service**：

| 工具 | 能力 | 对应权限码（示例） |
|---|---|---|
| `queryUsers` | 按条件查用户 | `system:user:list` |
| `adjustUserPoints` | 给用户加/扣积分 | `uc:points:adjust` |
| `toggleUserStatus` | 启用/禁用用户 | `system:user:edit` |
| `queryAnalyticsOverview` | 查统计概览/趋势 | `analytics:report:view` |
| `queryApiCallLogs` | 查 API 调用日志 | `system:log:api:list` |
| `getSystemConfig` / `updateSystemConfig` | 读/改系统配置 | `system:config:*` |

> 起步集小而精，后续按需增量添加。

### 权限护栏（关键）
- **权限随人**：每个工具执行前 `StpUtil.checkPermission("<对应权限码>")`，即 **Agent 不能越权做当前登录管理员本人做不了的事**；缺权限 → 工具返回错误信息，模型据此告知用户。
- **身份传递**：流式在 reactive 线程执行，`ThreadLocal` 不一定可用 —— 在请求入口捕获登录态（loginId + 权限快照），封装为显式 `AgentContext` 传入工具执行；不依赖隐式上下文。
- **写操作全自动**：按用户要求不做逐步确认弹窗，但**每次工具调用全量写入 `ai_tool_call_log`**，且受总开关与权限双重约束。

---

## 6. 外部 MCP 配置

- **配置存储**：`ai_mcp_server` 表，后台提供管理页（CRUD，支持启用/停用，可配 1+ 个）。
- **传输类型**：`stdio`（本地命令）、`sse` / `streamable-http`（远程 URL + headers）。
- **运行时连接**：应用启动时按 `enabled=1` 的配置建立 Spring AI MCP Client 连接，将其工具并入 ChatClient 工具集；配置变更后提供「重载 MCP」动作（v1 可接受重载/重启生效，不强求热插拔）。
- **失败隔离**：单个 MCP 连接失败不影响内置工具与对话主链路，记录告警。

---

## 7. 流式接口契约

`POST /ai/agent/chat`（SSE）
```jsonc
// 请求
{ "sessionId": "可选，缺省新建", "message": "给 nurse-calendar-ios 这个用户加 100 积分" }

// SSE 事件（data 为 JSON）
{ "type": "session", "sessionId": "..." }      // 首帧返回会话 id
{ "type": "delta",   "text": "好的，正在..." }  // 文本增量
{ "type": "tool",    "name": "adjustUserPoints", "phase": "start", "args": {...} }
{ "type": "tool",    "name": "adjustUserPoints", "phase": "end",   "ok": true }
{ "type": "done",    "messageId": "..." }       // 结束
{ "type": "error",   "message": "..." }         // 异常
```
辅助接口：`GET /ai/agent/sessions`、`GET /ai/agent/sessions/{id}/messages`、`POST /ai/agent/sessions/{id}/clear`。

---

## 8. 模型切换与配置

- `application-ai.yml`：各 provider 的 key、默认 provider/model、`enabled`。
- 运行时切换：从 `ai_setting` 读取 `active_provider/active_model`，每次构建 ChatClient 时套用；切换不影响历史会话。
- 默认 `provider=anthropic`、`model=claude-opus-4-8`。

```yaml
# application-ai.yml（示意）
spring:
  ai:
    anthropic:
      api-key: ${ANTHROPIC_API_KEY}
      chat.options.model: claude-opus-4-8
    openai:                       # 切换到 OpenAI 兼容（DeepSeek/Qwen）时启用
      base-url: ${OPENAI_COMPAT_BASE_URL}
      api-key: ${OPENAI_COMPAT_KEY}
      chat.options.model: ${OPENAI_COMPAT_MODEL}
ai-assistant:
  enabled: true                   # 总开关
  max-tool-iterations: 8          # 单轮最大工具调用次数
```

---

## 9. 安全与错误处理

- **总开关**：`ai-assistant.enabled=false` 时前端隐藏入口、后端拒绝请求。
- **访问控制**：仅有 `ai:assistant:use` 权限的管理员可用；敏感工具再受各自权限码约束。
- **审计**：每次工具调用（含 MCP）落 `ai_tool_call_log`，记录操作人、入参、结果/错误、耗时。
- **循环上限**：`max-tool-iterations` 防止工具调用失控。
- **工具错误**：捕获后作为 tool result 回传模型，让其自我恢复或告知用户，不直接 500。
- **限流（可选）**：单会话/单用户速率与最大 token 限制，后续按需。
- **提示注入意识**：MCP/外部数据返回内容不得提升为指令权限；系统提示明确工具结果为数据而非命令。

---

## 10. 前端组件

- 全局挂载（主布局层），右下角 `FloatButton`；点击打开 `Drawer` 聊天面板。
- 组件：`AiAssistant/`（FAB + Drawer + 消息列表 + 工具调用时间线 + 输入框）。
- 通过 SSE 消费后端流；复用现有 `requestClient`（携带 Sa-Token）；token 通过 header 或查询参数传给 SSE。
- 新增 API 模块 `#/api/ai/agent`。

---

## 11. 测试策略（TDD）

- **工具单测**：mock 现有 Service + 校验权限分支（有权限执行、无权限拒绝、错误回传）。
- **会话/审计**：消息与 `ai_tool_call_log` 落库正确性。
- **Agent 编排集成测**：用 stub `ChatModel` 驱动一次「文本→工具→文本」往返，断言 SSE 事件序列。
- **MCP**：用本地 stub MCP server 验证工具并入与失败隔离。

---

## 12. 分期落地

1. **M1 骨架**：模块 + Spring AI ChatClient + SSE 接口 + 前端 FAB/抽屉 + 2 个只读工具（查用户、查统计），跑通端到端。
2. **M2 写能力 + 护栏**：加写工具（加积分、改状态）、权限校验、审计落库、总开关。
3. **M3 MCP**：`ai_mcp_server` 配置 + 管理页 + Spring AI MCP Client 接入。
4. **M4 模型切换 + 打磨**：provider 切换、错误处理、限流、UI 细节。

---

## 13. 风险与待确认

- **R1 Spring AI × Boot 3.5.11 兼容性**：Spring AI 1.0.x 主要面向 Boot 3.4；落地时需先验证 3.5.11 下可用（必要时 pin 到 1.0 最新补丁或评估升级路径）。
- **R2 reactive 上下文传递**：Sa-Token 登录态在流式/reactive 线程的传递需用显式 `AgentContext`，避免 `ThreadLocal` 丢失。
- **R3 全自动写操作风险**：无逐步确认，依赖「权限随人 + 审计 + 总开关」三道护栏；高危工具是否需要二次确认，后续可按工具粒度再评估。
- **R4 模型成本**：`claude-opus-4-8` 能力强但单价高；如成本敏感，可将默认切到更廉价模型或按场景分流（设计已支持切换）。

---

## 14. 开放问题（评审前请确认）
1. `ai_setting` 独立建表，还是并入现有 `sys_config`？
2. v1 内置工具起步集是否就按 §5 这 6 个？还有必须先有的运维场景吗？
3. 浮动助手是否仅限超管，还是所有有 `ai:assistant:use` 权限的角色？
