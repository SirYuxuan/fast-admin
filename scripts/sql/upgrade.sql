-- ============================================================================
-- Fast-Admin 升级脚本（增量）
--
-- 适用：已有 fast-admin 早期版本（仅含 sys_user/sys_role/sys_menu/sys_dept/
--      sys_users_roles/sys_roles_menus 6 张基础表）想要升级到含完整功能的版本。
--
-- 包含：
--   1. 文件管理 (sys_file_config / sys_file)
--   2. 系统参数 (sys_config)
--   3. 数据字典 (sys_dict_type / sys_dict_data)
--   4. 操作日志 (sys_operation_log)
--   5. 登录日志 (sys_login_log)
--   6. 定时任务业务表 (sys_job / sys_job_log)
--   7. Quartz 框架表 (QRTZ_*) - 11 张
--   8. sys_user 表新增 avatar 字段
--   9. 菜单 + 角色菜单关联（含管理员授权）
--
-- 兼容性：
--   - 所有表使用 CREATE TABLE IF NOT EXISTS（不会覆盖已存在的）
--   - 所有数据用 INSERT IGNORE（不会重复插入）
--   - 可以重复执行此脚本
--
-- 注意：
--   - 默认管理员角色 ID 为 35PlNb9zFOIQVWx8YoSlGGxjhHF
--     若不一致，请在执行后手动给目标角色授权这些菜单
--
-- 使用：
--   mysql -u<user> -p<pass> <db_name> < upgrade.sql
-- ============================================================================


-- ============================================
-- sys_user 表：新增 avatar 字段（如不存在）
-- ============================================
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'sys_user'
                     AND COLUMN_NAME = 'avatar');
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN avatar VARCHAR(512) DEFAULT NULL COMMENT ''头像URL'' AFTER nickname',
    'SELECT ''avatar column already exists'' AS msg');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- ============================================
