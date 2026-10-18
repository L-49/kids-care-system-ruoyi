-- =============================================================
-- 幼儿园管理系统 · 迁移到若依框架 数据迁移脚本 v1
-- 依赖：ry_20260417.sql（若依 20 张系统表已导入 kids_care）
-- 幂等：可重复执行
-- =============================================================
USE kids_care;

SET NAMES utf8mb4;

-- -------------------------------------------------------------
-- 1. 部门：若依演示部门 -> 幼儿园组织架构
-- -------------------------------------------------------------
DELETE FROM sys_role_dept WHERE dept_id IN (101, 102, 103, 104, 105, 106, 107, 108, 109);
DELETE FROM sys_dept WHERE dept_id IN (101, 102, 103, 104, 105, 106, 107, 108, 109);
DELETE FROM sys_dept WHERE dept_id IN (200, 201, 202, 203, 204);

UPDATE sys_dept
SET dept_name = '阳光幼儿园', leader = '张园长', phone = '13800000001', order_num = 0
WHERE dept_id = 100;

INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time, update_by, update_time)
VALUES
(200, 100, '0,100', '小班', 1, '陈静', '13800000010', '', '0', '0', 'admin', SYSDATE(), '', NULL),
(201, 100, '0,100', '中班', 2, '李静', '13800000002', '', '0', '0', 'admin', SYSDATE(), '', NULL),
(202, 100, '0,100', '大班', 3, '陈鹏', '13800000011', '', '0', '0', 'admin', SYSDATE(), '', NULL),
(203, 100, '0,100', '托班', 4, '刘敏', '13800000012', '', '0', '0', 'admin', SYSDATE(), '', NULL),
(204, 100, '0,100', '后勤部', 5, '周平', '13800000013', '', '0', '0', 'admin', SYSDATE(), '', NULL);

-- -------------------------------------------------------------
-- 2. 角色：园长沿用若依超级管理员 admin，新增教师、家长
-- -------------------------------------------------------------
DELETE FROM sys_role_menu WHERE role_id IN (100, 101);
DELETE FROM sys_user_role WHERE role_id IN (100, 101);
DELETE FROM sys_role WHERE role_id IN (100, 101);

INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, update_by, update_time, remark)
VALUES
(100, '教师', 'teacher', 3, '1', 1, 1, '0', '0', 'admin', SYSDATE(), '', NULL, '教师：可维护本班幼儿、考勤、食谱与库存等业务数据'),
(101, '家长', 'parent', 4, '1', 1, 1, '0', '0', 'admin', SYSDATE(), '', NULL, '家长：仅可查看自己孩子的档案、考勤、缴费与全园公告');

-- -------------------------------------------------------------
-- 3. 用户：园长 / 教师 / 家长（初始口令均为 123456）
-- -------------------------------------------------------------
ALTER TABLE sys_user ADD COLUMN child_id BIGINT NULL COMMENT '家长关联的幼儿ID（kids 业务扩展字段）' 
  AFTER dept_id;

UPDATE sys_user
SET dept_id = 100, nick_name = '园长', user_name = 'admin',
    phonenumber = '13800000001', email = 'admin@kids.com',
    password = '$2a$10$zTbRHRWgQZjVH23oMIA2RuKzq8wl38wVeyNXm0Adlw7MlzUYZP3ta',
    remark = '园长账号'
WHERE user_id = 1;

UPDATE sys_user
SET dept_id = 201, nick_name = '李老师', user_name = 'teacher',
    phonenumber = '13800000002', email = 'teacher@kids.com',
    password = '$2a$10$zTbRHRWgQZjVH23oMIA2RuKzq8wl38wVeyNXm0Adlw7MlzUYZP3ta',
    remark = '教师账号'
WHERE user_id = 2;

DELETE FROM sys_user WHERE user_id = 3;
DELETE FROM sys_user_role WHERE user_id = 3;

INSERT INTO sys_user (user_id, dept_id, child_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, login_ip, login_date, pwd_update_date, create_by, create_time, update_by, update_time, remark)
VALUES (3, 100, 1, 'parent', '张建国', '00', 'parent@kids.com', '13800000003', '1', '',
        '$2a$10$zTbRHRWgQZjVH23oMIA2RuKzq8wl38wVeyNXm0Adlw7MlzUYZP3ta', '0', '0', '127.0.0.1', SYSDATE(), SYSDATE(), 'admin', SYSDATE(), '', NULL,
        '家长账号，关联幼儿「王梓涵」(child_id=1)');

DELETE FROM sys_user_role WHERE user_id IN (1, 2, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);
INSERT INTO sys_user_role (user_id, role_id) VALUES (2, 100);
INSERT INTO sys_user_role (user_id, role_id) VALUES (3, 101);

-- 清理若依演示用户
DELETE FROM sys_user WHERE user_id > 3 AND user_id < 100;
DELETE FROM sys_user_role WHERE user_id > 3 AND user_id < 100;

