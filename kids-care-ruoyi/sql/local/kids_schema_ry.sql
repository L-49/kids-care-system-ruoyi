-- =============================================================
-- 幼儿园管理系统 · 业务表建表脚本（若依版交付补齐）
-- 说明：组长交付包原先缺少业务表 DDL，空库直接跑 ry/migrate 脚本会报表不存在。
--       本脚本只建 12 张业务表（不含若依系统表），已存在的表不会被覆盖（IF NOT EXISTS）。
-- 执行顺序：ry_20260417.sql -> kids_schema.sql -> kids_migrate_v1.sql -> kids_menu_v2.sql -> kids_fix_v3.sql
-- =============================================================
CREATE DATABASE IF NOT EXISTS `kids_care_ry` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE kids_care_ry;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `child` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '幼儿姓名',
  `gender` varchar(4) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '男',
  `birthday` date DEFAULT NULL COMMENT '出生日期',
  `clazz_id` bigint DEFAULT NULL COMMENT '所在班级',
  `guardian_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '家长姓名',
  `guardian_phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '家长电话',
  `address` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '家庭住址',
  `allergy` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '过敏史/禁忌',
  `enroll_date` date DEFAULT NULL COMMENT '入园日期',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1在园 0离园',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_child_clazz` (`clazz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='幼儿档案';

CREATE TABLE IF NOT EXISTS `clazz` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '班级名称',
  `age_range` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '适龄段，如 3-4岁',
  `room` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '教室',
  `capacity` int NOT NULL DEFAULT '25' COMMENT '额定人数',
  `head_teacher_id` bigint DEFAULT NULL COMMENT '班主任ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  `open_date` date DEFAULT NULL COMMENT '开班日期',
  `remark` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '班级特色',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='班级';

CREATE TABLE IF NOT EXISTS `teacher` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '姓名',
  `gender` varchar(4) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '女' COMMENT '性别',
  `phone` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系电话',
  `role` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位：园长/教师/保育员/保健医',
  `entry_date` date DEFAULT NULL COMMENT '入职日期',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1在职 0离职',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  `id_card` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '身份证号',
  `education` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '学历',
  `title` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '职称',
  `address` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '家庭住址',
  `emergency_contact` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '紧急联系人',
  `emergency_phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '紧急联系电话',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教职工';

CREATE TABLE IF NOT EXISTS `course` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程名称',
  `clazz_id` bigint NOT NULL COMMENT '班级',
  `teacher_id` bigint DEFAULT NULL COMMENT '授课教师',
  `course_date` date NOT NULL COMMENT '上课日期',
  `start_time` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '开始时间 HH:mm',
  `end_time` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '结束时间 HH:mm',
  `content` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '活动内容',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1正常 0取消',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_course_date` (`course_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课程活动';

CREATE TABLE IF NOT EXISTS `attendance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `child_id` bigint NOT NULL COMMENT '幼儿ID',
  `clazz_id` bigint DEFAULT NULL COMMENT '班级ID（冗余，便于按班统计）',
  `att_date` date NOT NULL COMMENT '考勤日期',
  `status` varchar(10) COLLATE utf8mb4_general_ci NOT NULL COMMENT '出勤/迟到/请假/缺勤',
  `check_in_time` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '入园时间 HH:mm',
  `remark` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_att` (`child_id`,`att_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考勤记录';

CREATE TABLE IF NOT EXISTS `fee` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `child_id` bigint NOT NULL COMMENT '幼儿ID',
  `fee_type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '保教费/餐费/校车费/其他',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `fee_month` varchar(7) COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属月份 yyyy-MM',
  `pay_status` tinyint NOT NULL DEFAULT '0' COMMENT '0未缴 1已缴',
  `pay_time` datetime DEFAULT NULL COMMENT '缴费时间',
  `remark` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_fee_month` (`fee_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='收费记录';

CREATE TABLE IF NOT EXISTS `leave_request` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `child_id` bigint NOT NULL COMMENT '幼儿ID',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `days` int NOT NULL DEFAULT '1' COMMENT '请假天数',
  `reason` varchar(300) COLLATE utf8mb4_general_ci NOT NULL COMMENT '请假事由',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0待审批 1已通过 2已驳回',
  `approver` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='请假申请';

CREATE TABLE IF NOT EXISTS `notice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '标题',
  `content` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '内容',
  `type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '园务' COMMENT '类型：园务/班级/健康/活动',
  `publisher` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '发布人',
  `status` tinyint NOT NULL DEFAULT '2' COMMENT '0草稿 1定时待发 2已发布 3已撤回',
  `target_type` varchar(10) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'all' COMMENT '可见范围：all全园 class指定班级',
  `target_id` bigint DEFAULT NULL COMMENT '指定班级ID（target_type=class 时有效）',
  `publish_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_notice_status` (`status`,`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='通知公告';

CREATE TABLE IF NOT EXISTS `menu_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `menu_date` date NOT NULL COMMENT '日期',
  `meal_type` varchar(10) COLLATE utf8mb4_general_ci NOT NULL COMMENT '餐次：早餐/午餐/午点',
  `dishes` varchar(300) COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜品',
  `note` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注/营养说明',
  `publisher` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '发布人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_menu` (`menu_date`,`meal_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='每周食谱';

CREATE TABLE IF NOT EXISTS `food_stock` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '食材名称',
  `category` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类：主食/蔬菜/肉类/水果/其他',
  `quantity` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '库存数量',
  `unit` varchar(10) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'kg' COMMENT '单位',
  `warn_line` decimal(10,2) NOT NULL DEFAULT '5.00' COMMENT '预警线',
  `supplier` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '供应商',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='食材库存';

CREATE TABLE IF NOT EXISTS `stock_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `food_id` bigint NOT NULL COMMENT '食材ID',
  `food_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '食材名称（冗余，食材改名不影响历史）',
  `type` varchar(10) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'IN入库 / OUT出库',
  `quantity` decimal(10,2) NOT NULL COMMENT '本次数量',
  `before_qty` decimal(10,2) NOT NULL COMMENT '操作前库存',
  `after_qty` decimal(10,2) NOT NULL COMMENT '操作后库存',
  `unit` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '单位',
  `operator` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作人',
  `remark` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_record_food` (`food_id`),
  KEY `idx_record_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='食材出入库流水';

CREATE TABLE IF NOT EXISTS `child_change_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `child_id` bigint NOT NULL COMMENT '幼儿ID',
  `child_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '留档时的姓名（档案被删也能追溯）',
  `change_type` varchar(10) COLLATE utf8mb4_general_ci NOT NULL COMMENT '新增/修改/导入',
  `field_name` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字段英文名，整条记录时为空',
  `field_label` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字段中文名',
  `old_value` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '变更前',
  `new_value` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '变更后',
  `operator` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作人',
  `batch_id` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '批量导入批次号，一次导入共用一个',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ccl_child` (`child_id`,`create_time`),
  KEY `idx_ccl_batch` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='幼儿档案变更留痕';