-- sys_file_config
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_file_config` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `name` varchar(64) NOT NULL COMMENT '配置名',
  `type` varchar(16) NOT NULL COMMENT 'LOCAL/OSS/S3/FTP/SFTP',
  `config` json NOT NULL COMMENT '类型相关参数',
  `url_prefix` varchar(255) NOT NULL COMMENT '访问地址前缀（如 https://files.example.com）',
  `is_active` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否激活，全表至多 1 行为 1',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_by` varchar(64) DEFAULT NULL,
  `created_id` varchar(32) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_by` varchar(64) DEFAULT NULL,
  `updated_id` varchar(32) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_config_name` (`name`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件存储配置';

-- ============================================
-- sys_file
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_file` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `storage_key` varchar(512) NOT NULL COMMENT '存储相对路径/objectKey',
  `url` varchar(1024) NOT NULL COMMENT '上传时算好的完整访问地址',
  `size` bigint(20) NOT NULL COMMENT '文件字节数',
  `content_type` varchar(128) DEFAULT NULL,
  `ext` varchar(16) DEFAULT NULL,
  `hash` varchar(64) DEFAULT NULL COMMENT 'sha256',
  `storage_type` varchar(16) NOT NULL COMMENT '上传时使用的存储类型',
  `config_id` varchar(32) NOT NULL COMMENT '上传时使用的配置 id',
  `biz_type` varchar(64) DEFAULT NULL COMMENT '业务类型',
  `biz_id` varchar(64) DEFAULT NULL COMMENT '业务 id',
  `created_by` varchar(64) DEFAULT NULL,
  `created_id` varchar(32) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_by` varchar(64) DEFAULT NULL,
  `updated_id` varchar(32) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_file_biz` (`biz_type`,`biz_id`),
  KEY `idx_file_hash` (`hash`),
  KEY `idx_file_config` (`config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件记录';

-- ============================================
-- sys_config
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_config` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `config_name` varchar(128) NOT NULL COMMENT '参数名称',
  `config_key` varchar(128) NOT NULL COMMENT '参数键名',
  `config_value` varchar(1024) DEFAULT NULL COMMENT '参数键值',
  `config_type` tinyint(1) DEFAULT '0' COMMENT '是否系统内置：1是 0否',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_by` varchar(64) DEFAULT NULL,
  `created_id` varchar(32) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_by` varchar(64) DEFAULT NULL,
  `updated_id` varchar(32) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数';

-- ============================================
-- sys_dict_type
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_dict_type` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `dict_name` varchar(128) NOT NULL COMMENT '字典名称',
  `dict_type` varchar(128) NOT NULL COMMENT '字典类型（编码）',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：1启用 0禁用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_by` varchar(64) DEFAULT NULL,
  `created_id` varchar(32) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_by` varchar(64) DEFAULT NULL,
  `updated_id` varchar(32) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_type` (`dict_type`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型';

-- ============================================
-- sys_dict_data
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_dict_data` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `dict_type` varchar(128) NOT NULL COMMENT '字典类型',
  `dict_label` varchar(128) NOT NULL COMMENT '字典标签',
  `dict_value` varchar(128) NOT NULL COMMENT '字典键值',
  `dict_sort` int(11) DEFAULT '0' COMMENT '排序',
  `css_class` varchar(64) DEFAULT NULL COMMENT '样式属性（颜色等）',
  `list_class` varchar(64) DEFAULT NULL COMMENT '表格回显样式（如 success/danger）',
  `is_default` tinyint(1) DEFAULT '0' COMMENT '是否默认：1是 0否',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：1启用 0禁用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_by` varchar(64) DEFAULT NULL,
  `created_id` varchar(32) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_by` varchar(64) DEFAULT NULL,
  `updated_id` varchar(32) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据';

-- ============================================
-- sys_login_log
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_login_log` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `user_id` varchar(32) DEFAULT NULL COMMENT '用户ID',
  `username` varchar(64) DEFAULT NULL COMMENT '用户名',
  `ip` varchar(64) DEFAULT NULL COMMENT 'IP',
  `location` varchar(128) DEFAULT NULL COMMENT '地点',
  `browser` varchar(64) DEFAULT NULL COMMENT '浏览器',
  `os` varchar(64) DEFAULT NULL COMMENT '操作系统',
  `device` varchar(64) DEFAULT NULL COMMENT '设备类型',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：1成功 0失败',
  `msg` varchar(255) DEFAULT NULL COMMENT '描述/失败原因',
  `type` varchar(16) DEFAULT 'LOGIN' COMMENT 'LOGIN/LOGOUT',
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志';

-- ============================================
-- sys_operation_log
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `title` varchar(128) DEFAULT NULL COMMENT '操作模块/标题',
  `business_type` varchar(32) DEFAULT NULL COMMENT '业务类型：CREATE/UPDATE/DELETE/QUERY/IMPORT/EXPORT/OTHER',
  `method` varchar(255) DEFAULT NULL COMMENT '方法名 ClassName.methodName',
  `request_method` varchar(16) DEFAULT NULL COMMENT 'HTTP 请求方式',
  `operator_type` varchar(16) DEFAULT NULL COMMENT '操作类别 ADMIN/USER',
  `user_id` varchar(32) DEFAULT NULL COMMENT '用户ID',
  `username` varchar(64) DEFAULT NULL COMMENT '用户名',
  `url` varchar(500) DEFAULT NULL COMMENT '请求URL',
  `ip` varchar(64) DEFAULT NULL COMMENT '操作 IP',
  `location` varchar(128) DEFAULT NULL COMMENT '操作地点',
  `request_params` text COMMENT '请求参数 JSON',
  `response_result` text COMMENT '响应结果 JSON',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：1成功 0失败',
  `error_msg` text COMMENT '错误消息',
  `cost_time` bigint(20) DEFAULT NULL COMMENT '耗时(ms)',
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_created` (`created_at`),
  KEY `idx_business` (`business_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

-- ============================================
-- sys_job
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_job` (
  `id` varchar(32) NOT NULL,
  `job_name` varchar(64) NOT NULL COMMENT '任务名称',
  `job_group` varchar(64) DEFAULT 'DEFAULT' COMMENT '任务分组',
  `bean_name` varchar(128) NOT NULL COMMENT '调用的 Spring Bean 名',
  `method_name` varchar(64) DEFAULT 'execute' COMMENT '调用方法名（无参或单 String）',
  `method_params` varchar(512) DEFAULT NULL COMMENT '方法参数（JSON 数组或字符串）',
  `cron_expression` varchar(64) NOT NULL COMMENT 'Cron 表达式',
  `misfire_policy` tinyint(1) DEFAULT '1' COMMENT '错过策略: 1立即执行 2忽略 3触发一次',
  `concurrent` tinyint(1) DEFAULT '0' COMMENT '是否允许并发: 1是 0否',
  `status` tinyint(1) DEFAULT '0' COMMENT '状态: 1正常 0暂停',
  `remark` varchar(255) DEFAULT NULL,
  `created_by` varchar(64) DEFAULT NULL,
  `created_id` varchar(32) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_by` varchar(64) DEFAULT NULL,
  `updated_id` varchar(32) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_job_name` (`job_name`,`job_group`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务';

-- ============================================
-- sys_job_log
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_job_log` (
  `id` varchar(32) NOT NULL,
  `job_id` varchar(32) NOT NULL COMMENT '任务ID',
  `job_name` varchar(64) NOT NULL,
  `job_group` varchar(64) DEFAULT NULL,
  `bean_name` varchar(128) DEFAULT NULL,
  `method_name` varchar(64) DEFAULT NULL,
  `method_params` varchar(512) DEFAULT NULL,
  `status` tinyint(1) DEFAULT '1' COMMENT '1成功 0失败',
  `cost_time` bigint(20) DEFAULT NULL COMMENT '耗时(ms)',
  `error_msg` text,
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_job` (`job_id`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务执行日志';

-- ============================================
-- QRTZ_JOB_DETAILS
-- ============================================
CREATE TABLE IF NOT EXISTS `QRTZ_JOB_DETAILS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `JOB_NAME` varchar(190) NOT NULL,
  `JOB_GROUP` varchar(190) NOT NULL,
  `DESCRIPTION` varchar(250) DEFAULT NULL,
  `JOB_CLASS_NAME` varchar(250) NOT NULL,
  `IS_DURABLE` varchar(1) NOT NULL,
  `IS_NONCONCURRENT` varchar(1) NOT NULL,
  `IS_UPDATE_DATA` varchar(1) NOT NULL,
  `REQUESTS_RECOVERY` varchar(1) NOT NULL,
  `JOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- QRTZ_TRIGGERS
-- ============================================
CREATE TABLE IF NOT EXISTS `QRTZ_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(190) NOT NULL,
  `TRIGGER_GROUP` varchar(190) NOT NULL,
  `JOB_NAME` varchar(190) NOT NULL,
  `JOB_GROUP` varchar(190) NOT NULL,
  `DESCRIPTION` varchar(250) DEFAULT NULL,
  `NEXT_FIRE_TIME` bigint(20) DEFAULT NULL,
  `PREV_FIRE_TIME` bigint(20) DEFAULT NULL,
  `PRIORITY` int(11) DEFAULT NULL,
  `TRIGGER_STATE` varchar(16) NOT NULL,
  `TRIGGER_TYPE` varchar(8) NOT NULL,
  `START_TIME` bigint(20) NOT NULL,
  `END_TIME` bigint(20) DEFAULT NULL,
  `CALENDAR_NAME` varchar(190) DEFAULT NULL,
  `MISFIRE_INSTR` smallint(6) DEFAULT NULL,
  `JOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  KEY `SCHED_NAME` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
  CONSTRAINT `QRTZ_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) REFERENCES `QRTZ_JOB_DETAILS` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- QRTZ_SIMPLE_TRIGGERS
-- ============================================
CREATE TABLE IF NOT EXISTS `QRTZ_SIMPLE_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(190) NOT NULL,
  `TRIGGER_GROUP` varchar(190) NOT NULL,
  `REPEAT_COUNT` bigint(20) NOT NULL,
  `REPEAT_INTERVAL` bigint(20) NOT NULL,
  `TIMES_TRIGGERED` bigint(20) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_SIMPLE_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- QRTZ_CRON_TRIGGERS
-- ============================================
CREATE TABLE IF NOT EXISTS `QRTZ_CRON_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(190) NOT NULL,
  `TRIGGER_GROUP` varchar(190) NOT NULL,
  `CRON_EXPRESSION` varchar(120) NOT NULL,
  `TIME_ZONE_ID` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_CRON_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- QRTZ_SIMPROP_TRIGGERS
-- ============================================
CREATE TABLE IF NOT EXISTS `QRTZ_SIMPROP_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(190) NOT NULL,
  `TRIGGER_GROUP` varchar(190) NOT NULL,
  `STR_PROP_1` varchar(512) DEFAULT NULL,
  `STR_PROP_2` varchar(512) DEFAULT NULL,
  `STR_PROP_3` varchar(512) DEFAULT NULL,
  `INT_PROP_1` int(11) DEFAULT NULL,
  `INT_PROP_2` int(11) DEFAULT NULL,
  `LONG_PROP_1` bigint(20) DEFAULT NULL,
  `LONG_PROP_2` bigint(20) DEFAULT NULL,
  `DEC_PROP_1` decimal(13,4) DEFAULT NULL,
  `DEC_PROP_2` decimal(13,4) DEFAULT NULL,
  `BOOL_PROP_1` varchar(1) DEFAULT NULL,
  `BOOL_PROP_2` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_SIMPROP_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- QRTZ_BLOB_TRIGGERS
-- ============================================
CREATE TABLE IF NOT EXISTS `QRTZ_BLOB_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(190) NOT NULL,
  `TRIGGER_GROUP` varchar(190) NOT NULL,
  `BLOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  KEY `SCHED_NAME` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_BLOB_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- QRTZ_CALENDARS
-- ============================================
CREATE TABLE IF NOT EXISTS `QRTZ_CALENDARS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `CALENDAR_NAME` varchar(190) NOT NULL,
  `CALENDAR` blob NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`CALENDAR_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- QRTZ_PAUSED_TRIGGER_GRPS
-- ============================================
CREATE TABLE IF NOT EXISTS `QRTZ_PAUSED_TRIGGER_GRPS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_GROUP` varchar(190) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- QRTZ_FIRED_TRIGGERS
-- ============================================
CREATE TABLE IF NOT EXISTS `QRTZ_FIRED_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `ENTRY_ID` varchar(95) NOT NULL,
  `TRIGGER_NAME` varchar(190) NOT NULL,
  `TRIGGER_GROUP` varchar(190) NOT NULL,
  `INSTANCE_NAME` varchar(190) NOT NULL,
  `FIRED_TIME` bigint(20) NOT NULL,
  `SCHED_TIME` bigint(20) NOT NULL,
  `PRIORITY` int(11) NOT NULL,
  `STATE` varchar(16) NOT NULL,
  `JOB_NAME` varchar(190) DEFAULT NULL,
  `JOB_GROUP` varchar(190) DEFAULT NULL,
  `IS_NONCONCURRENT` varchar(1) DEFAULT NULL,
  `REQUESTS_RECOVERY` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`ENTRY_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- QRTZ_SCHEDULER_STATE
-- ============================================
CREATE TABLE IF NOT EXISTS `QRTZ_SCHEDULER_STATE` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `INSTANCE_NAME` varchar(190) NOT NULL,
  `LAST_CHECKIN_TIME` bigint(20) NOT NULL,
  `CHECKIN_INTERVAL` bigint(20) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`INSTANCE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- QRTZ_LOCKS
-- ============================================
CREATE TABLE IF NOT EXISTS `QRTZ_LOCKS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `LOCK_NAME` varchar(40) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`LOCK_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 菜单数据（新增的功能菜单）
-- 使用 INSERT IGNORE，安全多次执行
-- ============================================

INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('3122e315d4ecf6d5c2f0ed35add','35fcfg9ef55798c3ca9611a9c14','SysFileConfigAdd','system:file:config:add',3,1,NULL,NULL,NULL,NULL,NULL,'新增',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('e3651e7cf7382d4202b5c035edt','35fcfg9ef55798c3ca9611a9c14','SysFileConfigEdit','system:file:config:edit',3,1,NULL,NULL,NULL,NULL,NULL,'编辑',1,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('1671612b50b414ae76ed2935del','35fcfg9ef55798c3ca9611a9c14','SysFileConfigDelete','system:file:config:delete',3,1,NULL,NULL,NULL,NULL,NULL,'删除',2,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('ff6aa327abf9329be65edb35act','35fcfg9ef55798c3ca9611a9c14','SysFileConfigActivate','system:file:config:activate',3,1,NULL,NULL,NULL,NULL,NULL,'激活',3,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('c7086c22b7bd6706c8c25435upl','35flstd84b75d35ae70afd0a236','SysFileUpload','system:file:upload',3,1,NULL,NULL,NULL,NULL,NULL,'上传',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('f1bb21997562811b8dbcd135dwn','35flstd84b75d35ae70afd0a236','SysFileDownload','system:file:download',3,1,NULL,NULL,NULL,NULL,NULL,'下载',1,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('fccb25f29043e633715c8935fdl','35flstd84b75d35ae70afd0a236','SysFileDelete','system:file:delete',3,1,NULL,NULL,NULL,NULL,NULL,'删除',2,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('35fcfg9ef55798c3ca9611a9c14','35fmgr7c8366ccdcc31f6ab5de7','SysFileConfig','system:file:config:list',1,1,'/system/file-config',NULL,'/system/file-config/list','carbon:cloud-upload',NULL,'文件配置',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('35flstd84b75d35ae70afd0a236','35fmgr7c8366ccdcc31f6ab5de7','SysFile','system:file:list',1,1,'/system/file',NULL,'/system/file/list','carbon:document',NULL,'文件列表',1,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('35fmgr7c8366ccdcc31f6ab5de7','35PlZtb7CCmpEZ5XWwTZqEX7fMZ','FileManage',NULL,2,1,'/system/file-manage',NULL,NULL,'carbon:folder',NULL,'文件管理',12,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('sys_monitor','35PlZtb7CCmpEZ5XWwTZqEX7fMZ','SysMonitor',NULL,2,1,'/system/monitor',NULL,NULL,'lucide:activity',NULL,'系统监控',50,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('sys_config_menu','35PlZtb7CCmpEZ5XWwTZqEX7fMZ','SysConfig','system:config:list',1,1,'/system/config',NULL,'system/config/list','lucide:sliders',NULL,'系统参数',60,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('sys_dict_menu','35PlZtb7CCmpEZ5XWwTZqEX7fMZ','SysDict','system:dict:list',1,1,'/system/dict',NULL,'system/dict/list','lucide:book-marked',NULL,'数据字典',70,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('1b3eb8846f65413384429bc181d','sys_config_menu','SysConfigDel','system:config:delete',3,1,NULL,NULL,NULL,NULL,NULL,'SysConfigDel',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('871bdf0bd7054b2a8e83cc1ce3c','sys_config_menu','SysConfigEdit','system:config:edit',3,1,NULL,NULL,NULL,NULL,NULL,'SysConfigEdit',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('d64c72cc8cce4c57a34e58d2a54','sys_config_menu','SysConfigAdd','system:config:add',3,1,NULL,NULL,NULL,NULL,NULL,'SysConfigAdd',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('4d8d3c3af4a945de80f241a181c','sys_dict_menu','SysDictDataEdit','system:dict:data:edit',3,1,NULL,NULL,NULL,NULL,NULL,'SysDictDataEdit',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('56e811b5628643e3b88aee98b33','sys_dict_menu','SysDictTypeDel','system:dict:type:delete',3,1,NULL,NULL,NULL,NULL,NULL,'SysDictTypeDel',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('5b8f5eef71bb423d81d3b01d3f0','sys_dict_menu','SysDictTypeEdit','system:dict:type:edit',3,1,NULL,NULL,NULL,NULL,NULL,'SysDictTypeEdit',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('871e5be9fe6e431eb105e467ceb','sys_dict_menu','SysDictDataAdd','system:dict:data:add',3,1,NULL,NULL,NULL,NULL,NULL,'SysDictDataAdd',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('f0d731c8f0c54ce7b183d13e309','sys_dict_menu','SysDictTypeAdd','system:dict:type:add',3,1,NULL,NULL,NULL,NULL,NULL,'SysDictTypeAdd',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('fd3c5aa8b46940e6ad4c11ad873','sys_dict_menu','SysDictDataDel','system:dict:data:delete',3,1,NULL,NULL,NULL,NULL,NULL,'SysDictDataDel',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('03a5e92d0929456796f1e06651f','sys_job_menu','SysJobEdit','system:job:edit',3,1,NULL,NULL,NULL,NULL,NULL,'SysJobEdit',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('20dec697ac954fb79364a7d3f2d','sys_job_menu','SysJobDel','system:job:delete',3,1,NULL,NULL,NULL,NULL,NULL,'SysJobDel',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('a04831863f234156ae1cc6cd706','sys_job_menu','SysJobAdd','system:job:add',3,1,NULL,NULL,NULL,NULL,NULL,'SysJobAdd',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('b271e8f9d6c64d8a980bceb44ef','sys_login_log','SysLoginLogDelete','system:log:login:delete',3,1,NULL,NULL,NULL,NULL,NULL,'SysLoginLogDelete',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('sys_op_log','sys_monitor','SysOperationLog','system:log:operation:list',1,1,'/system/log/operation',NULL,'system/log/operation/list','lucide:scroll-text',NULL,'操作日志',10,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('sys_login_log','sys_monitor','SysLoginLog','system:log:login:list',1,1,'/system/log/login',NULL,'system/log/login/list','lucide:log-in',NULL,'登录日志',20,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('sys_job_menu','sys_monitor','SysJob','system:job:list',1,1,'/system/job',NULL,'system/job/list','lucide:timer',NULL,'定时任务',30,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('sys_job_log_menu','sys_monitor','SysJobLog','system:job:log:list',1,1,'/system/job/log',NULL,'system/job/log/list','lucide:scroll-text',NULL,'任务日志',31,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('sys_server_monitor','sys_monitor','SysServerMonitor','system:monitor:server',1,1,'/system/monitor/server',NULL,'system/monitor/server/index','lucide:gauge',NULL,'服务监控',40,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('sys_online_menu','sys_monitor','SysOnline','system:online:list',1,1,'/system/monitor/online',NULL,'system/monitor/online/index','lucide:users-round',NULL,'在线用户',45,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('c29db81ccafd49c3ac3e990ca1c','sys_online_menu','SysOnlineKickout','system:online:kickout',3,1,NULL,NULL,NULL,NULL,NULL,'强制下线',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());
INSERT IGNORE INTO sys_menu (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`active_path`,`component`,`icon`,`meta_active_icon`,`meta_title`,`meta_order`,`meta_affix_tab`,`meta_keep_alive`,`meta_hide_in_menu`,`meta_hide_children_in_menu`,`meta_hide_in_breadcrumb`,`meta_hide_in_tab`,`meta_badge`,`meta_badge_type`,`meta_badge_variants`,`meta_iframe_src`,`meta_link`,`remark`, is_deleted, created_at) VALUES ('d206c1ee8dc24ba1ae2873ef0af','sys_op_log','SysOperationLogDelete','system:log:operation:delete',3,1,NULL,NULL,NULL,NULL,NULL,'SysOperationLogDelete',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL, 0, NOW());

-- 角色菜单关联
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', '03a5e92d0929456796f1e06651f');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', '1671612b50b414ae76ed2935del');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', '1b3eb8846f65413384429bc181d');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', '20dec697ac954fb79364a7d3f2d');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', '3122e315d4ecf6d5c2f0ed35add');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', '35fcfg9ef55798c3ca9611a9c14');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', '35flstd84b75d35ae70afd0a236');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', '35fmgr7c8366ccdcc31f6ab5de7');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', '4d8d3c3af4a945de80f241a181c');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', '56e811b5628643e3b88aee98b33');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', '5b8f5eef71bb423d81d3b01d3f0');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', '871bdf0bd7054b2a8e83cc1ce3c');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', '871e5be9fe6e431eb105e467ceb');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', 'a04831863f234156ae1cc6cd706');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', 'b271e8f9d6c64d8a980bceb44ef');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', 'c29db81ccafd49c3ac3e990ca1c');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', 'c7086c22b7bd6706c8c25435upl');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', 'd206c1ee8dc24ba1ae2873ef0af');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', 'd64c72cc8cce4c57a34e58d2a54');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', 'e3651e7cf7382d4202b5c035edt');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', 'f0d731c8f0c54ce7b183d13e309');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', 'f1bb21997562811b8dbcd135dwn');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', 'fccb25f29043e633715c8935fdl');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', 'fd3c5aa8b46940e6ad4c11ad873');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', 'ff6aa327abf9329be65edb35act');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', 'sys_config_menu');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', 'sys_dict_menu');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', 'sys_job_log_menu');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', 'sys_job_menu');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', 'sys_login_log');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', 'sys_monitor');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', 'sys_online_menu');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', 'sys_op_log');
INSERT IGNORE INTO sys_roles_menus (role_id, menu_id) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF', 'sys_server_monitor');
