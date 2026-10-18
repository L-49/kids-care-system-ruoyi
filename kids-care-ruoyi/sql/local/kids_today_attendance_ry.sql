-- =============================================================
-- 幼儿园管理系统 · 当天考勤演示数据
-- 用途：演示/验收前执行一次，让「工作台 · 今日考勤」与「数据报表」有当日数据。
-- 幂等：当天已有考勤记录的幼儿会被跳过，可重复执行。
-- =============================================================
USE kids_care_ry;
SET NAMES utf8mb4;

INSERT INTO attendance (child_id, clazz_id, att_date, status, check_in_time, create_time, deleted)
SELECT
    c.id,
    c.clazz_id,
    CURDATE(),
    CASE
        WHEN c.id % 17 = 0 THEN '缺勤'
        WHEN c.id % 13 = 0 THEN '请假'
        WHEN c.id % 9  = 0 THEN '迟到'
        ELSE '出勤'
    END,
    CASE
        WHEN c.id % 17 = 0 OR c.id % 13 = 0 THEN NULL
        WHEN c.id % 9  = 0 THEN '08:36'
        ELSE '08:05'
    END,
    SYSDATE(),
    0
FROM child c
WHERE c.deleted = 0
  AND c.status = 1
  AND NOT EXISTS (
      SELECT 1 FROM attendance a
      WHERE a.child_id = c.id AND a.att_date = CURDATE()
  );

SELECT '今日考勤' AS item, COUNT(*) AS cnt FROM attendance WHERE deleted = 0 AND att_date = CURDATE()
UNION ALL
SELECT CONCAT('今日-', status), COUNT(*) FROM attendance WHERE deleted = 0 AND att_date = CURDATE() GROUP BY status;
