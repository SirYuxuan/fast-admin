-- =============================================================
-- Fast Admin AI 模块建表脚本
-- 生成日期：2026-06-16
-- 说明：若表已存在则跳过（CREATE TABLE IF NOT EXISTS）
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- AI 模型配置
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ai_model_config` (
  `id`            varchar(36)    NOT NULL                    COMMENT '主键',
  `name`          varchar(100)   NOT NULL                    COMMENT '配置名称',
  `provider`      varchar(50)    NOT NULL                    COMMENT '提供方：anthropic / openai / openai-compatible',
  `model`         varchar(100)   NOT NULL                    COMMENT '模型名称',
  `base_url`      varchar(500)   DEFAULT NULL                COMMENT 'OpenAI 兼容接口地址',
  `api_key`       varchar(500)   DEFAULT NULL                COMMENT 'API Key（存储时建议加密）',
  `enabled`       tinyint(1)     NOT NULL DEFAULT 1          COMMENT '是否启用',
  `active`        tinyint(1)     NOT NULL DEFAULT 0          COMMENT '是否为当前激活模型',
  `temperature`   double         DEFAULT NULL                COMMENT '采样温度',
  `max_tokens`    int            DEFAULT NULL                COMMENT '最大输出 token',
  `remark`        varchar(500)   DEFAULT NULL                COMMENT '备注',
  `last_latency_ms` bigint       DEFAULT NULL                COMMENT '上次测试延时(ms)',
  `last_test_ok`  tinyint(1)     DEFAULT NULL                COMMENT '上次测试是否成功',
  `last_tested_at` datetime      DEFAULT NULL                COMMENT '上次测试时间',
  `created_by`    varchar(100)   DEFAULT NULL                COMMENT '创建人',
  `created_id`    varchar(100)   DEFAULT NULL                COMMENT '创建人ID',
  `created_at`    datetime       DEFAULT NULL                COMMENT '创建时间',
  `updated_by`    varchar(100)   DEFAULT NULL                COMMENT '更新人',
  `updated_id`    varchar(100)   DEFAULT NULL                COMMENT '更新人ID',
  `updated_at`    datetime       DEFAULT NULL                COMMENT '更新时间',
  `is_deleted`    tinyint(1)     NOT NULL DEFAULT 0          COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 模型配置';

-- ----------------------------
-- MCP 服务配置
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ai_mcp_server` (
  `id`            varchar(36)    NOT NULL                    COMMENT '主键',
  `name`          varchar(100)   NOT NULL                    COMMENT 'MCP 服务名称',
  `transport`     varchar(50)    NOT NULL                    COMMENT '传输类型：stdio / sse / streamable-http',
  `command`       varchar(500)   DEFAULT NULL                COMMENT 'stdio 启动命令',
  `url`           varchar(500)   DEFAULT NULL                COMMENT 'sse/streamable-http 地址',
  `args_json`     text           DEFAULT NULL                COMMENT '命令参数 JSON 数组',
  `headers_json`  text           DEFAULT NULL                COMMENT '请求头 JSON 对象',
  `enabled`       tinyint(1)     NOT NULL DEFAULT 1          COMMENT '是否启用',
  `remark`        varchar(500)   DEFAULT NULL                COMMENT '备注',
  `created_by`    varchar(100)   DEFAULT NULL,
  `created_id`    varchar(100)   DEFAULT NULL,
  `created_at`    datetime       DEFAULT NULL,
  `updated_by`    varchar(100)   DEFAULT NULL,
  `updated_id`    varchar(100)   DEFAULT NULL,
  `updated_at`    datetime       DEFAULT NULL,
  `is_deleted`    tinyint(1)     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP 服务配置';

-- ----------------------------
-- AI 工具配置
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ai_tool_config` (
  `id`              varchar(36)   NOT NULL                   COMMENT '主键',
  `name`            varchar(100)  NOT NULL                   COMMENT '工具名称',
  `tool_code`       varchar(64)   NOT NULL                   COMMENT '工具编码（唯一，作为 tool name 传给模型）',
  `type`            varchar(20)   NOT NULL                   COMMENT '类型：sql / http',
  `description`     varchar(500)  NOT NULL                   COMMENT '工具说明（传给模型）',
  `enabled`         tinyint(1)    NOT NULL DEFAULT 1         COMMENT '是否启用',
  `system_builtin`  tinyint(1)    NOT NULL DEFAULT 0         COMMENT '是否系统内置工具',
  `permission_code` varchar(200)  DEFAULT NULL               COMMENT '调用所需权限码，空表示登录即可',
  `method`          varchar(10)   DEFAULT NULL               COMMENT 'HTTP 方法',
  `url`             varchar(500)  DEFAULT NULL               COMMENT 'HTTP 地址模板',
  `headers_json`    text          DEFAULT NULL               COMMENT 'HTTP 请求头 JSON',
  `body_template`   text          DEFAULT NULL               COMMENT 'HTTP 请求体模板',
  `sql_text`        text          DEFAULT NULL               COMMENT 'SQL 模板',
  `read_only`       tinyint(1)    DEFAULT 1                  COMMENT 'SQL 是否只读',
  `timeout_ms`      int           DEFAULT 10000              COMMENT '超时时间(ms)',
  `remark`          varchar(500)  DEFAULT NULL               COMMENT '备注',
  `created_by`      varchar(100)  DEFAULT NULL,
  `created_id`      varchar(100)  DEFAULT NULL,
  `created_at`      datetime      DEFAULT NULL,
  `updated_by`      varchar(100)  DEFAULT NULL,
  `updated_id`      varchar(100)  DEFAULT NULL,
  `updated_at`      datetime      DEFAULT NULL,
  `is_deleted`      tinyint(1)    NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tool_code` (`tool_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 工具配置';

SET @ai_tool_system_builtin_sql := IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'ai_tool_config'
     AND COLUMN_NAME = 'system_builtin') = 0,
  'ALTER TABLE `ai_tool_config` ADD COLUMN `system_builtin` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否系统内置工具'' AFTER `enabled`',
  'SELECT 1'
);
PREPARE ai_tool_system_builtin_stmt FROM @ai_tool_system_builtin_sql;
EXECUTE ai_tool_system_builtin_stmt;
DEALLOCATE PREPARE ai_tool_system_builtin_stmt;

INSERT INTO `ai_tool_config`
(`id`, `name`, `tool_code`, `type`, `description`, `enabled`, `system_builtin`, `permission_code`,
 `method`, `url`, `headers_json`, `body_template`, `sql_text`, `read_only`, `timeout_ms`, `remark`,
 `created_by`, `created_at`, `updated_by`, `updated_at`, `is_deleted`)
VALUES
('builtin_execute_readonly_sql', '执行只读 SQL', 'execute_readonly_sql', 'sql',
 '系统内置工具：执行单条只读 SQL，返回 JSON 结果。仅允许 select/show/desc/describe/explain。',
 1, 1, 'ai:sql:readonly',
 NULL, NULL, NULL, NULL, NULL, 1, 10000, '系统内置，配置项来自系统参数表',
 'system', NOW(), 'system', NOW(), 0)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `type` = VALUES(`type`),
  `description` = VALUES(`description`),
  `system_builtin` = 1,
  `permission_code` = VALUES(`permission_code`),
  `read_only` = VALUES(`read_only`),
  `timeout_ms` = VALUES(`timeout_ms`),
  `remark` = VALUES(`remark`),
  `updated_by` = VALUES(`updated_by`),
  `updated_at` = VALUES(`updated_at`),
  `is_deleted` = 0;

-- ----------------------------
-- AI 对话会话
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ai_chat_session` (
  `id`          varchar(36)   NOT NULL                       COMMENT '主键',
  `session_id`  varchar(100)  NOT NULL                       COMMENT '会话业务 ID（前端生成）',
  `user_id`     varchar(100)  DEFAULT NULL                   COMMENT '所属用户 ID',
  `title`       varchar(100)  DEFAULT NULL                   COMMENT '会话标题（取首条消息）',
  `created_by`  varchar(100)  DEFAULT NULL,
  `created_id`  varchar(100)  DEFAULT NULL,
  `created_at`  datetime      DEFAULT NULL,
  `updated_by`  varchar(100)  DEFAULT NULL,
  `updated_id`  varchar(100)  DEFAULT NULL,
  `updated_at`  datetime      DEFAULT NULL,
  `is_deleted`  tinyint(1)    NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 对话会话';

-- ----------------------------
-- AI 对话消息
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ai_chat_message` (
  `id`          varchar(36)   NOT NULL                       COMMENT '主键',
  `session_id`  varchar(100)  NOT NULL                       COMMENT '会话业务 ID',
  `role`        varchar(20)   NOT NULL                       COMMENT '角色：user / assistant',
  `content`     longtext      DEFAULT NULL                   COMMENT '消息内容',
  `process_json` longtext     DEFAULT NULL                   COMMENT '助手消息的思考与工具过程 JSON',
  `created_by`  varchar(100)  DEFAULT NULL,
  `created_id`  varchar(100)  DEFAULT NULL,
  `created_at`  datetime      DEFAULT NULL,
  `updated_by`  varchar(100)  DEFAULT NULL,
  `updated_id`  varchar(100)  DEFAULT NULL,
  `updated_at`  datetime      DEFAULT NULL,
  `is_deleted`  tinyint(1)    NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 对话消息';

SET @ai_chat_message_process_json_exists := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ai_chat_message'
    AND COLUMN_NAME = 'process_json'
);
SET @ai_chat_message_process_json_sql := IF(
  @ai_chat_message_process_json_exists = 0,
  'ALTER TABLE `ai_chat_message` ADD COLUMN `process_json` longtext DEFAULT NULL COMMENT ''助手消息的思考与工具过程 JSON'' AFTER `content`',
  'SELECT 1'
);
PREPARE ai_chat_message_process_json_stmt FROM @ai_chat_message_process_json_sql;
EXECUTE ai_chat_message_process_json_stmt;
DEALLOCATE PREPARE ai_chat_message_process_json_stmt;

-- ----------------------------
-- AI 工具调用审计日志
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ai_tool_call_log` (
  `id`              varchar(36)   NOT NULL                   COMMENT '主键',
  `session_id`      varchar(100)  DEFAULT NULL               COMMENT '对话会话业务 ID',
  `operator_id`     varchar(100)  DEFAULT NULL               COMMENT '操作人用户 ID',
  `tool_name`       varchar(64)   NOT NULL                   COMMENT '工具编码/名称',
  `source`          varchar(20)   NOT NULL DEFAULT 'builtin' COMMENT '工具来源：builtin / mcp',
  `arguments_json`  text          DEFAULT NULL               COMMENT '调用入参 JSON',
  `result_json`     text          DEFAULT NULL               COMMENT '执行结果（截断至 4000 字符）',
  `success`         tinyint(1)    NOT NULL DEFAULT 1         COMMENT '是否成功',
  `error_msg`       varchar(500)  DEFAULT NULL               COMMENT '失败错误信息',
  `cost_ms`         bigint        DEFAULT NULL               COMMENT '执行耗时(ms)',
  `created_by`      varchar(100)  DEFAULT NULL,
  `created_id`      varchar(100)  DEFAULT NULL,
  `created_at`      datetime      DEFAULT NULL,
  `updated_by`      varchar(100)  DEFAULT NULL,
  `updated_id`      varchar(100)  DEFAULT NULL,
  `updated_at`      datetime      DEFAULT NULL,
  `is_deleted`      tinyint(1)    NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_tool_name` (`tool_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 工具调用审计日志';

-- ----------------------------
-- AI 只读 SQL 内置工具系统参数
-- ----------------------------
INSERT INTO `sys_config`
(`id`, `config_name`, `config_key`, `config_value`, `config_type`, `remark`,
 `created_by`, `created_at`, `updated_by`, `updated_at`, `is_deleted`)
VALUES
('ai_readonly_sql_enabled', 'AI只读SQL工具开关', 'ai.readonly-sql.enabled', 'true', 1,
 '控制内置 execute_readonly_sql 工具是否注册给模型',
 'system', NOW(), 'system', NOW(), 0),
('ai_readonly_sql_perm', 'AI只读SQL工具权限码', 'ai.readonly-sql.permission-code', 'ai:sql:readonly', 1,
 '调用内置 execute_readonly_sql 工具需要的权限码',
 'system', NOW(), 'system', NOW(), 0),
('ai_readonly_sql_max_rows', 'AI只读SQL最大返回行数', 'ai.readonly-sql.max-rows', '100', 1,
 '内置 execute_readonly_sql 工具单次最多返回行数，代码层最大 100',
 'system', NOW(), 'system', NOW(), 0)
ON DUPLICATE KEY UPDATE
  `config_name` = VALUES(`config_name`),
  `config_type` = VALUES(`config_type`),
  `remark` = VALUES(`remark`),
  `updated_by` = VALUES(`updated_by`),
  `updated_at` = VALUES(`updated_at`),
  `is_deleted` = 0;

INSERT INTO `sys_menu`
(`id`, `pid`, `name`, `code`, `type`, `status`, `path`, `active_path`, `component`, `icon`,
 `meta_active_icon`, `meta_title`, `meta_order`, `meta_affix_tab`, `meta_keep_alive`,
 `meta_hide_in_menu`, `meta_hide_children_in_menu`, `meta_hide_in_breadcrumb`, `meta_hide_in_tab`,
 `meta_badge`, `meta_badge_type`, `meta_badge_variants`, `meta_iframe_src`, `meta_link`, `remark`,
 `created_by`, `created_at`, `updated_by`, `updated_at`, `created_id`, `updated_id`, `is_deleted`)
VALUES
('ai_sql_readonly', 'ai_tool', 'AiSqlReadonly', 'ai:sql:readonly', 3, 1, NULL, NULL, NULL, NULL,
 NULL, 'AI只读SQL', 10, 0, 0, 0, 0, 0, 0,
 NULL, NULL, NULL, NULL, NULL, '允许 AI 调用内置只读 SQL 工具',
 'system', NOW(), 'system', NOW(), NULL, NULL, 0)
ON DUPLICATE KEY UPDATE
  `pid` = VALUES(`pid`),
  `code` = VALUES(`code`),
  `type` = VALUES(`type`),
  `status` = VALUES(`status`),
  `meta_title` = VALUES(`meta_title`),
  `meta_order` = VALUES(`meta_order`),
  `remark` = VALUES(`remark`),
  `updated_by` = VALUES(`updated_by`),
  `updated_at` = VALUES(`updated_at`),
  `is_deleted` = 0;

INSERT IGNORE INTO `sys_roles_menus` (`role_id`, `menu_id`)
SELECT r.`id`, 'ai_sql_readonly'
FROM `sys_role` r
WHERE r.`code` = 'System' AND r.`is_deleted` = 0;

SET FOREIGN_KEY_CHECKS = 1;
