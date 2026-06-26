-- fast-admin scaffold database initialization
-- Full structure, curated seed data. Runtime logs/history/files/secrets are intentionally excluded.
SET NAMES utf8mb4;

-- MySQL dump 10.13  Distrib 8.4.9, for macos26.4 (arm64)
--
-- Host: localhost    Database: fast_admin
-- ------------------------------------------------------
-- Server version	9.5.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `fast_admin`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `fast_admin` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `fast_admin`;

--
-- Table structure for table `ai_chat_message`
--

DROP TABLE IF EXISTS `ai_chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chat_message` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `session_id` varchar(64) NOT NULL COMMENT '所属会话业务ID',
  `role` varchar(16) NOT NULL COMMENT '角色：user/assistant',
  `content` mediumtext COMMENT '消息内容',
  `process_json` longtext COMMENT '助手消息的思考与工具过程 JSON',
  `model_name` varchar(128) DEFAULT NULL COMMENT '助手消息使用的模型配置名称',
  `model_provider` varchar(32) DEFAULT NULL COMMENT '助手消息使用的模型提供方',
  `model_code` varchar(128) DEFAULT NULL COMMENT '助手消息使用的模型编码',
  `prompt_tokens` int DEFAULT NULL COMMENT '输入(提示)token 数',
  `completion_tokens` int DEFAULT NULL COMMENT '输出(补全)token 数',
  `total_tokens` int DEFAULT NULL COMMENT '总 token 数',
  `created_by` varchar(64) DEFAULT NULL,
  `created_id` varchar(32) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_by` varchar(64) DEFAULT NULL,
  `updated_id` varchar(32) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_ai_chat_message_sid` (`session_id`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 会话消息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_chat_session`
--

DROP TABLE IF EXISTS `ai_chat_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chat_session` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `session_id` varchar(64) NOT NULL COMMENT '会话业务ID（前端生成）',
  `user_id` varchar(64) DEFAULT NULL COMMENT '所属用户ID',
  `title` varchar(255) DEFAULT NULL COMMENT '会话标题，取首条用户消息',
  `created_by` varchar(64) DEFAULT NULL,
  `created_id` varchar(32) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_by` varchar(64) DEFAULT NULL,
  `updated_id` varchar(32) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_chat_session_sid` (`session_id`,`is_deleted`),
  KEY `idx_ai_chat_session_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 会话';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_mcp_server`
--

DROP TABLE IF EXISTS `ai_mcp_server`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_mcp_server` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `name` varchar(64) NOT NULL COMMENT '服务名称',
  `transport` varchar(32) NOT NULL COMMENT '传输类型：stdio/sse/streamable-http',
  `command` varchar(255) DEFAULT NULL COMMENT 'stdio 启动命令',
  `url` varchar(512) DEFAULT NULL COMMENT '远程 MCP 服务地址',
  `args_json` text COMMENT 'stdio 命令参数 JSON 数组',
  `headers_json` text COMMENT '远程连接请求头 JSON 对象',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `keep_alive` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'SSE 连接保活开关',
  `keep_alive_interval` int DEFAULT '30' COMMENT '保活间隔(秒)',
  `keep_alive_job_id` varchar(36) DEFAULT NULL COMMENT '关联 sys_job 主键',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI MCP 服务配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_model_config`
--

DROP TABLE IF EXISTS `ai_model_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_model_config` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 模型配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_tool_call_log`
--

DROP TABLE IF EXISTS `ai_tool_call_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_tool_call_log` (
  `id` varchar(36) NOT NULL COMMENT '主键',
  `session_id` varchar(100) DEFAULT NULL COMMENT '对话会话业务 ID',
  `operator_id` varchar(100) DEFAULT NULL COMMENT '操作人用户 ID',
  `tool_name` varchar(64) NOT NULL COMMENT '工具编码/名称',
  `source` varchar(20) NOT NULL DEFAULT 'builtin' COMMENT '工具来源：builtin / mcp',
  `arguments_json` text COMMENT '调用入参 JSON',
  `result_json` text COMMENT '执行结果（截断至 4000 字符）',
  `success` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否成功',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '失败错误信息',
  `cost_ms` bigint DEFAULT NULL COMMENT '执行耗时(ms)',
  `created_by` varchar(100) DEFAULT NULL,
  `created_id` varchar(100) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_by` varchar(100) DEFAULT NULL,
  `updated_id` varchar(100) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_tool_name` (`tool_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 工具调用审计日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_tool_config`
--

DROP TABLE IF EXISTS `ai_tool_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_tool_config` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 工具配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QRTZ_BLOB_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_BLOB_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_BLOB_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(190) NOT NULL,
  `TRIGGER_GROUP` varchar(190) NOT NULL,
  `BLOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  KEY `SCHED_NAME` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_BLOB_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QRTZ_CALENDARS`
--

DROP TABLE IF EXISTS `QRTZ_CALENDARS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_CALENDARS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `CALENDAR_NAME` varchar(190) NOT NULL,
  `CALENDAR` blob NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`CALENDAR_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QRTZ_CRON_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_CRON_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_CRON_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(190) NOT NULL,
  `TRIGGER_GROUP` varchar(190) NOT NULL,
  `CRON_EXPRESSION` varchar(120) NOT NULL,
  `TIME_ZONE_ID` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_CRON_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QRTZ_FIRED_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_FIRED_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_FIRED_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `ENTRY_ID` varchar(95) NOT NULL,
  `TRIGGER_NAME` varchar(190) NOT NULL,
  `TRIGGER_GROUP` varchar(190) NOT NULL,
  `INSTANCE_NAME` varchar(190) NOT NULL,
  `FIRED_TIME` bigint NOT NULL,
  `SCHED_TIME` bigint NOT NULL,
  `PRIORITY` int NOT NULL,
  `STATE` varchar(16) NOT NULL,
  `JOB_NAME` varchar(190) DEFAULT NULL,
  `JOB_GROUP` varchar(190) DEFAULT NULL,
  `IS_NONCONCURRENT` varchar(1) DEFAULT NULL,
  `REQUESTS_RECOVERY` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`ENTRY_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QRTZ_JOB_DETAILS`
--

DROP TABLE IF EXISTS `QRTZ_JOB_DETAILS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_JOB_DETAILS` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QRTZ_LOCKS`
--

DROP TABLE IF EXISTS `QRTZ_LOCKS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_LOCKS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `LOCK_NAME` varchar(40) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`LOCK_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QRTZ_PAUSED_TRIGGER_GRPS`
--

DROP TABLE IF EXISTS `QRTZ_PAUSED_TRIGGER_GRPS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_PAUSED_TRIGGER_GRPS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_GROUP` varchar(190) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QRTZ_SCHEDULER_STATE`
--

DROP TABLE IF EXISTS `QRTZ_SCHEDULER_STATE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_SCHEDULER_STATE` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `INSTANCE_NAME` varchar(190) NOT NULL,
  `LAST_CHECKIN_TIME` bigint NOT NULL,
  `CHECKIN_INTERVAL` bigint NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`INSTANCE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QRTZ_SIMPLE_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_SIMPLE_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_SIMPLE_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(190) NOT NULL,
  `TRIGGER_GROUP` varchar(190) NOT NULL,
  `REPEAT_COUNT` bigint NOT NULL,
  `REPEAT_INTERVAL` bigint NOT NULL,
  `TIMES_TRIGGERED` bigint NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_SIMPLE_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QRTZ_SIMPROP_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_SIMPROP_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_SIMPROP_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(190) NOT NULL,
  `TRIGGER_GROUP` varchar(190) NOT NULL,
  `STR_PROP_1` varchar(512) DEFAULT NULL,
  `STR_PROP_2` varchar(512) DEFAULT NULL,
  `STR_PROP_3` varchar(512) DEFAULT NULL,
  `INT_PROP_1` int DEFAULT NULL,
  `INT_PROP_2` int DEFAULT NULL,
  `LONG_PROP_1` bigint DEFAULT NULL,
  `LONG_PROP_2` bigint DEFAULT NULL,
  `DEC_PROP_1` decimal(13,4) DEFAULT NULL,
  `DEC_PROP_2` decimal(13,4) DEFAULT NULL,
  `BOOL_PROP_1` varchar(1) DEFAULT NULL,
  `BOOL_PROP_2` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_SIMPROP_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QRTZ_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_TRIGGERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `QRTZ_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(190) NOT NULL,
  `TRIGGER_GROUP` varchar(190) NOT NULL,
  `JOB_NAME` varchar(190) NOT NULL,
  `JOB_GROUP` varchar(190) NOT NULL,
  `DESCRIPTION` varchar(250) DEFAULT NULL,
  `NEXT_FIRE_TIME` bigint DEFAULT NULL,
  `PREV_FIRE_TIME` bigint DEFAULT NULL,
  `PRIORITY` int DEFAULT NULL,
  `TRIGGER_STATE` varchar(16) NOT NULL,
  `TRIGGER_TYPE` varchar(8) NOT NULL,
  `START_TIME` bigint NOT NULL,
  `END_TIME` bigint DEFAULT NULL,
  `CALENDAR_NAME` varchar(190) DEFAULT NULL,
  `MISFIRE_INSTR` smallint DEFAULT NULL,
  `JOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  KEY `SCHED_NAME` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
  CONSTRAINT `QRTZ_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) REFERENCES `QRTZ_JOB_DETAILS` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_config`
--

DROP TABLE IF EXISTS `sys_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_config` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统参数';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_dept`
--