-- -------------------------------------------------------------
-- 4. 字典：原 sys_dict（5 类 19 条）-> sys_dict_type + sys_dict_data
-- -------------------------------------------------------------
DELETE FROM sys_dict_data WHERE dict_type IN ('att_status', 'fee_type', 'food_category', 'meal_type', 'notice_type', 'leave_type');
DELETE FROM sys_dict_type WHERE dict_type IN ('att_status', 'fee_type', 'food_category', 'meal_type', 'notice_type', 'leave_type');

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark)
VALUES
(100, '考勤状态', 'att_status', '0', 'admin', SYSDATE(), '', NULL, '考勤状态列表'),
(101, '收费类型', 'fee_type', '0', 'admin', SYSDATE(), '', NULL, '保教费/餐费/校车费'),
(102, '食材分类', 'food_category', '0', 'admin', SYSDATE(), '', NULL, '食材库存分类'),
(103, '食谱餐次', 'meal_type', '0', 'admin', SYSDATE(), '', NULL, '每周食谱餐次'),
(104, '公告类型', 'notice_type', '0', 'admin', SYSDATE(), '', NULL, '通知公告类型'),
(105, '请假类型', 'leave_type', '0', 'admin', SYSDATE(), '', NULL, '请假申请类型');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES
(1, '出勤', '出勤', 'att_status', '', 'success', 'Y', '0', 'admin', SYSDATE(), '', NULL, '考勤状态'),
(2, '迟到', '迟到', 'att_status', '', 'warning', 'N', '0', 'admin', SYSDATE(), '', NULL, '考勤状态'),
(3, '请假', '请假', 'att_status', '', 'info', 'N', '0', 'admin', SYSDATE(), '', NULL, '考勤状态'),
(4, '缺勤', '缺勤', 'att_status', '', 'danger', 'N', '0', 'admin', SYSDATE(), '', NULL, '考勤状态'),
(1, '保教费', '保教费', 'fee_type', '', 'primary', 'Y', '0', 'admin', SYSDATE(), '', NULL, '收费类型'),
(2, '餐费', '餐费', 'fee_type', '', 'success', 'N', '0', 'admin', SYSDATE(), '', NULL, '收费类型'),
(3, '校车费', '校车费', 'fee_type', '', 'warning', 'N', '0', 'admin', SYSDATE(), '', NULL, '收费类型'),
(1, '主食', '主食', 'food_category', '', 'primary', 'N', '0', 'admin', SYSDATE(), '', NULL, '食材分类'),
(2, '蔬菜', '蔬菜', 'food_category', '', 'success', 'N', '0', 'admin', SYSDATE(), '', NULL, '食材分类'),
(3, '肉类', '肉类', 'food_category', '', 'danger', 'N', '0', 'admin', SYSDATE(), '', NULL, '食材分类'),
(4, '水果', '水果', 'food_category', '', 'warning', 'N', '0', 'admin', SYSDATE(), '', NULL, '食材分类'),
(5, '其他', '其他', 'food_category', '', 'info', 'N', '0', 'admin', SYSDATE(), '', NULL, '食材分类'),
(1, '早餐', '早餐', 'meal_type', '', 'primary', 'Y', '0', 'admin', SYSDATE(), '', NULL, '食谱餐次'),
(2, '午餐', '午餐', 'meal_type', '', 'success', 'N', '0', 'admin', SYSDATE(), '', NULL, '食谱餐次'),
(3, '午点', '午点', 'meal_type', '', 'warning', 'N', '0', 'admin', SYSDATE(), '', NULL, '食谱餐次'),
(1, '通知', '通知', 'notice_type', '', 'primary', 'Y', '0', 'admin', SYSDATE(), '', NULL, '公告类型'),
(2, '活动', '活动', 'notice_type', '', 'success', 'N', '0', 'admin', SYSDATE(), '', NULL, '公告类型'),
(3, '食谱', '食谱', 'notice_type', '', 'warning', 'N', '0', 'admin', SYSDATE(), '', NULL, '公告类型'),
(4, '放假', '放假', 'notice_type', '', 'danger', 'N', '0', 'admin', SYSDATE(), '', NULL, '公告类型'),
(5, '园务', '园务', 'notice_type', '', 'info', 'N', '0', 'admin', SYSDATE(), '', NULL, '公告类型'),
(6, '班级', '班级', 'notice_type', '', 'primary', 'N', '0', 'admin', SYSDATE(), '', NULL, '公告类型'),
(7, '健康', '健康', 'notice_type', '', 'success', 'N', '0', 'admin', SYSDATE(), '', NULL, '公告类型'),
(1, '事假', '事假', 'leave_type', '', 'info', 'Y', '0', 'admin', SYSDATE(), '', NULL, '请假类型'),
(2, '病假', '病假', 'leave_type', '', 'warning', 'N', '0', 'admin', SYSDATE(), '', NULL, '请假类型');

