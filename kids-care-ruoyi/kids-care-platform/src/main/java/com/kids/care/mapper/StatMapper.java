package com.kids.care.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 数据看板统计 Mapper（多表聚合查询）
 *
 * <p>看板与报表以「汇总展示」为主，直接写聚合 SQL 比逐表走 Service 更直观，
 * 因此这里不引入实体，统一返回 Map 由 Controller 组装。</p>
 *
 * @author kids
 */
@Mapper
public interface StatMapper
{
    /** 在园幼儿数 */
    @Select("select count(*) from child where deleted = 0 and status = 1")
    Long countChild();

    /** 在职教职工数 */
    @Select("select count(*) from teacher where deleted = 0 and status = 1")
    Long countTeacher();

    /** 班级数 */
    @Select("select count(*) from clazz where deleted = 0")
    Long countClazz();

    /** 公告总数 */
    @Select("select count(*) from notice where deleted = 0")
    Long countNotice();

    /** 待审批请假 */
    @Select("select count(*) from leave_request where deleted = 0 and status = 0")
    Long countPendingLeave();

    /** 未缴费笔数 */
    @Select("select count(*) from fee where deleted = 0 and pay_status = 0")
    Long countUnpaid();

    /** 未缴金额合计 */
    @Select("select ifnull(sum(amount), 0) from fee where deleted = 0 and pay_status = 0")
    BigDecimal sumUnpaidAmount();

    /** 指定月份实收金额 */
    @Select("select ifnull(sum(amount), 0) from fee where deleted = 0 and pay_status = 1 and fee_month = #{month}")
    BigDecimal sumPaidByMonth(@Param("month") String month);

    /** 今日考勤分布：status -> 人数 */
    @Select("select status, count(*) as num from attendance "
            + "where deleted = 0 and att_date = curdate() group by status")
    List<Map<String, Object>> attendanceToday();

    /** 近 7 天出勤趋势（幼儿园周末不开园，仅统计周一至周五） */
    @Select("select date_format(att_date, '%m-%d') as day, "
            + "sum(case when status = #{present} then 1 else 0 end) as present, count(*) as total "
            + "from attendance where deleted = 0 and att_date >= date_sub(curdate(), interval 6 day) "
            + "and dayofweek(att_date) not in (1, 7) "
            + "group by att_date order by att_date")
    List<Map<String, Object>> attendanceTrend(@Param("present") String present);

    /** 各班在园人数分布 */
    @Select("select c.name as name, count(ch.id) as value from clazz c "
            + "left join child ch on ch.clazz_id = c.id and ch.deleted = 0 and ch.status = 1 "
            + "where c.deleted = 0 group by c.id, c.name order by c.id")
    List<Map<String, Object>> clazzDistribution();

    /** 待缴费明细（最近 5 条） */
    @Select("select ch.name as childName, f.fee_type as feeType, f.amount as amount, "
            + "f.fee_month as feeMonth from fee f join child ch on ch.id = f.child_id "
            + "where f.deleted = 0 and f.pay_status = 0 order by f.fee_month desc, f.id desc limit 5")
    List<Map<String, Object>> unpaidFeeTop();

    /** 近 6 个月收费趋势 */
    @Select("select fee_month as month, sum(case when pay_status = 1 then amount else 0 end) as paid, "
            + "sum(amount) as total from fee where deleted = 0 "
            + "group by fee_month order by fee_month desc limit 6")
    List<Map<String, Object>> feeTrend();

    /** 各班出勤汇总 */
    @Select("select c.name as clazzName, sum(case when a.status = #{present} then 1 else 0 end) as present, "
            + "count(a.id) as total from attendance a join clazz c on c.id = a.clazz_id "
            + "where a.deleted = 0 group by c.id, c.name order by c.id")
    List<Map<String, Object>> attendanceByClazz(@Param("present") String present);

    /** 今日考勤记录总数 */
    @Select("select count(*) from attendance where deleted = 0 and att_date = curdate()")
    Long countTodayAttendance();

    /** 食材低于预警线条数 */
    @Select("select count(*) from food_stock where deleted = 0 and quantity <= warn_line")
    Long countStockWarn();

    /** 操作日志总数（若依自带 sys_oper_log） */
    @Select("select count(*) from sys_oper_log")
    Long countLog();
}
