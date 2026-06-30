-- =============================================================
-- fast-flow 工作流模块 —— 业务表与菜单
-- 说明：Flowable 引擎自身的 ACT_* 表由 flowable.database-schema-update
--       在应用启动时自动创建，无需在此维护。
-- =============================================================

-- ----------------------------
-- 流程模型表（设计态）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `flow_model` (
  `id` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID（KSUID）',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型名称',
  `model_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流程标识（BPMN process id）',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类',
  `bpmn_xml` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'BPMN 2.0 XML',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `latest_deploy_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最近部署ID',
  `latest_definition_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最近流程定义ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '模型状态：1启用 0停用',
  `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人名字',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人名字',
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `created_id` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人ID',
  `updated_id` varchar(27) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_flow_model_key` (`model_key`),
  KEY `idx_flow_model_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程模型表（设计态）';

-- ----------------------------
-- 菜单与权限（顶级目录「流程管理」+ 流程模型 / 流程定义）
-- 父目录 meta_order=9996 置于「AI 运维(9998)」之前
-- ----------------------------
INSERT INTO `sys_menu` (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`component`,`icon`,`meta_title`,`meta_order`) VALUES
 ('flow_root',NULL,'Flow',NULL,2,1,'/flow',NULL,'lucide:workflow','流程管理',9996),
 ('flow_model','flow_root','FlowModel','flow:model:list',1,1,'/flow/model','flow/model/list','lucide:git-branch','流程模型',10),
 ('flow_model_add','flow_model','FlowModelAdd','flow:model:add',3,1,NULL,NULL,NULL,'新增',0),
 ('flow_model_edit','flow_model','FlowModelEdit','flow:model:edit',3,1,NULL,NULL,NULL,'编辑',1),
 ('flow_model_del','flow_model','FlowModelDel','flow:model:delete',3,1,NULL,NULL,NULL,'删除',2),
 ('flow_model_design','flow_model','FlowModelDesign','flow:model:design',3,1,NULL,NULL,NULL,'设计',3),
 ('flow_model_deploy','flow_model','FlowModelDeploy','flow:model:deploy',3,1,NULL,NULL,NULL,'部署',4),
 ('flow_definition','flow_root','FlowDefinition','flow:definition:list',1,1,'/flow/definition','flow/definition/list','lucide:list-tree','流程定义',20),
 ('flow_def_suspend','flow_definition','FlowDefSuspend','flow:definition:suspend',3,1,NULL,NULL,NULL,'挂起/激活',0),
 ('flow_def_del','flow_definition','FlowDefDel','flow:definition:delete',3,1,NULL,NULL,NULL,'删除',1)
ON DUPLICATE KEY UPDATE `meta_title`=VALUES(`meta_title`);

-- ----------------------------
-- 审批记录 / 抄送 / 自定义表单 表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `flow_task_record` (
  `id` varchar(27) NOT NULL,
  `process_instance_id` varchar(64) DEFAULT NULL COMMENT '流程实例ID',
  `task_id` varchar(64) DEFAULT NULL COMMENT '任务ID',
  `task_name` varchar(100) DEFAULT NULL COMMENT '任务节点名称',
  `assignee_id` varchar(27) DEFAULT NULL COMMENT '处理人ID',
  `assignee_name` varchar(50) DEFAULT NULL COMMENT '处理人名称',
  `outcome` varchar(20) DEFAULT NULL COMMENT '动作：start/approve/reject/transfer/cancel',
  `comment` varchar(1000) DEFAULT NULL COMMENT '审批意见',
  `created_by` varchar(50) DEFAULT NULL,
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_by` varchar(50) DEFAULT NULL,
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `created_id` varchar(27) DEFAULT NULL,
  `updated_id` varchar(27) DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_ftr_instance` (`process_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程审批记录';

CREATE TABLE IF NOT EXISTS `flow_cc_record` (
  `id` varchar(27) NOT NULL,
  `process_instance_id` varchar(64) DEFAULT NULL COMMENT '流程实例ID',
  `process_name` varchar(100) DEFAULT NULL COMMENT '流程名称',
  `task_name` varchar(100) DEFAULT NULL COMMENT '触发节点名称',
  `cc_user_id` varchar(27) DEFAULT NULL COMMENT '被抄送人ID',
  `cc_user_name` varchar(50) DEFAULT NULL COMMENT '被抄送人名称',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '抄送发起人名称',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '是否已读：1是 0否',
  `created_by` varchar(50) DEFAULT NULL,
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_by` varchar(50) DEFAULT NULL,
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `created_id` varchar(27) DEFAULT NULL,
  `updated_id` varchar(27) DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_fcc_user` (`cc_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程抄送记录';

CREATE TABLE IF NOT EXISTS `flow_form` (
  `id` varchar(27) NOT NULL,
  `form_key` varchar(100) NOT NULL COMMENT '表单标识（与BPMN formKey对应）',
  `name` varchar(100) NOT NULL COMMENT '表单名称',
  `content` longtext COMMENT '表单结构JSON（字段数组）',
  `remark` varchar(500) DEFAULT NULL,
  `created_by` varchar(50) DEFAULT NULL,
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_by` varchar(50) DEFAULT NULL,
  `updated_at` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `created_id` varchar(27) DEFAULT NULL,
  `updated_id` varchar(27) DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_fform_key` (`form_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程自定义表单';

-- ----------------------------
-- 流程中心菜单（待办/已办/我发起的/抄送/发起）+ 表单管理
-- ----------------------------
INSERT INTO `sys_menu` (`id`,`pid`,`name`,`code`,`type`,`status`,`path`,`component`,`icon`,`meta_title`,`meta_order`) VALUES
 ('flow_form','flow_root','FlowForm','flow:form:list',1,1,'/flow/form','flow/form/list','lucide:layout-template','表单管理',30),
 ('flow_form_add','flow_form','FlowFormAdd','flow:form:add',3,1,NULL,NULL,NULL,'新增',0),
 ('flow_form_edit','flow_form','FlowFormEdit','flow:form:edit',3,1,NULL,NULL,NULL,'编辑',1),
 ('flow_form_del','flow_form','FlowFormDel','flow:form:delete',3,1,NULL,NULL,NULL,'删除',2),
 ('flow_center',NULL,'FlowCenter',NULL,2,1,'/flow-center',NULL,'lucide:inbox','流程中心',9995),
 ('flow_start','flow_center','FlowStart','flow:process:start',1,1,'/flow-center/start','flow/center/start','lucide:send','发起流程',10),
 ('flow_todo','flow_center','FlowTodo','flow:task:todo',1,1,'/flow-center/todo','flow/center/todo','lucide:check-square','待办任务',20),
 ('flow_initiated','flow_center','FlowInitiated','flow:process:initiated',1,1,'/flow-center/initiated','flow/center/initiated','lucide:file-clock','我发起的',30),
 ('flow_done','flow_center','FlowDone','flow:task:done',1,1,'/flow-center/done','flow/center/done','lucide:check-check','我的已办',40),
 ('flow_cc','flow_center','FlowCc','flow:cc:list',1,1,'/flow-center/cc','flow/center/cc','lucide:copy','抄送我的',50)
ON DUPLICATE KEY UPDATE `meta_title`=VALUES(`meta_title`);

-- 授权给系统管理员角色（设计 + 运行时全部）
INSERT INTO `sys_roles_menus` (`role_id`,`menu_id`)
SELECT '35PlNb9zFOIQVWx8YoSlGGxjhHF', m.id FROM `sys_menu` m
WHERE m.id IN ('flow_root','flow_model','flow_model_add','flow_model_edit','flow_model_del',
               'flow_model_design','flow_model_deploy','flow_definition','flow_def_suspend','flow_def_del',
               'flow_form','flow_form_add','flow_form_edit','flow_form_del',
               'flow_center','flow_start','flow_todo','flow_initiated','flow_done','flow_cc')
ON DUPLICATE KEY UPDATE `role_id`=VALUES(`role_id`);
