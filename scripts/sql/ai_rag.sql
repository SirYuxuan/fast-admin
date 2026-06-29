SET NAMES utf8mb4;
USE `fast_admin`;

CREATE TABLE IF NOT EXISTS `ai_knowledge_base` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `name` varchar(100) NOT NULL COMMENT '知识库名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `chunk_size` int NOT NULL DEFAULT '800' COMMENT '切片长度',
  `chunk_overlap` int NOT NULL DEFAULT '100' COMMENT '切片重叠长度',
  `chunk_delimiter` varchar(100) NOT NULL DEFAULT '\\n\\n' COMMENT '切片分隔符',
  `document_count` int NOT NULL DEFAULT '0' COMMENT '文档数',
  `chunk_count` int NOT NULL DEFAULT '0' COMMENT '切片数',
  `last_indexed_at` datetime DEFAULT NULL COMMENT '最后索引时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_by` varchar(64) DEFAULT NULL,
  `created_id` varchar(32) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_by` varchar(64) DEFAULT NULL,
  `updated_id` varchar(32) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_kb_name` (`name`,`is_deleted`),
  KEY `idx_ai_kb_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 知识库';

SET @has_ai_kb_chunk_delimiter := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ai_knowledge_base'
    AND COLUMN_NAME = 'chunk_delimiter'
);
SET @ddl_ai_kb_chunk_delimiter := IF(
  @has_ai_kb_chunk_delimiter = 0,
  'ALTER TABLE `ai_knowledge_base` ADD COLUMN `chunk_delimiter` varchar(100) NOT NULL DEFAULT ''\\\\n\\\\n'' COMMENT ''切片分隔符'' AFTER `chunk_overlap`',
  'SELECT 1'
);
PREPARE stmt_ai_kb_chunk_delimiter FROM @ddl_ai_kb_chunk_delimiter;
EXECUTE stmt_ai_kb_chunk_delimiter;
DEALLOCATE PREPARE stmt_ai_kb_chunk_delimiter;

CREATE TABLE IF NOT EXISTS `ai_knowledge_document` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `knowledge_base_id` varchar(32) NOT NULL COMMENT '知识库ID',
  `file_id` varchar(32) NOT NULL COMMENT '系统文件ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `content_type` varchar(128) DEFAULT NULL COMMENT '文件类型',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/indexing/indexed/failed',
  `chunk_count` int NOT NULL DEFAULT '0' COMMENT '切片数',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `indexed_at` datetime DEFAULT NULL COMMENT '索引完成时间',
  `created_by` varchar(64) DEFAULT NULL,
  `created_id` varchar(32) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_by` varchar(64) DEFAULT NULL,
  `updated_id` varchar(32) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_ai_doc_kb` (`knowledge_base_id`),
  KEY `idx_ai_doc_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 知识库文档';

