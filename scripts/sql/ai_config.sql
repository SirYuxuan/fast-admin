SET NAMES utf8mb4;
USE `fast_admin`;

INSERT IGNORE INTO `sys_config`
(`id`, `config_name`, `config_key`, `config_value`, `config_type`, `remark`, `created_by`, `created_at`, `updated_by`, `updated_at`, `is_deleted`)
VALUES
('ai_assistant_enabled','AI助手开关','ai.assistant.enabled','true',1,'控制 AI 运维助手是否启用','system',NOW(),'system',NOW(),0),
('ai_assistant_require_permission','AI助手使用权限校验','ai.assistant.require-permission','false',1,'控制使用 AI 运维助手时是否校验 ai:assistant:use 权限码','system',NOW(),'system',NOW(),0),
('ai_assistant_max_tool_iterations','AI助手最大工具轮次','ai.assistant.max-tool-iterations','8',1,'单轮对话最大工具调用轮次，防止工具调用失控','system',NOW(),'system',NOW(),0),
('ai_assistant_system_prompt','AI助手系统提示词','ai.assistant.system-prompt','你是 Fast Admin 后台的 AI 运维助手。\n回答要简洁、准确；当你无法确认后台事实时，明确说明需要工具或数据支持。\n当前版本仅支持对话，不得声称已经执行后台写操作。',1,'AI 运维助手的基础系统提示词','system',NOW(),'system',NOW(),0),
('ai_mcp_client_enabled','AI MCP 客户端开关','ai.mcp.client.enabled','true',1,'控制 AI 对话是否允许加载已启用的 MCP 服务','system',NOW(),'system',NOW(),0),
('ai_rag_enabled','AI 知识库开关','ai.rag.enabled','true',1,'控制 AI 知识库 / RAG 功能是否启用','system',NOW(),'system',NOW(),0),
('ai_rag_collection_name','AI 知识库 Qdrant 集合名','ai.rag.collection-name','fast_admin_rag',1,'AI 知识库写入 Qdrant 时使用的集合名','system',NOW(),'system',NOW(),0),
('ai_rag_qdrant_url','AI 知识库 Qdrant URL','ai.rag.qdrant.url','http://100.115.97.59:6333',1,'Qdrant REST 地址','system',NOW(),'system',NOW(),0),
('ai_rag_qdrant_api_key','AI 知识库 Qdrant API Key','ai.rag.qdrant.api-key','',1,'Qdrant API Key，未开启鉴权时留空','system',NOW(),'system',NOW(),0),
('ai_rag_qdrant_timeout_ms','AI 知识库 Qdrant 超时','ai.rag.qdrant.timeout-ms','5000',1,'Qdrant 请求超时时间，单位毫秒','system',NOW(),'system',NOW(),0),
('ai_rag_embedding_base_url','AI 知识库 Embedding Base URL','ai.rag.embedding.base-url','',1,'OpenAI 兼容 Embedding Base URL','system',NOW(),'system',NOW(),0),
('ai_rag_embedding_api_key','AI 知识库 Embedding API Key','ai.rag.embedding.api-key','',1,'Embedding 服务 API Key','system',NOW(),'system',NOW(),0),
('ai_rag_embedding_model','AI 知识库 Embedding 模型','ai.rag.embedding.model','text-embedding-3-small',1,'Embedding 模型名称','system',NOW(),'system',NOW(),0),
('ai_rag_embedding_timeout_ms','AI 知识库 Embedding 超时','ai.rag.embedding.timeout-ms','20000',1,'Embedding 请求超时时间，单位毫秒','system',NOW(),'system',NOW(),0);

INSERT IGNORE INTO `sys_menu`
(`id`, `pid`, `name`, `code`, `type`, `status`, `path`, `active_path`, `component`, `icon`, `meta_active_icon`, `meta_title`, `meta_order`, `meta_affix_tab`, `meta_keep_alive`, `meta_hide_in_menu`, `meta_hide_children_in_menu`, `meta_hide_in_breadcrumb`, `meta_hide_in_tab`, `meta_badge`, `meta_badge_type`, `meta_badge_variants`, `meta_iframe_src`, `meta_link`, `remark`, `created_by`, `created_at`, `updated_by`, `updated_at`, `created_id`, `updated_id`, `is_deleted`)
VALUES
('ai_config','ai_ops','AiConfig','ai:config:list',1,1,'/ai/config',NULL,'ai/config/index','lucide:sliders-horizontal',NULL,'AI 配置',5,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'AI 运维配置','system',NOW(),'system',NOW(),NULL,NULL,0),
('ai_config_edit','ai_config','AiConfigEdit','ai:config:edit',3,1,NULL,NULL,NULL,NULL,NULL,'编辑',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,'system',NOW(),'system',NOW(),NULL,NULL,0);

INSERT IGNORE INTO `sys_roles_menus` (`role_id`, `menu_id`)
SELECT '35PlNb9zFOIQVWx8YoSlGGxjhHF', m.id
FROM `sys_menu` m
WHERE m.id IN ('ai_config','ai_config_edit');
