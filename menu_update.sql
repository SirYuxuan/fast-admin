-- 查询现有菜单结构
SELECT id, pid, name, code, type, path, component FROM sys_menu WHERE is_deleted = 0 ORDER BY pid, meta_order;

-- 查询角色菜单关系
SELECT rm.id, rm.role_id, rm.menu_id, m.name as menu_name FROM sys_roles_menus rm
LEFT JOIN sys_menu m ON rm.menu_id = m.id
WHERE m.is_deleted = 0
ORDER BY rm.role_id, m.name;

-- ========== 菜单调整 ==========

-- 1. 如果不存在"系统管理"菜单，则创建
INSERT IGNORE INTO sys_menu (id, pid, name, code, type, status, path, component, icon, meta_title, meta_order, created_at, is_deleted)
VALUES ('system_mgmt', NULL, '系统管理', NULL, 'CATALOG', 1, 'system', NULL, 'ion:settings-outline', '系统管理', 100, NOW(), 0);

-- 2. 获取系统管理菜单ID（用于后续操作）
SET @system_menu_id = (SELECT id FROM sys_menu WHERE name = '系统管理' AND is_deleted = 0 LIMIT 1);

-- 3. 创建"文件管理"菜单（如果不存在）作为系统管理的子菜单
INSERT IGNORE INTO sys_menu (id, pid, name, code, type, status, path, component, icon, meta_title, meta_order, created_at, is_deleted)
VALUES ('file_management', @system_menu_id, '文件管理', NULL, 'CATALOG', 1, 'file', NULL, 'ion:document-outline', '文件管理', 10, NOW(), 0);

-- 4. 获取文件管理菜单ID
SET @file_menu_id = (SELECT id FROM sys_menu WHERE name = '文件管理' AND is_deleted = 0 LIMIT 1);

-- 5. 更新或创建"文件配置"菜单，放在"文件管理"下
UPDATE sys_menu
SET pid = @file_menu_id, meta_order = 1
WHERE name = '文件配置' AND is_deleted = 0;

-- 如果不存在，则创建
INSERT IGNORE INTO sys_menu (id, pid, name, code, type, status, path, component, icon, meta_title, meta_order, created_at, is_deleted)
SELECT 'file_config_menu', @file_menu_id, '文件配置', 'system:file:config:list', 'MENU', 1, 'system/file-config', 'system/file-config/index', 'ion:settings-outline', '文件存储配置', 1, NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE name = '文件配置' AND is_deleted = 0);

-- 6. 更新或创建"文件列表"菜单，放在"文件管理"下
UPDATE sys_menu
SET pid = @file_menu_id, meta_order = 2
WHERE name = '文件列表' AND is_deleted = 0;

-- 如果不存在，则创建
INSERT IGNORE INTO sys_menu (id, pid, name, code, type, status, path, component, icon, meta_title, meta_order, created_at, is_deleted)
SELECT 'file_list_menu', @file_menu_id, '文件列表', 'system:file:list', 'MENU', 1, 'system/file', 'system/file/index', 'ion:document-text-outline', '文件列表', 2, NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE name = '文件列表' AND is_deleted = 0);

-- 7. 验证菜单结构
SELECT '========== 调整后的菜单结构 ==========' as status;
SELECT id, pid, name, code, type, path, meta_order FROM sys_menu WHERE is_deleted = 0 ORDER BY pid, meta_order;
