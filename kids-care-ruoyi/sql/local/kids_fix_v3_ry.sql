-- =============================================================
-- 幼儿园管理系统 · 若依版 增量修复脚本 v3
-- 适用对象：已经初始化过的数据库（执行过 ry / kids_migrate_v1 / kids_menu_v2）
-- 幂等：可重复执行
-- 内容：
--   1. 收回教师的「幼儿删除」权限（教师可新增/修改档案，删档只留给园长）
--   2. 家长端补「通知公告」只读菜单（挂在「我的孩子」下，园长不可见）
--   3. 公告类型字典补齐「园务 / 班级 / 健康」三项
--   4. 数据修复：把「已发布但发布时间在将来」的历史公告改为定时待发
-- =============================================================
USE kids_care_ry;
SET NAMES utf8mb4;

-- -------------------------------------------------------------
-- 1. 教师收回幼儿删除权限
-- -------------------------------------------------------------
DELETE FROM sys_role_menu WHERE role_id = 100 AND menu_id = 2114;

-- -------------------------------------------------------------
-- 2. 家长端通知公告菜单（remark = 'parent-only'，园长路由会被剔除）
-- -------------------------------------------------------------
DELETE FROM sys_role_menu WHERE menu_id = 2520;
DELETE FROM sys_menu WHERE menu_id = 2520;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2520, '通知公告', 2500, 2, 'notice', 'kids/notice/index', '', '', 1, 0, 'C', '0', '0', 'kids:notice:list', 'notification', 'admin', SYSDATE(), '', NULL, 'parent-only');

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 101, 2520 FROM DUAL
WHERE EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2520);

-- -------------------------------------------------------------
-- 3. 公告类型字典补齐（已存在的值不重复插入）
-- -------------------------------------------------------------
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
SELECT 5, '园务', '园务', 'notice_type', '', 'info',    'N', '0', 'admin', SYSDATE(), '', NULL, '公告类型' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'notice_type' AND dict_value = '园务');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
SELECT 6, '班级', '班级', 'notice_type', '', 'primary', 'N', '0', 'admin', SYSDATE(), '', NULL, '公告类型' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'notice_type' AND dict_value = '班级');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
SELECT 7, '健康', '健康', 'notice_type', '', 'success', 'N', '0', 'admin', SYSDATE(), '', NULL, '公告类型' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'notice_type' AND dict_value = '健康');

-- -------------------------------------------------------------
-- 4. 数据修复：已发布但发布时间还没到的公告，回调为定时待发
--    这类记录会让家长提前看到未到点的公告
-- -------------------------------------------------------------
UPDATE notice SET status = 1, update_time = SYSDATE()
WHERE deleted = 0 AND status = 2 AND publish_time IS NOT NULL AND publish_time > SYSDATE();

-- -------------------------------------------------------------
-- 5. 校验
-- -------------------------------------------------------------
SELECT '教师幼儿删除权限(应为0)' AS item, COUNT(*) AS cnt FROM sys_role_menu WHERE role_id = 100 AND menu_id = 2114
UNION ALL SELECT '家长公告菜单(应为1)', COUNT(*) FROM sys_role_menu WHERE role_id = 101 AND menu_id = 2520
UNION ALL SELECT '公告类型字典(应为7)', COUNT(*) FROM sys_dict_data WHERE dict_type = 'notice_type'
UNION ALL SELECT '仍提前可见的公告(应为0)', COUNT(*) FROM notice WHERE deleted = 0 AND status = 2 AND publish_time > SYSDATE();
