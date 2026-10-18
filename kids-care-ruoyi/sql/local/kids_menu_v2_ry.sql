-- =============================================================
-- 幼儿园管理系统 · 若依菜单与权限初始化 v2
-- 幂等：可重复执行
-- 菜单编号段：2000-2599 业务菜单；1000-1999 为若依演示菜单（本脚本清除）
-- =============================================================
USE kids_care_ry;
SET NAMES utf8mb4;

-- 清理演示菜单与官网入口
DELETE FROM sys_role_menu WHERE menu_id >= 1000 AND menu_id < 2000;
DELETE FROM sys_menu WHERE menu_id >= 1000 AND menu_id < 2000;
DELETE FROM sys_role_menu WHERE menu_id = 4;
DELETE FROM sys_menu WHERE menu_id = 4;

-- 清理本脚本管辖的菜单与授权（便于重复执行）
DELETE FROM sys_role_menu WHERE menu_id >= 2000 AND menu_id < 2600;
DELETE FROM sys_menu WHERE menu_id >= 2000 AND menu_id < 2600;

-- -------------------------------------------------------------
-- 一级：工作台 / 我的孩子
-- remark = 'parent-only' 为约定标记：仅家长端可见，园长（超级管理员）的路由里会被剔除
-- -------------------------------------------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES
(2000, '工作台',   0, 0, 'dashboard', 'kids/dashboard/index', '', '', 1, 0, 'C', '0', '0', '', 'dashboard', 'admin', SYSDATE(), '', NULL, '数据看板'),
(2500, '我的孩子', 0, 9, 'mychild',    NULL,                   '', '', 1, 0, 'M', '0', '0', '', 'heart', 'admin', SYSDATE(), '', NULL, 'parent-only'),
(2510, '孩子档案', 2500, 1, 'index',    'kids/my-child/index',  '', '', 1, 0, 'C', '0', '0', 'kids:mychild:view', 'people', 'admin', SYSDATE(), '', NULL, 'parent-only');