CREATE TABLE IF NOT EXISTS `ai_knowledge_chunk` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `knowledge_base_id` varchar(32) NOT NULL COMMENT '知识库ID',
  `document_id` varchar(32) NOT NULL COMMENT '文档ID',
  `point_id` varchar(64) NOT NULL COMMENT 'Qdrant Point ID',
  `chunk_index` int NOT NULL COMMENT '切片序号',
  `token_count` int DEFAULT NULL COMMENT '估算 token 数',
  `content` mediumtext NOT NULL COMMENT '切片文本',
  `created_by` varchar(64) DEFAULT NULL,
  `created_id` varchar(32) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_by` varchar(64) DEFAULT NULL,
  `updated_id` varchar(32) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_chunk_point` (`point_id`,`is_deleted`),
  KEY `idx_ai_chunk_kb` (`knowledge_base_id`),
  KEY `idx_ai_chunk_doc` (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 知识库切片';

INSERT IGNORE INTO `sys_menu` (`id`, `pid`, `name`, `code`, `type`, `status`, `path`, `active_path`, `component`, `icon`, `meta_active_icon`, `meta_title`, `meta_order`, `meta_affix_tab`, `meta_keep_alive`, `meta_hide_in_menu`, `meta_hide_children_in_menu`, `meta_hide_in_breadcrumb`, `meta_hide_in_tab`, `meta_badge`, `meta_badge_type`, `meta_badge_variants`, `meta_iframe_src`, `meta_link`, `remark`, `created_by`, `created_at`, `updated_by`, `updated_at`, `created_id`, `updated_id`, `is_deleted`) VALUES
('ai_rag','ai_ops','AiRag','ai:rag:list',1,1,'/ai/rag',NULL,'ai/rag/list','lucide:database-zap',NULL,'知识库',60,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'AI 知识库管理','system',NOW(),'system',NOW(),NULL,NULL,0),
('ai_rag_add','ai_rag','AiRagAdd','ai:rag:add',3,1,NULL,NULL,NULL,NULL,NULL,'新增',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,'system',NOW(),'system',NOW(),NULL,NULL,0),
('ai_rag_edit','ai_rag','AiRagEdit','ai:rag:edit',3,1,NULL,NULL,NULL,NULL,NULL,'编辑',1,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,'system',NOW(),'system',NOW(),NULL,NULL,0),
('ai_rag_delete','ai_rag','AiRagDelete','ai:rag:delete',3,1,NULL,NULL,NULL,NULL,NULL,'删除',2,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,'system',NOW(),'system',NOW(),NULL,NULL,0),
('ai_rag_upload','ai_rag','AiRagUpload','ai:rag:upload',3,1,NULL,NULL,NULL,NULL,NULL,'上传文档',3,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,'system',NOW(),'system',NOW(),NULL,NULL,0),
('ai_rag_reindex','ai_rag','AiRagReindex','ai:rag:reindex',3,1,NULL,NULL,NULL,NULL,NULL,'重建索引',4,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,'system',NOW(),'system',NOW(),NULL,NULL,0),
('ai_rag_recall','ai_rag','AiRagRecall','ai:rag:recall',3,1,NULL,NULL,NULL,NULL,NULL,'召回测试',5,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,'system',NOW(),'system',NOW(),NULL,NULL,0);

-- 知识库详情页：隐藏菜单，挂在 ai_ops 目录下（与列表同级，避免成为 ai_rag 的子级而把 /ai/rag 变成重定向）
INSERT IGNORE INTO `sys_menu` (`id`, `pid`, `name`, `code`, `type`, `status`, `path`, `active_path`, `component`, `icon`, `meta_active_icon`, `meta_title`, `meta_order`, `meta_affix_tab`, `meta_keep_alive`, `meta_hide_in_menu`, `meta_hide_children_in_menu`, `meta_hide_in_breadcrumb`, `meta_hide_in_tab`, `meta_badge`, `meta_badge_type`, `meta_badge_variants`, `meta_iframe_src`, `meta_link`, `remark`, `created_by`, `created_at`, `updated_by`, `updated_at`, `created_id`, `updated_id`, `is_deleted`) VALUES
('ai_rag_detail','ai_ops','AiRagDetail','ai:rag:list',1,1,'/ai/rag/:id','/ai/rag','ai/rag/detail','lucide:database-zap',NULL,'知识库详情',61,0,0,1,0,0,0,NULL,NULL,NULL,NULL,NULL,'AI 知识库详情','system',NOW(),'system',NOW(),NULL,NULL,0);

-- 文档切片页：隐藏菜单
INSERT IGNORE INTO `sys_menu` (`id`, `pid`, `name`, `code`, `type`, `status`, `path`, `active_path`, `component`, `icon`, `meta_active_icon`, `meta_title`, `meta_order`, `meta_affix_tab`, `meta_keep_alive`, `meta_hide_in_menu`, `meta_hide_children_in_menu`, `meta_hide_in_breadcrumb`, `meta_hide_in_tab`, `meta_badge`, `meta_badge_type`, `meta_badge_variants`, `meta_iframe_src`, `meta_link`, `remark`, `created_by`, `created_at`, `updated_by`, `updated_at`, `created_id`, `updated_id`, `is_deleted`) VALUES
('ai_rag_chunks','ai_ops','AiRagChunks','ai:rag:list',1,1,'/ai/rag/:id/documents/:documentId/chunks','/ai/rag','ai/rag/chunks','lucide:file-text',NULL,'文档切片',62,0,0,1,0,0,0,NULL,NULL,NULL,NULL,NULL,'AI 知识库文档切片','system',NOW(),'system',NOW(),NULL,NULL,0);

INSERT IGNORE INTO `sys_roles_menus` (`role_id`, `menu_id`)
SELECT '35PlNb9zFOIQVWx8YoSlGGxjhHF', m.id
FROM `sys_menu` m
WHERE m.id IN ('ai_rag','ai_rag_add','ai_rag_edit','ai_rag_delete','ai_rag_upload','ai_rag_reindex','ai_rag_recall');

-- 详情页隐藏菜单绑定给所有已拥有「知识库」列表的角色
INSERT IGNORE INTO `sys_roles_menus` (`role_id`, `menu_id`)
SELECT `role_id`, 'ai_rag_detail' FROM `sys_roles_menus` WHERE `menu_id` = 'ai_rag';

-- 切片页隐藏菜单绑定给所有已拥有「知识库」列表的角色
INSERT IGNORE INTO `sys_roles_menus` (`role_id`, `menu_id`)
SELECT `role_id`, 'ai_rag_chunks' FROM `sys_roles_menus` WHERE `menu_id` = 'ai_rag';