-- -------------------------------------------------------------
-- 5. 业务表补若依审计字段（create_by / update_by / update_time）
-- -------------------------------------------------------------
DROP PROCEDURE IF EXISTS kids_add_col;
DELIMITER $$
CREATE PROCEDURE kids_add_col(IN tbl VARCHAR(64), IN col VARCHAR(64), IN defn VARCHAR(200))
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = tbl) THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = tbl AND column_name = col) THEN
            SET @ddl = CONCAT('ALTER TABLE `', tbl, '` ADD COLUMN `', col, '` ', defn);
            PREPARE s FROM @ddl;
            EXECUTE s;
            DEALLOCATE PREPARE s;
        END IF;
    END IF;
END$$
DELIMITER ;

CALL kids_add_col('child',             'create_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''创建者''');
CALL kids_add_col('child',             'update_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''更新者''');
CALL kids_add_col('child',             'update_time', 'DATETIME COMMENT ''更新时间''');
CALL kids_add_col('clazz',             'create_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''创建者''');
CALL kids_add_col('clazz',             'update_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''更新者''');
CALL kids_add_col('clazz',             'update_time', 'DATETIME COMMENT ''更新时间''');
CALL kids_add_col('teacher',           'create_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''创建者''');
CALL kids_add_col('teacher',           'update_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''更新者''');
CALL kids_add_col('teacher',           'update_time', 'DATETIME COMMENT ''更新时间''');
CALL kids_add_col('attendance',        'create_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''创建者''');
CALL kids_add_col('attendance',        'update_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''更新者''');
CALL kids_add_col('attendance',        'update_time', 'DATETIME COMMENT ''更新时间''');
CALL kids_add_col('course',            'create_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''创建者''');
CALL kids_add_col('course',            'update_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''更新者''');
CALL kids_add_col('course',            'update_time', 'DATETIME COMMENT ''更新时间''');
CALL kids_add_col('fee',               'create_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''创建者''');
CALL kids_add_col('fee',               'update_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''更新者''');
CALL kids_add_col('fee',               'update_time', 'DATETIME COMMENT ''更新时间''');
CALL kids_add_col('food_stock',        'create_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''创建者''');
CALL kids_add_col('food_stock',        'update_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''更新者''');
CALL kids_add_col('food_stock',        'update_time', 'DATETIME COMMENT ''更新时间''');
CALL kids_add_col('leave_request',     'create_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''创建者''');
CALL kids_add_col('leave_request',     'update_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''更新者''');
CALL kids_add_col('leave_request',     'update_time', 'DATETIME COMMENT ''更新时间''');
CALL kids_add_col('menu_plan',         'create_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''创建者''');
CALL kids_add_col('menu_plan',         'update_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''更新者''');
CALL kids_add_col('menu_plan',         'update_time', 'DATETIME COMMENT ''更新时间''');
CALL kids_add_col('notice',            'create_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''创建者''');
CALL kids_add_col('notice',            'update_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''更新者''');
CALL kids_add_col('notice',            'update_time', 'DATETIME COMMENT ''更新时间''');
CALL kids_add_col('stock_record',      'create_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''创建者''');
CALL kids_add_col('stock_record',      'update_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''更新者''');
CALL kids_add_col('stock_record',      'update_time', 'DATETIME COMMENT ''更新时间''');
CALL kids_add_col('child_change_log',  'create_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''创建者''');
CALL kids_add_col('child_change_log',  'update_by', 'VARCHAR(64) DEFAULT '''' COMMENT ''更新者''');
CALL kids_add_col('child_change_log',  'update_time', 'DATETIME COMMENT ''更新时间''');
-- child_change_log 走逻辑删除（Mapper 的 select/update 均带 deleted = 0），列缺失会导致 Unknown column 'deleted'
CALL kids_add_col('child_change_log',  'deleted', 'TINYINT NOT NULL DEFAULT 0 COMMENT ''删除标志(0存在 1删除)''');

DROP PROCEDURE IF EXISTS kids_add_col;

-- -------------------------------------------------------------
-- 6. 旧系统表：自研 sys_dict / sys_log 已由若依字典表与操作日志表替代
-- -------------------------------------------------------------
DROP TABLE IF EXISTS sys_dict;
DROP TABLE IF EXISTS sys_log;

-- -------------------------------------------------------------
-- 7. 校验
-- -------------------------------------------------------------
SELECT '用户' AS item, COUNT(*) AS cnt FROM sys_user WHERE del_flag = '0'
UNION ALL SELECT '角色', COUNT(*) FROM sys_role WHERE del_flag = '0'
UNION ALL SELECT '部门', COUNT(*) FROM sys_dept WHERE del_flag = '0'
UNION ALL SELECT '字典类型', COUNT(*) FROM sys_dict_type
UNION ALL SELECT '字典数据', COUNT(*) FROM sys_dict_data
UNION ALL SELECT '菜单', COUNT(*) FROM sys_menu;
