-- AI 运维助手管理模块：模型配置、MCP 服务配置、菜单权限。

CREATE TABLE IF NOT EXISTS `ai_model_config` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `name` varchar(64) NOT NULL COMMENT '配置名称',
  `provider` varchar(32) NOT NULL COMMENT '模型提供方：anthropic/openai/openai-compatible',
  `model` varchar(128) NOT NULL COMMENT '模型名称',
  `base_url` varchar(255) DEFAULT NULL COMMENT 'OpenAI 兼容接口地址',
  `api_key` varchar(512) DEFAULT NULL COMMENT 'API Key',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `active` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否当前模型',
  `temperature` decimal(4,2) DEFAULT NULL COMMENT '采样温度',
  `max_tokens` int DEFAULT NULL COMMENT '最大输出 token',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `last_latency_ms` bigint DEFAULT NULL COMMENT '上次测试延时(ms)',
  `last_test_ok` tinyint(1) DEFAULT NULL COMMENT '上次测试是否成功',
  `last_tested_at` datetime DEFAULT NULL COMMENT '上次测试时间',
  `created_by` varchar(64) DEFAULT NULL,
  `created_id` varchar(32) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_by` varchar(64) DEFAULT NULL,
  `updated_id` varchar(32) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_model_name` (`name`,`is_deleted`),
  KEY `idx_ai_model_active` (`active`),
  KEY `idx_ai_model_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 模型配置';

CREATE TABLE IF NOT EXISTS `ai_mcp_server` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `name` varchar(64) NOT NULL COMMENT '服务名称',
  `transport` varchar(32) NOT NULL COMMENT '传输类型：stdio/sse/streamable-http',
  `command` varchar(255) DEFAULT NULL COMMENT 'stdio 启动命令',
  `url` varchar(512) DEFAULT NULL COMMENT '远程 MCP 服务地址',
  `args_json` text COMMENT 'stdio 命令参数 JSON 数组',
  `headers_json` text COMMENT '远程连接请求头 JSON 对象',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_by` varchar(64) DEFAULT NULL,
  `created_id` varchar(32) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_by` varchar(64) DEFAULT NULL,
  `updated_id` varchar(32) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_mcp_name` (`name`,`is_deleted`),
  KEY `idx_ai_mcp_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI MCP 服务配置';

CREATE TABLE IF NOT EXISTS `ai_tool_config` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `name` varchar(64) NOT NULL COMMENT '展示名称',
  `tool_code` varchar(64) NOT NULL COMMENT '工具编码，暴露给模型的 tool name',
  `type` varchar(16) NOT NULL COMMENT '工具类型：sql/http',
  `description` varchar(512) NOT NULL COMMENT '工具说明，供模型判断调用时机',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `system_builtin` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否系统内置工具',
  `permission_code` varchar(64) DEFAULT NULL COMMENT '调用所需权限码，空表示登录用户均可调用',
  `method` varchar(16) DEFAULT NULL COMMENT 'HTTP 方法',
  `url` varchar(512) DEFAULT NULL COMMENT 'HTTP 地址模板，支持 {{param}} 占位',
  `headers_json` text COMMENT 'HTTP 请求头 JSON 对象',
  `body_template` text COMMENT 'HTTP 请求体模板',
  `sql_text` text COMMENT 'SQL 模板，使用 :param 命名参数',
  `read_only` tinyint(1) DEFAULT '1' COMMENT 'SQL 是否只读',
  `timeout_ms` int DEFAULT '10000' COMMENT '调用超时时间(ms)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_by` varchar(64) DEFAULT NULL,
  `created_id` varchar(32) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_by` varchar(64) DEFAULT NULL,
  `updated_id` varchar(32) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_tool_code` (`tool_code`,`is_deleted`),
  KEY `idx_ai_tool_enabled` (`enabled`)
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

SET @ai_chat_message_model_name_sql := IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'ai_chat_message'
     AND COLUMN_NAME = 'model_name') = 0,
  'ALTER TABLE `ai_chat_message` ADD COLUMN `model_name` varchar(128) DEFAULT NULL COMMENT ''助手消息使用的模型配置名称'' AFTER `process_json`',
  'SELECT 1'
);
PREPARE ai_chat_message_model_name_stmt FROM @ai_chat_message_model_name_sql;
EXECUTE ai_chat_message_model_name_stmt;
DEALLOCATE PREPARE ai_chat_message_model_name_stmt;