-- -------------------------------------------------------------
-- 幼儿管理
-- -------------------------------------------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES
(2100, '幼儿管理', 0, 1, 'child', NULL, '', '', 1, 0, 'M', '0', '0', '', 'user', 'admin', SYSDATE(), '', NULL, '幼儿档案与班级、教职工'),
(2110, '幼儿档案', 2100, 1, 'list', 'kids/child/index', '', '', 1, 0, 'C', '0', '0', 'kids:child:list', 'peoples', 'admin', SYSDATE(), '', NULL, ''),
(2111, '幼儿查询', 2110, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:child:query',  '#', 'admin', SYSDATE(), '', NULL, ''),
(2112, '幼儿新增', 2110, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:child:add',    '#', 'admin', SYSDATE(), '', NULL, ''),
(2113, '幼儿修改', 2110, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:child:edit',   '#', 'admin', SYSDATE(), '', NULL, ''),
(2114, '幼儿删除', 2110, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:child:remove', '#', 'admin', SYSDATE(), '', NULL, ''),
(2115, '幼儿导出', 2110, 5, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:child:export', '#', 'admin', SYSDATE(), '', NULL, ''),
(2116, '幼儿导入', 2110, 6, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:child:import', '#', 'admin', SYSDATE(), '', NULL, ''),

(2120, '班级管理', 2100, 2, 'clazz', 'kids/clazz/index', '', '', 1, 0, 'C', '0', '0', 'kids:clazz:list', 'tree', 'admin', SYSDATE(), '', NULL, ''),
(2121, '班级查询', 2120, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:clazz:query',  '#', 'admin', SYSDATE(), '', NULL, ''),
(2122, '班级新增', 2120, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:clazz:add',    '#', 'admin', SYSDATE(), '', NULL, ''),
(2123, '班级修改', 2120, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:clazz:edit',   '#', 'admin', SYSDATE(), '', NULL, ''),
(2124, '班级删除', 2120, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:clazz:remove', '#', 'admin', SYSDATE(), '', NULL, ''),
(2125, '班级导出', 2120, 5, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:clazz:export', '#', 'admin', SYSDATE(), '', NULL, ''),

(2130, '教职工管理', 2100, 3, 'teacher', 'kids/teacher/index', '', '', 1, 0, 'C', '0', '0', 'kids:teacher:list', 'post', 'admin', SYSDATE(), '', NULL, ''),
(2131, '职工查询', 2130, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:teacher:query',  '#', 'admin', SYSDATE(), '', NULL, ''),
(2132, '职工新增', 2130, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:teacher:add',    '#', 'admin', SYSDATE(), '', NULL, ''),
(2133, '职工修改', 2130, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:teacher:edit',   '#', 'admin', SYSDATE(), '', NULL, ''),
(2134, '职工删除', 2130, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:teacher:remove', '#', 'admin', SYSDATE(), '', NULL, ''),
(2135, '职工导出', 2130, 5, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:teacher:export', '#', 'admin', SYSDATE(), '', NULL, '');

-- -------------------------------------------------------------
-- 教学管理
-- -------------------------------------------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES
(2200, '教学管理', 0, 2, 'teach', NULL, '', '', 1, 0, 'M', '0', '0', '', 'education', 'admin', SYSDATE(), '', NULL, ''),
(2210, '课程管理', 2200, 1, 'course', 'kids/course/index', '', '', 1, 0, 'C', '0', '0', 'kids:course:list', 'form', 'admin', SYSDATE(), '', NULL, ''),
(2211, '课程查询', 2210, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:course:query',  '#', 'admin', SYSDATE(), '', NULL, ''),
(2212, '课程新增', 2210, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:course:add',    '#', 'admin', SYSDATE(), '', NULL, ''),
(2213, '课程修改', 2210, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:course:edit',   '#', 'admin', SYSDATE(), '', NULL, ''),
(2214, '课程删除', 2210, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:course:remove', '#', 'admin', SYSDATE(), '', NULL, ''),

(2220, '考勤管理', 2200, 2, 'attendance', 'kids/attendance/index', '', '', 1, 0, 'C', '0', '0', 'kids:attendance:list', 'date', 'admin', SYSDATE(), '', NULL, ''),
(2221, '考勤查询', 2220, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:attendance:query',  '#', 'admin', SYSDATE(), '', NULL, ''),
(2222, '考勤登记', 2220, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:attendance:add',    '#', 'admin', SYSDATE(), '', NULL, ''),
(2223, '考勤修改', 2220, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:attendance:edit',   '#', 'admin', SYSDATE(), '', NULL, ''),
(2224, '考勤删除', 2220, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:attendance:remove', '#', 'admin', SYSDATE(), '', NULL, ''),

(2230, '请假管理', 2200, 3, 'leave', 'kids/leave/index', '', '', 1, 0, 'C', '0', '0', 'kids:leave:list', 'message', 'admin', SYSDATE(), '', NULL, ''),
(2231, '请假查询', 2230, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:leave:query',   '#', 'admin', SYSDATE(), '', NULL, ''),
(2232, '请假申请', 2230, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:leave:add',     '#', 'admin', SYSDATE(), '', NULL, ''),
(2233, '请假修改', 2230, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:leave:edit',    '#', 'admin', SYSDATE(), '', NULL, ''),
(2234, '请假删除', 2230, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:leave:remove',  '#', 'admin', SYSDATE(), '', NULL, ''),
(2235, '请假审批', 2230, 5, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:leave:approve', '#', 'admin', SYSDATE(), '', NULL, '');

-- -------------------------------------------------------------
-- 后勤管理
-- -------------------------------------------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES
(2300, '后勤管理', 0, 3, 'logistics', NULL, '', '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', SYSDATE(), '', NULL, ''),
(2310, '每周食谱', 2300, 1, 'menu', 'kids/menu/index', '', '', 1, 0, 'C', '0', '0', 'kids:menu:list', 'list', 'admin', SYSDATE(), '', NULL, ''),
(2311, '食谱查询', 2310, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:menu:query',   '#', 'admin', SYSDATE(), '', NULL, ''),
(2312, '食谱新增', 2310, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:menu:add',     '#', 'admin', SYSDATE(), '', NULL, ''),
(2313, '食谱修改', 2310, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:menu:edit',    '#', 'admin', SYSDATE(), '', NULL, ''),
(2314, '食谱删除', 2310, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:menu:remove',  '#', 'admin', SYSDATE(), '', NULL, ''),
(2315, '复制上周', 2310, 5, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:menu:copy',    '#', 'admin', SYSDATE(), '', NULL, ''),

(2320, '食材库存', 2300, 2, 'food', 'kids/food/index', '', '', 1, 0, 'C', '0', '0', 'kids:food:list', 'component', 'admin', SYSDATE(), '', NULL, ''),
(2321, '食材查询', 2320, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:food:query',   '#', 'admin', SYSDATE(), '', NULL, ''),
(2322, '食材新增', 2320, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:food:add',     '#', 'admin', SYSDATE(), '', NULL, ''),
(2323, '食材修改', 2320, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:food:edit',    '#', 'admin', SYSDATE(), '', NULL, ''),
(2324, '食材删除', 2320, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:food:remove',  '#', 'admin', SYSDATE(), '', NULL, ''),
(2325, '出入库',   2320, 5, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:food:stock',   '#', 'admin', SYSDATE(), '', NULL, '');

-- -------------------------------------------------------------
-- 财务与公告
-- -------------------------------------------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES
(2400, '财务与公告', 0, 4, 'finance', NULL, '', '', 1, 0, 'M', '0', '0', '', 'money', 'admin', SYSDATE(), '', NULL, ''),
(2410, '收费管理', 2400, 1, 'fee', 'kids/fee/index', '', '', 1, 0, 'C', '0', '0', 'kids:fee:list', 'money', 'admin', SYSDATE(), '', NULL, ''),
(2411, '收费查询', 2410, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:fee:query',  '#', 'admin', SYSDATE(), '', NULL, ''),
(2412, '收费新增', 2410, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:fee:add',    '#', 'admin', SYSDATE(), '', NULL, ''),
(2413, '收费修改', 2410, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:fee:edit',   '#', 'admin', SYSDATE(), '', NULL, ''),
(2414, '收费删除', 2410, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:fee:remove', '#', 'admin', SYSDATE(), '', NULL, ''),
(2415, '确认收款', 2410, 5, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:fee:pay',    '#', 'admin', SYSDATE(), '', NULL, ''),

(2420, '通知公告', 2400, 2, 'notice', 'kids/notice/index', '', '', 1, 0, 'C', '0', '0', 'kids:notice:list', 'notification', 'admin', SYSDATE(), '', NULL, ''),
(2421, '公告查询', 2420, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:notice:query',   '#', 'admin', SYSDATE(), '', NULL, ''),
(2422, '公告新增', 2420, 2, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:notice:add',     '#', 'admin', SYSDATE(), '', NULL, ''),
(2423, '公告修改', 2420, 3, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:notice:edit',    '#', 'admin', SYSDATE(), '', NULL, ''),
(2424, '公告删除', 2420, 4, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:notice:remove',  '#', 'admin', SYSDATE(), '', NULL, ''),
(2425, '公告发布', 2420, 5, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:notice:publish', '#', 'admin', SYSDATE(), '', NULL, ''),
(2426, '公告撤回', 2420, 6, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:notice:revoke',  '#', 'admin', SYSDATE(), '', NULL, ''),

(2430, '数据报表', 2400, 3, 'report', 'kids/report/index', '', '', 1, 0, 'C', '0', '0', 'kids:report:list', 'chart', 'admin', SYSDATE(), '', NULL, ''),
(2431, '报表查询', 2430, 1, '', NULL, '', '', 1, 0, 'F', '0', '0', 'kids:report:query', '#', 'admin', SYSDATE(), '', NULL, '');

-- -------------------------------------------------------------
-- 角色授权
-- 园长 = 超级管理员(role_id=1)，若依内 admin 直接拥有全部权限，无需分配
-- 教师(100)：业务数据可维护，教职工与收费只读，系统管理不可见
-- 家长(101)：工作台 + 我的孩子 + 通知公告（只读）
-- -------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 100, menu_id FROM sys_menu WHERE menu_id IN (
    2000,
    2100, 2110, 2111, 2112, 2113, 2114, 2115, 2116,
    2120, 2121, 2122, 2123, 2124, 2125,
    2130, 2131,
    2200, 2210, 2211, 2212, 2213, 2214,
    2220, 2221, 2222, 2223, 2224,
    2230, 2231, 2232, 2233, 2234, 2235,
    2300, 2310, 2311, 2312, 2313, 2314, 2315,
    2320, 2321, 2322, 2323, 2324, 2325,
    2400, 2410, 2411,
    2420, 2421, 2422, 2423, 2424, 2425, 2426,
    2430, 2431
);

-- 家长(101)：工作台 + 我的孩子（公告在家园互动页内查看，不再单独开放公告菜单）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(101, 2000), (101, 2500), (101, 2510);

-- 园长角色名称对齐
UPDATE sys_role SET role_name = '园长' WHERE role_id = 1;

SELECT '菜单总数' AS item, COUNT(*) AS cnt FROM sys_menu
UNION ALL SELECT '教师授权', COUNT(*) FROM sys_role_menu WHERE role_id = 100
UNION ALL SELECT '家长授权', COUNT(*) FROM sys_role_menu WHERE role_id = 101;