DROP TABLE IF EXISTS `sys_dept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dept` (
  `id` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID（KSUID）',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门名称',
  `pid` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '上级部门ID',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `remark` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `is_enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用',
  `created_by` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人ID',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_by` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人ID',
  `updated_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：false未删除，true已删除',
  `created_id` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人ID',
  `updated_id` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_pid` (`pid`),
  KEY `idx_status` (`status`),
  KEY `idx_is_enabled` (`is_enabled`),
  KEY `idx_is_deleted` (`is_deleted`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统-部门表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_dict_data`
--

DROP TABLE IF EXISTS `sys_dict_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_data` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `dict_type` varchar(128) NOT NULL COMMENT '字典类型',
  `dict_label` varchar(128) NOT NULL COMMENT '字典标签',
  `dict_value` varchar(128) NOT NULL COMMENT '字典键值',
  `dict_sort` int DEFAULT '0' COMMENT '排序',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_dict_type`
--

DROP TABLE IF EXISTS `sys_dict_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_type` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_file`
--

DROP TABLE IF EXISTS `sys_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_file` (
  `id` varchar(32) NOT NULL COMMENT '主键',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `storage_key` varchar(512) NOT NULL COMMENT '存储相对路径/objectKey',
  `url` varchar(1024) NOT NULL COMMENT '上传时算好的完整访问地址',
  `size` bigint NOT NULL COMMENT '文件字节数',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_file_config`
--

DROP TABLE IF EXISTS `sys_file_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_file_config` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件存储配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_job`
--

DROP TABLE IF EXISTS `sys_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='定时任务';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_job_log`
--

DROP TABLE IF EXISTS `sys_job_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job_log` (
  `id` varchar(32) NOT NULL,
  `job_id` varchar(32) NOT NULL COMMENT '任务ID',
  `job_name` varchar(64) NOT NULL,
  `job_group` varchar(64) DEFAULT NULL,
  `bean_name` varchar(128) DEFAULT NULL,
  `method_name` varchar(64) DEFAULT NULL,
  `method_params` varchar(512) DEFAULT NULL,
  `status` tinyint(1) DEFAULT '1' COMMENT '1成功 0失败',
  `cost_time` bigint DEFAULT NULL COMMENT '耗时(ms)',
  `error_msg` text,
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_job` (`job_id`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='定时任务执行日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_login_log`
--

DROP TABLE IF EXISTS `sys_login_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_login_log` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_menu`
--

DROP TABLE IF EXISTS `sys_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_menu` (
  `id` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID（KSUID）',
  `pid` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '父菜单ID，顶级菜单为NULL',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单/按钮名称',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '菜单/按钮编码，用于权限控制',
  `type` tinyint NOT NULL COMMENT '菜单类型：1=menu, 2=catalog, 3=button, 4=embedded, 5=link',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1启用，0禁用',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '前端路由路径',
  `active_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '激活路径',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '前端组件路径',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '菜单/按钮图标',
  `meta_active_icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '激活状态图标',
  `meta_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '前端显示标题，可用于国际化',
  `meta_order` int DEFAULT '0' COMMENT '菜单排序，数值越小越靠前',
  `meta_affix_tab` tinyint(1) DEFAULT '0' COMMENT '是否固定标签页',
  `meta_keep_alive` tinyint(1) DEFAULT '0' COMMENT '是否保持组件活跃状态',
  `meta_hide_in_menu` tinyint(1) DEFAULT '0' COMMENT '是否在菜单中隐藏',
  `meta_hide_children_in_menu` tinyint(1) DEFAULT '0' COMMENT '是否隐藏子菜单',
  `meta_hide_in_breadcrumb` tinyint(1) DEFAULT '0' COMMENT '是否在面包屑中隐藏',
  `meta_hide_in_tab` tinyint(1) DEFAULT '0' COMMENT '是否在标签页中隐藏',
  `meta_badge` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '徽标显示内容',
  `meta_badge_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '徽标类型，如 normal/dot',
  `meta_badge_variants` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '徽标颜色或样式',
  `meta_iframe_src` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '内嵌 IFrame 链接',
  `meta_link` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '外部链接地址',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人名字',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人名字',
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `created_id` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人ID',
  `updated_id` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：false=未删除，true=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_sys_menu_pid` (`pid`),
  KEY `idx_sys_menu_type` (`type`),
  KEY `idx_sys_menu_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统菜单表（支持多级菜单和按钮权限）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_operation_log`
--

DROP TABLE IF EXISTS `sys_operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_operation_log` (
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
  `cost_time` bigint DEFAULT NULL COMMENT '耗时(ms)',
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_created` (`created_at`),
  KEY `idx_business` (`business_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_role`
--

DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `id` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID（KSUID）',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名字',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '角色备注',
  `is_enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用',
  `created_by` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人ID',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_by` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人ID',
  `updated_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：false未删除，true已删除',
  `created_id` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人ID',
  `updated_id` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`code`) COMMENT '角色编码唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_roles_menus`
--

DROP TABLE IF EXISTS `sys_roles_menus`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_roles_menus` (
  `role_id` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色ID（KSUID）',
  `menu_id` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单ID（KSUID）',
  PRIMARY KEY (`role_id`,`menu_id`),
  KEY `idx_roles_menus_role` (`role_id`),
  KEY `idx_roles_menus_menu` (`menu_id`),
  CONSTRAINT `sys_roles_menus_ibfk_1` FOREIGN KEY (`menu_id`) REFERENCES `sys_menu` (`id`) ON DELETE CASCADE,
  CONSTRAINT `sys_roles_menus_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID（KSUID）',
  `dept_id` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '部门ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（加密存储）',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0正常，1冻结，2锁定',
  `sex` tinyint NOT NULL DEFAULT '0' COMMENT '性别：1男，2女，0未知',
  `login_ip` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后登录IP（支持IPv6）',
  `login_city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后登录城市',
  `login_time` timestamp(6) NULL DEFAULT NULL COMMENT '最后登录时间',
  `created_by` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人ID',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_by` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人ID',
  `updated_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：false未删除，true已删除',
  `created_id` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人ID',
  `updated_id` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`) COMMENT '用户名唯一',
  KEY `idx_user_status` (`status`),
  KEY `idx_user_phone` (`phone`),
  KEY `idx_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_users_roles`
--

DROP TABLE IF EXISTS `sys_users_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_users_roles` (
  `user_id` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID（KSUID）',
  `role_id` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色ID（KSUID）',
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `idx_users_roles_user` (`user_id`),
  KEY `idx_users_roles_role` (`role_id`),
  CONSTRAINT `sys_users_roles_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE,
  CONSTRAINT `sys_users_roles_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'fast_admin'
--

--
-- Dumping routines for database 'fast_admin'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-26 14:54:17

--
-- Seed data for table `sys_config`
--

-- MySQL dump 10.13  Distrib 8.4.9, for macos26.4 (arm64)
--
-- Host: localhost    Database: fast_admin
-- ------------------------------------------------------
-- Server version	9.5.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `sys_config`
--
-- WHERE:  is_deleted = 0

LOCK TABLES `sys_config` WRITE;
/*!40000 ALTER TABLE `sys_config` DISABLE KEYS */;
INSERT INTO `sys_config` (`id`, `config_name`, `config_key`, `config_value`, `config_type`, `remark`, `created_by`, `created_id`, `created_at`, `updated_by`, `updated_id`, `updated_at`, `is_deleted`) VALUES ('ai_chat_history_window','AI对话历史窗口','ai.chat.history-window','20',1,'每轮对话注入提示词的历史消息条数上限（2~100），越大上下文越完整但更费 token','system',NULL,'2026-06-26 11:13:45','system',NULL,'2026-06-26 11:13:45',0),('ai_execute_sql_enabled','AI执行SQL工具开关','ai.execute-sql.enabled','true',1,'控制内置 execute_sql 工具是否注册给模型，默认关闭，开启后 AI 可执行任意 SQL','system',NULL,'2026-06-25 20:03:19','Sir丶雨轩','35PaELKCOjq8YdhmuOtQWGPrBJh','2026-06-25 20:09:16',0),('ai_execute_sql_max_rows','AI执行SQL最大返回行数','ai.execute-sql.max-rows','100',1,'内置 execute_sql 工具查询语句单次最多返回行数，代码层最大 500','system',NULL,'2026-06-25 20:03:19','system',NULL,'2026-06-25 20:03:19',0),('ai_execute_sql_perm','AI执行SQL工具权限码','ai.execute-sql.permission-code','ai:sql:execute',1,'调用内置 execute_sql 工具需要的权限码，留空则不校验权限','system',NULL,'2026-06-25 20:03:19','system',NULL,'2026-06-25 20:03:19',0),('ai_readonly_sql_enabled','AI只读SQL工具开关','ai.readonly-sql.enabled','true',1,'控制内置 execute_readonly_sql 工具是否注册给模型','system',NULL,'2026-06-25 12:31:16','system',NULL,'2026-06-25 12:31:16',0),('ai_readonly_sql_max_rows','AI只读SQL最大返回行数','ai.readonly-sql.max-rows','100',1,'内置 execute_readonly_sql 工具单次最多返回行数，代码层最大 100','system',NULL,'2026-06-25 12:31:16','system',NULL,'2026-06-25 12:31:16',0),('ai_readonly_sql_perm','AI只读SQL工具权限码','ai.readonly-sql.permission-code','ai:sql:readonly',1,'调用内置 execute_readonly_sql 工具需要的权限码0','system',NULL,'2026-06-25 12:31:16','Sir丶雨轩','35PaELKCOjq8YdhmuOtQWGPrBJh','2026-06-25 19:49:12',0),('ai_schema_tool_enabled','AI表结构工具开关','ai.schema-tool.enabled','true',1,'控制内置 describe_schema 工具是否注册给模型，默认开启','system',NULL,'2026-06-26 11:13:45','system',NULL,'2026-06-26 11:13:45',0),('ai_schema_tool_perm','AI表结构工具权限码','ai.schema-tool.permission-code','ai:sql:readonly',1,'调用内置 describe_schema 工具需要的权限码，留空则不校验','system',NULL,'2026-06-26 11:13:45','system',NULL,'2026-06-26 11:13:45',0);
/*!40000 ALTER TABLE `sys_config` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-26 14:54:17

--
-- Seed data for table `sys_dept`
--

-- MySQL dump 10.13  Distrib 8.4.9, for macos26.4 (arm64)
--
-- Host: localhost    Database: fast_admin
-- ------------------------------------------------------
-- Server version	9.5.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `sys_dept`
--
-- WHERE:  is_deleted = 0

LOCK TABLES `sys_dept` WRITE;
/*!40000 ALTER TABLE `sys_dept` DISABLE KEYS */;
INSERT INTO `sys_dept` (`id`, `name`, `pid`, `status`, `remark`, `is_enabled`, `created_by`, `created_at`, `updated_by`, `updated_at`, `is_deleted`, `created_id`, `updated_id`) VALUES ('35SYbLjJaBxhMOAl7LdELC3Zezi','雨轩科技',NULL,1,NULL,1,'Sir丶雨轩','2025-11-14 07:23:29.366000','Sir丶雨轩','2025-11-14 07:36:52.468000',0,'35PaELKCOjq8YdhmuOtQWGPrBJh','35PaELKCOjq8YdhmuOtQWGPrBJh'),('35SZAmUMGKqvpWTC2EQODUKm59T','研发部门','35SYbLjJaBxhMOAl7LdELC3Zezi',1,NULL,1,'Sir丶雨轩','2025-11-14 07:28:11.661000','Sir丶雨轩','2025-11-14 07:28:11.684000',0,'35PaELKCOjq8YdhmuOtQWGPrBJh','35PaELKCOjq8YdhmuOtQWGPrBJh'),('35SZBmqUlmbghCPu2cyeSgv8GCo','测试部门','35SYbLjJaBxhMOAl7LdELC3Zezi',1,NULL,1,'Sir丶雨轩','2025-11-14 07:28:19.009000','Sir丶雨轩','2025-11-14 07:28:19.012000',0,'35PaELKCOjq8YdhmuOtQWGPrBJh','35PaELKCOjq8YdhmuOtQWGPrBJh'),('35SZCuQonf4kzmioV1almTAYpY7','研发一部','35SZAmUMGKqvpWTC2EQODUKm59T',1,NULL,1,'Sir丶雨轩','2025-11-14 07:28:28.891000','Sir丶雨轩','2025-11-14 07:28:28.893000',0,'35PaELKCOjq8YdhmuOtQWGPrBJh','35PaELKCOjq8YdhmuOtQWGPrBJh');
/*!40000 ALTER TABLE `sys_dept` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-26 14:54:18

--
-- Seed data for table `sys_dict_type`
--

-- MySQL dump 10.13  Distrib 8.4.9, for macos26.4 (arm64)
--
-- Host: localhost    Database: fast_admin
-- ------------------------------------------------------
-- Server version	9.5.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `sys_dict_type`
--
-- WHERE:  is_deleted = 0

LOCK TABLES `sys_dict_type` WRITE;
/*!40000 ALTER TABLE `sys_dict_type` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_dict_type` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-26 14:54:18

--
-- Seed data for table `sys_dict_data`
--

-- MySQL dump 10.13  Distrib 8.4.9, for macos26.4 (arm64)
--
-- Host: localhost    Database: fast_admin
-- ------------------------------------------------------
-- Server version	9.5.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `sys_dict_data`
--
-- WHERE:  is_deleted = 0

LOCK TABLES `sys_dict_data` WRITE;
/*!40000 ALTER TABLE `sys_dict_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_dict_data` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-26 14:54:18

--
-- Seed data for table `sys_menu`
--

-- MySQL dump 10.13  Distrib 8.4.9, for macos26.4 (arm64)
--
-- Host: localhost    Database: fast_admin
-- ------------------------------------------------------
-- Server version	9.5.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `sys_menu`
--
-- WHERE:  is_deleted = 0

LOCK TABLES `sys_menu` WRITE;
/*!40000 ALTER TABLE `sys_menu` DISABLE KEYS */;
INSERT INTO `sys_menu` (`id`, `pid`, `name`, `code`, `type`, `status`, `path`, `active_path`, `component`, `icon`, `meta_active_icon`, `meta_title`, `meta_order`, `meta_affix_tab`, `meta_keep_alive`, `meta_hide_in_menu`, `meta_hide_children_in_menu`, `meta_hide_in_breadcrumb`, `meta_hide_in_tab`, `meta_badge`, `meta_badge_type`, `meta_badge_variants`, `meta_iframe_src`, `meta_link`, `remark`, `created_by`, `created_at`, `updated_by`, `updated_at`, `created_id`, `updated_id`, `is_deleted`) VALUES ('03a5e92d0929456796f1e06651f','sys_job_menu','SysJobEdit','system:job:edit',3,1,NULL,NULL,NULL,NULL,NULL,'编辑',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 02:45:09.156892',NULL,'2026-05-27 04:50:23.376662',NULL,NULL,0),('1671612b50b414ae76ed2935del','35fcfg9ef55798c3ca9611a9c14','SysFileConfigDelete','system:file:config:delete',3,1,NULL,NULL,NULL,NULL,NULL,'删除',2,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-15 14:19:11.748346',NULL,'2026-05-15 14:19:11.748346',NULL,NULL,0),('1b3eb8846f65413384429bc181d','sys_config_menu','SysConfigDel','system:config:delete',3,1,NULL,NULL,NULL,NULL,NULL,'删除',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 02:07:00.431864',NULL,'2026-05-27 04:50:23.468608',NULL,NULL,0),('20dec697ac954fb79364a7d3f2d','sys_job_menu','SysJobDel','system:job:delete',3,1,NULL,NULL,NULL,NULL,NULL,'删除',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 02:45:09.156892',NULL,'2026-05-27 04:50:23.468608',NULL,NULL,0),('3122e315d4ecf6d5c2f0ed35add','35fcfg9ef55798c3ca9611a9c14','SysFileConfigAdd','system:file:config:add',3,1,NULL,NULL,NULL,NULL,NULL,'新增',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-15 14:19:11.748346',NULL,'2026-05-15 14:19:11.748346',NULL,NULL,0),('35akSVTGkD6d2WkjR0LUue4Rg1H','35PlZtb7CCmpEZ5XWwTZqEX7fMZ','SysUser','system:user:list',1,1,'/system/user',NULL,'/system/user/list','carbon:user',NULL,'用户管理',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,'Sir丶雨轩','2025-11-17 04:59:29.579000','Sir丶雨轩','2025-11-17 04:59:29.580000','35PaELKCOjq8YdhmuOtQWGPrBJh','35PaELKCOjq8YdhmuOtQWGPrBJh',0),('35aN4sLcP2Gd9PDUhNqmImfscW0','35PlZtb7CCmpEZ5XWwTZqEX7fMZ','SysRole','system:role:list',1,1,'/system/role',NULL,'/system/role/list','carbon:user-military',NULL,'角色管理',0,0,0,0,0,0,0,'','normal','warning',NULL,NULL,NULL,'系统管理员','2025-11-17 01:47:14.240000','系统管理员','2025-11-17 01:47:14.251000','35PaELKCOjq8YdhmuOtQWGPrBJh','35PaELKCOjq8YdhmuOtQWGPrBJh',0),('35aNAOJVk7dc8Cstw9k6xee1SnN','35aN4sLcP2Gd9PDUhNqmImfscW0','SysRoleAdd','system:role:add',3,1,'',NULL,NULL,NULL,NULL,'新增',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,'系统管理员','2025-11-17 01:47:58.986000','系统管理员','2025-11-17 01:47:58.989000','35PaELKCOjq8YdhmuOtQWGPrBJh','35PaELKCOjq8YdhmuOtQWGPrBJh',0),('35aNDFxbMYFIkujj27AXDTmSgH7','35aN4sLcP2Gd9PDUhNqmImfscW0','SysRoleEdit','system:role:edit',3,1,'',NULL,NULL,NULL,NULL,'编辑',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,'系统管理员','2025-11-17 01:48:20.231000','系统管理员','2025-11-17 01:48:20.233000','35PaELKCOjq8YdhmuOtQWGPrBJh','35PaELKCOjq8YdhmuOtQWGPrBJh',0),('35aNG4IapjYOpcanRPKLpw77jwX','35aN4sLcP2Gd9PDUhNqmImfscW0','SysRoleDel','system:role:del',3,1,'',NULL,NULL,NULL,NULL,'删除',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,'系统管理员','2025-11-17 01:48:43.126000','系统管理员','2025-11-17 01:48:43.129000','35PaELKCOjq8YdhmuOtQWGPrBJh','35PaELKCOjq8YdhmuOtQWGPrBJh',0),('35fcfg9ef55798c3ca9611a9c14','35fmgr7c8366ccdcc31f6ab5de7','SysFileConfig','system:file:config:list',1,1,'/system/file-config',NULL,'/system/file-config/list','carbon:cloud-upload',NULL,'文件配置',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-15 14:07:33.886573',NULL,'2026-05-15 14:28:16.543026',NULL,NULL,0),('35flstd84b75d35ae70afd0a236','35fmgr7c8366ccdcc31f6ab5de7','SysFile','system:file:list',1,1,'/system/file',NULL,'/system/file/list','carbon:document',NULL,'文件列表',1,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-15 14:07:33.886573',NULL,'2026-05-15 14:28:16.618596',NULL,NULL,0),('35fmgr7c8366ccdcc31f6ab5de7','35PlZtb7CCmpEZ5XWwTZqEX7fMZ','FileManage',NULL,2,1,'/system/file-manage',NULL,NULL,'carbon:folder',NULL,'文件管理',12,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-15 14:28:15.544721',NULL,'2026-05-15 14:28:15.544721',NULL,NULL,0),('35PlZm6yfj0946lcdUPH1bWe1Za','35PlZt0sYaZm7yBghgvZHyDlQ8R','SystemDeptCreate','system:dept:add',3,1,NULL,NULL,NULL,NULL,NULL,'新增',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'创建部门按钮','system','2025-11-13 07:41:56.489829','system','2025-11-14 08:07:01.845283',NULL,NULL,0),('35PlZml410bKeFH9y1MGrV5lfEL','35PlZt0sYaZm7yBghgvZHyDlQ8R','SystemDeptDelete','system:dept:delete',3,1,NULL,NULL,NULL,NULL,NULL,'删除',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'删除部门按钮','system','2025-11-13 07:41:56.489829','system','2025-11-14 08:07:08.807869',NULL,NULL,0),('35PlZmoOOFFC7MbxfHKRSm37iS6','35PlZrV4t4FP7RmvAjH2hHxslTu','SystemMenuCreate','system:menu:add',3,1,NULL,NULL,NULL,NULL,NULL,'新增',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'创建菜单按钮','system','2025-11-13 07:41:56.489829','system','2025-11-14 08:03:01.289789',NULL,NULL,0),('35PlZmvi8gW3sDhyVIA8oe8YMLG',NULL,'Workspace',NULL,1,1,'/workspace',NULL,'/dashboard/workspace/index','carbon:workspace',NULL,'工作台',0,1,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'工作台菜单','system','2025-11-13 07:41:56.489829','system','2025-11-13 07:41:56.489829',NULL,NULL,0),('35PlZn0U8GQDyiNQwKvbe2Q2wJk','35PlZrV4t4FP7RmvAjH2hHxslTu','SystemMenuDelete','system:menu:delete',3,1,NULL,NULL,NULL,NULL,NULL,'删除',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'删除菜单按钮','system','2025-11-13 07:41:56.489829','system','2025-11-14 07:47:46.965411',NULL,NULL,0),('35PlZqPBF9fiZmsewd7xsKC9kB1','35PlZt0sYaZm7yBghgvZHyDlQ8R','SystemDeptEdit','system:dept:edit',3,1,NULL,NULL,NULL,NULL,NULL,'编辑',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'编辑部门按钮','system','2025-11-13 07:41:56.489829','system','2025-11-14 08:07:16.157794',NULL,NULL,0),('35PlZraW752xuXEuWlTXKDCXVD1','35PlZrV4t4FP7RmvAjH2hHxslTu','SystemMenuEdit','system:menu:edit',3,1,NULL,NULL,NULL,NULL,NULL,'编辑',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'编辑菜单按钮','system','2025-11-13 07:41:56.489829','system','2025-11-14 08:03:06.888146',NULL,NULL,0),('35PlZrV4t4FP7RmvAjH2hHxslTu','35PlZtb7CCmpEZ5XWwTZqEX7fMZ','SystemMenu','system:menu:list',1,1,'/system/menu',NULL,'/system/menu/list','carbon:menu',NULL,'菜单管理',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'菜单管理','system','2025-11-13 07:41:56.489829','system','2025-11-14 08:07:26.968823',NULL,NULL,0),('35PlZt0sYaZm7yBghgvZHyDlQ8R','35PlZtb7CCmpEZ5XWwTZqEX7fMZ','SystemDept','',1,1,'/system/dept',NULL,'/system/dept/list','carbon:container-services',NULL,'部门管理',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'部门管理','system','2025-11-13 07:41:56.489829','system','2025-11-14 08:07:20.933952',NULL,'35PaELKCOjq8YdhmuOtQWGPrBJh',0),('35PlZtb7CCmpEZ5XWwTZqEX7fMZ',NULL,'System',NULL,2,1,'/system',NULL,NULL,'carbon:settings',NULL,'系统管理',9997,0,0,0,0,0,0,'','','',NULL,NULL,'系统管理目录','system','2025-11-13 07:41:56.489829','system','2025-11-13 07:41:56.489829',NULL,NULL,0),('3Fd3MKuj63qczAhkfZ2npgGBNeH','ai_tool','AI执行SQL','ai:sql:execute',3,1,'',NULL,NULL,NULL,NULL,'AI执行SQL',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,'Sir丶雨轩','2026-06-25 12:37:49.989000','Sir丶雨轩','2026-06-25 12:37:49.989000','35PaELKCOjq8YdhmuOtQWGPrBJh','35PaELKCOjq8YdhmuOtQWGPrBJh',0),('4d8d3c3af4a945de80f241a181c','sys_dict_menu','SysDictDataEdit','system:dict:data:edit',3,1,NULL,NULL,NULL,NULL,NULL,'编辑',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 02:07:00.431864',NULL,'2026-05-27 04:50:23.376662',NULL,NULL,0),('56e811b5628643e3b88aee98b33','sys_dict_menu','SysDictTypeDel','system:dict:type:delete',3,1,NULL,NULL,NULL,NULL,NULL,'删除',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 02:07:00.431864',NULL,'2026-05-27 04:50:23.468608',NULL,NULL,0),('5b8f5eef71bb423d81d3b01d3f0','sys_dict_menu','SysDictTypeEdit','system:dict:type:edit',3,1,NULL,NULL,NULL,NULL,NULL,'编辑',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 02:07:00.431864',NULL,'2026-05-27 04:50:23.376662',NULL,NULL,0),('871bdf0bd7054b2a8e83cc1ce3c','sys_config_menu','SysConfigEdit','system:config:edit',3,1,NULL,NULL,NULL,NULL,NULL,'编辑',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 02:07:00.431864',NULL,'2026-05-27 04:50:23.376662',NULL,NULL,0),('871e5be9fe6e431eb105e467ceb','sys_dict_menu','SysDictDataAdd','system:dict:data:add',3,1,NULL,NULL,NULL,NULL,NULL,'新增',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 02:07:00.431864',NULL,'2026-05-27 04:50:23.296564',NULL,NULL,0),('a04831863f234156ae1cc6cd706','sys_job_menu','SysJobAdd','system:job:add',3,1,NULL,NULL,NULL,NULL,NULL,'新增',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 02:45:09.156892',NULL,'2026-05-27 04:50:23.296564',NULL,NULL,0),('ai_mcp','ai_ops','AiMcp','ai:mcp:list',1,1,'/ai/mcp',NULL,'ai/mcp/list','lucide:plug-zap',NULL,'MCP 管理',20,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'MCP 服务配置管理','system','2026-06-16 07:44:07.000000','system','2026-06-16 07:44:07.000000',NULL,NULL,0),('ai_mcp_add','ai_mcp','AiMcpAdd','ai:mcp:add',3,1,NULL,NULL,NULL,NULL,NULL,'新增',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,'system','2026-06-16 07:44:07.000000','system','2026-06-16 07:44:07.000000',NULL,NULL,0),('ai_mcp_del','ai_mcp','AiMcpDelete','ai:mcp:delete',3,1,NULL,NULL,NULL,NULL,NULL,'删除',2,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,'system','2026-06-16 07:44:07.000000','system','2026-06-16 07:44:07.000000',NULL,NULL,0),('ai_mcp_edit','ai_mcp','AiMcpEdit','ai:mcp:edit',3,1,NULL,NULL,NULL,NULL,NULL,'编辑',1,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,'system','2026-06-16 07:44:07.000000','system','2026-06-16 07:44:07.000000',NULL,NULL,0),('ai_model','ai_ops','AiModel','ai:model:list',1,1,'/ai/model',NULL,'ai/model/list','lucide:brain-circuit',NULL,'模型管理',10,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'AI 模型配置管理','system','2026-06-16 07:44:07.000000','system','2026-06-16 07:44:07.000000',NULL,NULL,0),('ai_model_active','ai_model','AiModelActivate','ai:model:activate',3,1,NULL,NULL,NULL,NULL,NULL,'设为当前',3,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,'system','2026-06-16 07:44:07.000000','system','2026-06-16 07:44:07.000000',NULL,NULL,0),('ai_model_add','ai_model','AiModelAdd','ai:model:add',3,1,NULL,NULL,NULL,NULL,NULL,'新增',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,'system','2026-06-16 07:44:07.000000','system','2026-06-16 07:44:07.000000',NULL,NULL,0),('ai_model_del','ai_model','AiModelDelete','ai:model:delete',3,1,NULL,NULL,NULL,NULL,NULL,'删除',2,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,'system','2026-06-16 07:44:07.000000','system','2026-06-16 07:44:07.000000',NULL,NULL,0),('ai_model_edit','ai_model','AiModelEdit','ai:model:edit',3,1,NULL,NULL,NULL,NULL,NULL,'编辑',1,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,'system','2026-06-16 07:44:07.000000','system','2026-06-16 07:44:07.000000',NULL,NULL,0),('ai_ops',NULL,'AiOps',NULL,2,1,'/ai',NULL,NULL,'lucide:bot',NULL,'AI 运维',9998,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'AI 运维管理目录','system','2026-06-16 07:44:07.000000','system','2026-06-16 07:44:07.000000',NULL,NULL,0),('ai_sql_readonly','ai_tool','AiSqlReadonly','ai:sql:readonly',3,1,NULL,NULL,NULL,NULL,NULL,'AI只读SQL',10,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'允许 AI 调用内置只读 SQL 工具','system','2026-06-25 04:31:16.000000','system','2026-06-25 04:31:16.000000',NULL,NULL,0),('ai_tool','ai_ops','AiTool','ai:tool:list',1,1,'/ai/tool',NULL,'ai/tool/list','lucide:wrench',NULL,'工具管理',30,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'AI 工具配置管理','system','2026-06-16 08:26:57.000000','system','2026-06-16 08:26:57.000000',NULL,NULL,0),('ai_tool_add','ai_tool','AiToolAdd','ai:tool:add',3,1,NULL,NULL,NULL,NULL,NULL,'新增',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,'system','2026-06-16 08:26:57.000000','system','2026-06-16 08:26:57.000000',NULL,NULL,0),('ai_tool_del','ai_tool','AiToolDelete','ai:tool:delete',3,1,NULL,NULL,NULL,NULL,NULL,'删除',2,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,'system','2026-06-16 08:26:57.000000','system','2026-06-16 08:26:57.000000',NULL,NULL,0),('ai_tool_edit','ai_tool','AiToolEdit','ai:tool:edit',3,1,NULL,NULL,NULL,NULL,NULL,'编辑',1,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,'system','2026-06-16 08:26:57.000000','system','2026-06-16 08:26:57.000000',NULL,NULL,0),('ai_tool_log','ai_ops','AiToolLog','ai:toollog:list',1,1,'/ai/tool-log',NULL,'ai/tool-log/list','lucide:scroll-text',NULL,'调用日志',40,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'AI 工具调用审计日志','system','2026-06-26 03:13:45.000000','system','2026-06-26 03:13:45.000000',NULL,NULL,0),('ai_usage','ai_ops','AiUsage','ai:usage:list',1,1,'/ai/usage',NULL,'ai/usage/index','lucide:chart-column',NULL,'用量统计',50,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'AI 对话 token 用量统计','system','2026-06-26 03:13:45.000000','system','2026-06-26 03:13:45.000000',NULL,NULL,0),('b271e8f9d6c64d8a980bceb44ef','sys_login_log','SysLoginLogDelete','system:log:login:delete',3,1,NULL,NULL,NULL,NULL,NULL,'删除',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 02:07:00.431864',NULL,'2026-05-27 04:50:23.468608',NULL,NULL,0),('c29db81ccafd49c3ac3e990ca1c','sys_online_menu','SysOnlineKickout','system:online:kickout',3,1,NULL,NULL,NULL,NULL,NULL,'强制下线',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 12:55:40.482144',NULL,'2026-05-16 12:55:40.655918',NULL,NULL,0),('c7086c22b7bd6706c8c25435upl','35flstd84b75d35ae70afd0a236','SysFileUpload','system:file:upload',3,1,NULL,NULL,NULL,NULL,NULL,'上传',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-15 14:19:11.748346',NULL,'2026-05-15 14:19:11.748346',NULL,NULL,0),('d206c1ee8dc24ba1ae2873ef0af','sys_op_log','SysOperationLogDelete','system:log:operation:delete',3,1,NULL,NULL,NULL,NULL,NULL,'删除',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 02:07:00.431864',NULL,'2026-05-27 04:50:23.468608',NULL,NULL,0),('d64c72cc8cce4c57a34e58d2a54','sys_config_menu','SysConfigAdd','system:config:add',3,1,NULL,NULL,NULL,NULL,NULL,'新增',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 02:07:00.431864',NULL,'2026-05-27 04:50:23.296564',NULL,NULL,0),('e3651e7cf7382d4202b5c035edt','35fcfg9ef55798c3ca9611a9c14','SysFileConfigEdit','system:file:config:edit',3,1,NULL,NULL,NULL,NULL,NULL,'编辑',1,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-15 14:19:11.748346',NULL,'2026-05-15 14:19:11.748346',NULL,NULL,0),('f0d731c8f0c54ce7b183d13e309','sys_dict_menu','SysDictTypeAdd','system:dict:type:add',3,1,NULL,NULL,NULL,NULL,NULL,'新增',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 02:07:00.431864',NULL,'2026-05-27 04:50:23.296564',NULL,NULL,0),('f1bb21997562811b8dbcd135dwn','35flstd84b75d35ae70afd0a236','SysFileDownload','system:file:download',3,1,NULL,NULL,NULL,NULL,NULL,'下载',1,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-15 14:19:11.748346',NULL,'2026-05-15 14:19:11.748346',NULL,NULL,0),('fccb25f29043e633715c8935fdl','35flstd84b75d35ae70afd0a236','SysFileDelete','system:file:delete',3,1,NULL,NULL,NULL,NULL,NULL,'删除',2,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-15 14:19:11.748346',NULL,'2026-05-15 14:19:11.748346',NULL,NULL,0),('fd3c5aa8b46940e6ad4c11ad873','sys_dict_menu','SysDictDataDel','system:dict:data:delete',3,1,NULL,NULL,NULL,NULL,NULL,'删除',0,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 02:07:00.431864',NULL,'2026-05-27 04:50:23.468608',NULL,NULL,0),('ff6aa327abf9329be65edb35act','35fcfg9ef55798c3ca9611a9c14','SysFileConfigActivate','system:file:config:activate',3,1,NULL,NULL,NULL,NULL,NULL,'激活',3,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-15 14:19:11.748346',NULL,'2026-05-15 14:19:11.748346',NULL,NULL,0),('sys_config_menu','35PlZtb7CCmpEZ5XWwTZqEX7fMZ','SysConfig','system:config:list',1,1,'/system/config',NULL,'system/config/list','lucide:sliders',NULL,'系统参数',60,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 02:07:00.431864',NULL,'2026-05-16 02:07:00.884507',NULL,NULL,0),('sys_dict_menu','35PlZtb7CCmpEZ5XWwTZqEX7fMZ','SysDict','system:dict:list',1,1,'/system/dict',NULL,'system/dict/list','lucide:book-marked',NULL,'数据字典',70,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 02:07:00.431864',NULL,'2026-05-16 02:07:01.190405',NULL,NULL,0),('sys_job_log_menu','sys_monitor','SysJobLog','system:job:log:list',1,1,'/system/job/log',NULL,'system/job/log/list','lucide:scroll-text',NULL,'任务日志',31,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 02:45:09.156892',NULL,'2026-05-16 02:45:09.577258',NULL,NULL,0),('sys_job_menu','sys_monitor','SysJob','system:job:list',1,1,'/system/job',NULL,'system/job/list','lucide:timer',NULL,'定时任务',30,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 02:45:09.156892',NULL,'2026-05-16 02:45:09.254377',NULL,NULL,0),('sys_login_log','sys_monitor','SysLoginLog','system:log:login:list',1,1,'/system/log/login',NULL,'system/log/login/list','lucide:log-in',NULL,'登录日志',20,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 02:07:00.431864',NULL,'2026-05-16 02:07:00.738623',NULL,NULL,0),('sys_monitor','35PlZtb7CCmpEZ5XWwTZqEX7fMZ','SysMonitor',NULL,2,1,'/system/monitor',NULL,NULL,'lucide:activity',NULL,'系统监控',50,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 02:07:00.431864',NULL,'2026-05-16 02:07:00.525766',NULL,NULL,0),('sys_online_menu','sys_monitor','SysOnline','system:online:list',1,1,'/system/monitor/online',NULL,'system/monitor/online/index','lucide:users-round',NULL,'在线用户',45,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 12:55:40.482144',NULL,'2026-05-16 12:55:40.583180',NULL,NULL,0),('sys_op_log','sys_monitor','SysOperationLog','system:log:operation:list',1,1,'/system/log/operation',NULL,'system/log/operation/list','lucide:scroll-text',NULL,'操作日志',10,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 02:07:00.431864',NULL,'2026-05-16 02:07:00.597420',NULL,NULL,0),('sys_server_monitor','sys_monitor','SysServerMonitor','system:monitor:server',1,1,'/system/monitor/server',NULL,'system/monitor/server/index','lucide:gauge',NULL,'服务监控',40,0,0,0,0,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 07:22:10.033120',NULL,'2026-05-16 07:22:10.113167',NULL,NULL,0);
/*!40000 ALTER TABLE `sys_menu` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-26 14:54:18

--
-- Seed data for table `sys_role`
--

-- MySQL dump 10.13  Distrib 8.4.9, for macos26.4 (arm64)
--
-- Host: localhost    Database: fast_admin
-- ------------------------------------------------------
-- Server version	9.5.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `sys_role`
--
-- WHERE:  id IN ('35PlNb9zFOIQVWx8YoSlGGxjhHF') AND is_deleted = 0

LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
INSERT INTO `sys_role` (`id`, `name`, `code`, `remark`, `is_enabled`, `created_by`, `created_at`, `updated_by`, `updated_at`, `is_deleted`, `created_id`, `updated_id`) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF','系统管理员','System','系统级管理员，拥有最高权限',1,NULL,'2025-11-13 07:39:55.905897','系统管理员','2025-11-13 07:39:55.905897',0,NULL,'35PaELKCOjq8YdhmuOtQWGPrBJh');
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-26 14:54:18

--
-- Seed data for table `sys_roles_menus`
--

-- MySQL dump 10.13  Distrib 8.4.9, for macos26.4 (arm64)
--
-- Host: localhost    Database: fast_admin
-- ------------------------------------------------------
-- Server version	9.5.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `sys_roles_menus`
--
-- WHERE:  role_id IN ('35PlNb9zFOIQVWx8YoSlGGxjhHF') AND menu_id IN (SELECT id FROM sys_menu WHERE is_deleted = 0)

LOCK TABLES `sys_roles_menus` WRITE;
/*!40000 ALTER TABLE `sys_roles_menus` DISABLE KEYS */;
INSERT INTO `sys_roles_menus` (`role_id`, `menu_id`) VALUES ('35PlNb9zFOIQVWx8YoSlGGxjhHF','03a5e92d0929456796f1e06651f'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','1671612b50b414ae76ed2935del'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','1b3eb8846f65413384429bc181d'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','20dec697ac954fb79364a7d3f2d'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','3122e315d4ecf6d5c2f0ed35add'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','35akSVTGkD6d2WkjR0LUue4Rg1H'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','35aN4sLcP2Gd9PDUhNqmImfscW0'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','35aNAOJVk7dc8Cstw9k6xee1SnN'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','35aNDFxbMYFIkujj27AXDTmSgH7'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','35aNG4IapjYOpcanRPKLpw77jwX'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','35fcfg9ef55798c3ca9611a9c14'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','35flstd84b75d35ae70afd0a236'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','35fmgr7c8366ccdcc31f6ab5de7'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','35PlZm6yfj0946lcdUPH1bWe1Za'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','35PlZml410bKeFH9y1MGrV5lfEL'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','35PlZmoOOFFC7MbxfHKRSm37iS6'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','35PlZmvi8gW3sDhyVIA8oe8YMLG'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','35PlZn0U8GQDyiNQwKvbe2Q2wJk'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','35PlZqPBF9fiZmsewd7xsKC9kB1'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','35PlZraW752xuXEuWlTXKDCXVD1'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','35PlZrV4t4FP7RmvAjH2hHxslTu'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','35PlZt0sYaZm7yBghgvZHyDlQ8R'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','35PlZtb7CCmpEZ5XWwTZqEX7fMZ'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','3Fd3MKuj63qczAhkfZ2npgGBNeH'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','4d8d3c3af4a945de80f241a181c'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','56e811b5628643e3b88aee98b33'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','5b8f5eef71bb423d81d3b01d3f0'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','871bdf0bd7054b2a8e83cc1ce3c'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','871e5be9fe6e431eb105e467ceb'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','a04831863f234156ae1cc6cd706'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','ai_mcp'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','ai_mcp_add'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','ai_mcp_del'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','ai_mcp_edit'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','ai_model'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','ai_model_active'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','ai_model_add'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','ai_model_del'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','ai_model_edit'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','ai_ops'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','ai_sql_readonly'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','ai_tool'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','ai_tool_add'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','ai_tool_del'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','ai_tool_edit'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','ai_tool_log'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','ai_usage'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','b271e8f9d6c64d8a980bceb44ef'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','c29db81ccafd49c3ac3e990ca1c'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','c7086c22b7bd6706c8c25435upl'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','d206c1ee8dc24ba1ae2873ef0af'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','d64c72cc8cce4c57a34e58d2a54'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','e3651e7cf7382d4202b5c035edt'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','f0d731c8f0c54ce7b183d13e309'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','f1bb21997562811b8dbcd135dwn'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','fccb25f29043e633715c8935fdl'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','fd3c5aa8b46940e6ad4c11ad873'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','ff6aa327abf9329be65edb35act'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','sys_config_menu'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','sys_dict_menu'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','sys_job_log_menu'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','sys_job_menu'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','sys_login_log'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','sys_monitor'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','sys_online_menu'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','sys_op_log'),('35PlNb9zFOIQVWx8YoSlGGxjhHF','sys_server_monitor');
/*!40000 ALTER TABLE `sys_roles_menus` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-26 14:54:18

--
-- Seed data for table `sys_user`
--

-- MySQL dump 10.13  Distrib 8.4.9, for macos26.4 (arm64)
--
-- Host: localhost    Database: fast_admin
-- ------------------------------------------------------
-- Server version	9.5.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `sys_user`
--
-- WHERE:  id = '3FevgYob1gUFuwgc0AnlsbhdkzK' AND username = 'admin' AND is_deleted = 0

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` (`id`, `dept_id`, `username`, `password`, `email`, `phone`, `nickname`, `avatar`, `status`, `sex`, `login_ip`, `login_city`, `login_time`, `created_by`, `created_at`, `updated_by`, `updated_at`, `is_deleted`, `created_id`, `updated_id`) VALUES ('3FevgYob1gUFuwgc0AnlsbhdkzK','35SYbLjJaBxhMOAl7LdELC3Zezi','admin','$2a$10$XAdN.cVbK71pIQJVyJnd0OVnpygrgplDE9QQg3q5cztEGb/dyfdla',NULL,NULL,'超级管理员',NULL,0,1,NULL,NULL,NULL,'Sir丶雨轩','2026-06-26 04:34:20.982000','Sir丶雨轩','2026-06-26 04:34:20.982000',0,'35PaELKCOjq8YdhmuOtQWGPrBJh','35PaELKCOjq8YdhmuOtQWGPrBJh');
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-26 14:54:18

--
-- Seed data for table `sys_users_roles`
--

-- MySQL dump 10.13  Distrib 8.4.9, for macos26.4 (arm64)
--
-- Host: localhost    Database: fast_admin
-- ------------------------------------------------------
-- Server version	9.5.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `sys_users_roles`
--
-- WHERE:  user_id = '3FevgYob1gUFuwgc0AnlsbhdkzK' AND role_id IN ('35PlNb9zFOIQVWx8YoSlGGxjhHF')

LOCK TABLES `sys_users_roles` WRITE;
/*!40000 ALTER TABLE `sys_users_roles` DISABLE KEYS */;
INSERT INTO `sys_users_roles` (`user_id`, `role_id`) VALUES ('3FevgYob1gUFuwgc0AnlsbhdkzK','35PlNb9zFOIQVWx8YoSlGGxjhHF');
/*!40000 ALTER TABLE `sys_users_roles` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-26 14:54:18

--
-- Seed data for table `ai_tool_config`
--

-- MySQL dump 10.13  Distrib 8.4.9, for macos26.4 (arm64)
--
-- Host: localhost    Database: fast_admin
-- ------------------------------------------------------
-- Server version	9.5.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `ai_tool_config`
--
-- WHERE:  system_builtin = 1 AND is_deleted = 0

LOCK TABLES `ai_tool_config` WRITE;
/*!40000 ALTER TABLE `ai_tool_config` DISABLE KEYS */;
INSERT INTO `ai_tool_config` (`id`, `name`, `tool_code`, `type`, `description`, `enabled`, `system_builtin`, `permission_code`, `method`, `url`, `headers_json`, `body_template`, `sql_text`, `read_only`, `timeout_ms`, `remark`, `created_by`, `created_id`, `created_at`, `updated_by`, `updated_id`, `updated_at`, `is_deleted`) VALUES ('builtin_describe_schema','查询表结构','describe_schema','sql','系统内置工具：读取 information_schema，返回库表名或指定表的字段定义，供模型写 SQL 前确认结构。',1,1,'ai:sql:readonly',NULL,NULL,NULL,NULL,NULL,1,10000,'系统内置，开关来自系统参数 ai.schema-tool.enabled','system',NULL,'2026-06-26 11:13:45','system',NULL,'2026-06-26 11:13:45',0),('builtin_execute_readonly_sql','执行只读 SQL','execute_readonly_sql','sql','系统内置工具：执行单条只读 SQL，返回 JSON 结果。仅允许 select/show/desc/describe/explain。',1,1,'ai:sql:readonly',NULL,NULL,NULL,NULL,NULL,1,10000,'系统内置，配置项来自系统参数表','system',NULL,'2026-06-25 15:11:16','system',NULL,'2026-06-25 15:11:16',0),('builtin_execute_sql','执行任意 SQL','execute_sql','sql','系统内置工具：执行任意 SQL（select/insert/update/delete/ddl），执行前需用户二次确认。',1,1,'ai:sql:execute',NULL,NULL,NULL,NULL,NULL,0,10000,'系统内置，开关来自系统参数 ai.execute-sql.enabled','system',NULL,'2026-06-25 20:06:21','system',NULL,'2026-06-25 20:06:21',0);
/*!40000 ALTER TABLE `ai_tool_config` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-26 14:54:18