SET @ai_chat_message_model_provider_sql := IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'ai_chat_message'
     AND COLUMN_NAME = 'model_provider') = 0,
  'ALTER TABLE `ai_chat_message` ADD COLUMN `model_provider` varchar(32) DEFAULT NULL COMMENT ''助手消息使用的模型提供方'' AFTER `model_name`',
  'SELECT 1'
);
PREPARE ai_chat_message_model_provider_stmt FROM @ai_chat_message_model_provider_sql;
EXECUTE ai_chat_message_model_provider_stmt;
DEALLOCATE PREPARE ai_chat_message_model_provider_stmt;

SET @ai_chat_message_model_code_sql := IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'ai_chat_message'
     AND COLUMN_NAME = 'model_code') = 0,
  'ALTER TABLE `ai_chat_message` ADD COLUMN `model_code` varchar(128) DEFAULT NULL COMMENT ''助手消息使用的模型编码'' AFTER `model_provider`',
  'SELECT 1'
);
PREPARE ai_chat_message_model_code_stmt FROM @ai_chat_message_model_code_sql;
EXECUTE ai_chat_message_model_code_stmt;
DEALLOCATE PREPARE ai_chat_message_model_code_stmt;

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
('ai_ops', NULL, 'AiOps', NULL, 2, 1, '/ai', NULL, NULL, 'lucide:bot',
 NULL, 'AI 运维', 9998, 0, 0, 0, 0, 0, 0,
 NULL, NULL, NULL, NULL, NULL, 'AI 运维管理目录',
 'system', NOW(), 'system', NOW(), NULL, NULL, 0),
('ai_model', 'ai_ops', 'AiModel', 'ai:model:list', 1, 1, '/ai/model', NULL, 'ai/model/list', 'lucide:brain-circuit',
 NULL, '模型管理', 10, 0, 0, 0, 0, 0, 0,
 NULL, NULL, NULL, NULL, NULL, 'AI 模型配置管理',
 'system', NOW(), 'system', NOW(), NULL, NULL, 0),
('ai_mcp', 'ai_ops', 'AiMcp', 'ai:mcp:list', 1, 1, '/ai/mcp', NULL, 'ai/mcp/list', 'lucide:plug-zap',
 NULL, 'MCP 管理', 20, 0, 0, 0, 0, 0, 0,
 NULL, NULL, NULL, NULL, NULL, 'MCP 服务配置管理',
 'system', NOW(), 'system', NOW(), NULL, NULL, 0),
('ai_model_add', 'ai_model', 'AiModelAdd', 'ai:model:add', 3, 1, NULL, NULL, NULL, NULL,
 NULL, '新增', 0, 0, 0, 0, 0, 0, 0,
 NULL, NULL, NULL, NULL, NULL, NULL,
 'system', NOW(), 'system', NOW(), NULL, NULL, 0),
('ai_model_edit', 'ai_model', 'AiModelEdit', 'ai:model:edit', 3, 1, NULL, NULL, NULL, NULL,
 NULL, '编辑', 1, 0, 0, 0, 0, 0, 0,
 NULL, NULL, NULL, NULL, NULL, NULL,
 'system', NOW(), 'system', NOW(), NULL, NULL, 0),
('ai_model_del', 'ai_model', 'AiModelDelete', 'ai:model:delete', 3, 1, NULL, NULL, NULL, NULL,
 NULL, '删除', 2, 0, 0, 0, 0, 0, 0,
 NULL, NULL, NULL, NULL, NULL, NULL,
 'system', NOW(), 'system', NOW(), NULL, NULL, 0),
('ai_model_active', 'ai_model', 'AiModelActivate', 'ai:model:activate', 3, 1, NULL, NULL, NULL, NULL,
 NULL, '设为当前', 3, 0, 0, 0, 0, 0, 0,
 NULL, NULL, NULL, NULL, NULL, NULL,
 'system', NOW(), 'system', NOW(), NULL, NULL, 0),
('ai_mcp_add', 'ai_mcp', 'AiMcpAdd', 'ai:mcp:add', 3, 1, NULL, NULL, NULL, NULL,
 NULL, '新增', 0, 0, 0, 0, 0, 0, 0,
 NULL, NULL, NULL, NULL, NULL, NULL,
 'system', NOW(), 'system', NOW(), NULL, NULL, 0),
('ai_mcp_edit', 'ai_mcp', 'AiMcpEdit', 'ai:mcp:edit', 3, 1, NULL, NULL, NULL, NULL,
 NULL, '编辑', 1, 0, 0, 0, 0, 0, 0,
 NULL, NULL, NULL, NULL, NULL, NULL,
 'system', NOW(), 'system', NOW(), NULL, NULL, 0),
('ai_mcp_del', 'ai_mcp', 'AiMcpDelete', 'ai:mcp:delete', 3, 1, NULL, NULL, NULL, NULL,
 NULL, '删除', 2, 0, 0, 0, 0, 0, 0,
 NULL, NULL, NULL, NULL, NULL, NULL,
 'system', NOW(), 'system', NOW(), NULL, NULL, 0),
('ai_tool', 'ai_ops', 'AiTool', 'ai:tool:list', 1, 1, '/ai/tool', NULL, 'ai/tool/list', 'lucide:wrench',
 NULL, '工具管理', 30, 0, 0, 0, 0, 0, 0,
 NULL, NULL, NULL, NULL, NULL, 'AI 工具配置管理',
 'system', NOW(), 'system', NOW(), NULL, NULL, 0),
('ai_tool_add', 'ai_tool', 'AiToolAdd', 'ai:tool:add', 3, 1, NULL, NULL, NULL, NULL,
 NULL, '新增', 0, 0, 0, 0, 0, 0, 0,
 NULL, NULL, NULL, NULL, NULL, NULL,
 'system', NOW(), 'system', NOW(), NULL, NULL, 0),
('ai_tool_edit', 'ai_tool', 'AiToolEdit', 'ai:tool:edit', 3, 1, NULL, NULL, NULL, NULL,
 NULL, '编辑', 1, 0, 0, 0, 0, 0, 0,
 NULL, NULL, NULL, NULL, NULL, NULL,
 'system', NOW(), 'system', NOW(), NULL, NULL, 0),
('ai_sql_readonly', 'ai_tool', 'AiSqlReadonly', 'ai:sql:readonly', 3, 1, NULL, NULL, NULL, NULL,
 NULL, 'AI只读SQL', 10, 0, 0, 0, 0, 0, 0,
 NULL, NULL, NULL, NULL, NULL, '允许 AI 调用内置只读 SQL 工具',
 'system', NOW(), 'system', NOW(), NULL, NULL, 0),
('ai_tool_del', 'ai_tool', 'AiToolDelete', 'ai:tool:delete', 3, 1, NULL, NULL, NULL, NULL,
 NULL, '删除', 2, 0, 0, 0, 0, 0, 0,
 NULL, NULL, NULL, NULL, NULL, NULL,
 'system', NOW(), 'system', NOW(), NULL, NULL, 0)
ON DUPLICATE KEY UPDATE
  `pid` = VALUES(`pid`),
  `code` = VALUES(`code`),
  `type` = VALUES(`type`),
  `status` = VALUES(`status`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `icon` = VALUES(`icon`),
  `meta_title` = VALUES(`meta_title`),
  `meta_order` = VALUES(`meta_order`),
  `remark` = VALUES(`remark`),
  `updated_by` = VALUES(`updated_by`),
  `updated_at` = VALUES(`updated_at`),
  `is_deleted` = 0;

INSERT IGNORE INTO `sys_roles_menus` (`role_id`, `menu_id`)
SELECT r.`id`, m.`id`
FROM `sys_role` r
JOIN `sys_menu` m ON m.`id` IN (
  'ai_ops',
  'ai_model',
  'ai_mcp',
  'ai_model_add',
  'ai_model_edit',
  'ai_model_del',
  'ai_model_active',
  'ai_mcp_add',
  'ai_mcp_edit',
  'ai_mcp_del',
  'ai_tool',
  'ai_tool_add',
  'ai_tool_edit',
  'ai_sql_readonly',
  'ai_tool_del'
)
WHERE r.`code` = 'System' AND r.`is_deleted` = 0;
