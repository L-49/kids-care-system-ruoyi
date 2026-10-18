package com.kids.care.controller;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.kids.care.mapper.StatMapper;
import com.kids.care.utils.ScopeUtils;
import com.kids.common.core.domain.AjaxResult;

/**
 * 数据看板与统计报表 控制器
 *
 * <p>只做聚合展示，不含明细操作；家长登录时隐藏涉及他人金额的待缴明细。</p>
 *
 * @author kids
 * @date 2026-10-18
 */
@RestController
@RequestMapping("/kids/stat")
public class StatController
{
    /** 出勤 */
    private static final String PRESENT = "出勤";

    /** 迟到 */
    private static final String LATE = "迟到";

    /** 请假 */
    private static final String LEAVE = "请假";

    /** 缺勤 */
    private static final String ABSENT = "缺勤";

    @Autowired
    private StatMapper statMapper;

    /** 看板汇总数据 */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/overview")
    public AjaxResult overview()
    {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("childCount", statMapper.countChild());
        data.put("teacherCount", statMapper.countTeacher());
        data.put("clazzCount", statMapper.countClazz());
        data.put("noticeCount", statMapper.countNotice());
        data.put("pendingLeave", statMapper.countPendingLeave());
        data.put("unpaidCount", statMapper.countUnpaid());
        data.put("unpaidAmount", statMapper.sumUnpaidAmount());
        data.put("monthIncome", statMapper.sumPaidByMonth(YearMonth.now().toString()));

        // 今日考勤：补齐 4 种状态，便于前端直接渲染
        Map<String, Object> today = new LinkedHashMap<>();
        today.put(PRESENT, 0L);
        today.put(LATE, 0L);
        today.put(LEAVE, 0L);
        today.put(ABSENT, 0L);
        for (Map<String, Object> row : statMapper.attendanceToday())
        {
            today.put(String.valueOf(row.get("status")), row.get("num"));
        }
        data.put("attendanceToday", today);
        data.put("attendanceTrend", statMapper.attendanceTrend(PRESENT));
        data.put("clazzDistribution", statMapper.clazzDistribution());
        // 待缴明细含幼儿姓名与金额，家长端不下发
        data.put("unpaidFeeTop", ScopeUtils.isParent() ? List.of() : statMapper.unpaidFeeTop());
        return AjaxResult.success(data);
    }

    /** 近 7 天出勤趋势（单独接口，便于看板局部刷新） */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/trend")
    public AjaxResult trend()
    {
        return AjaxResult.success(statMapper.attendanceTrend(PRESENT));
    }

    /** 统计报表：收费趋势、班级出勤率、库存预警 */
    @PreAuthorize("@ss.hasPermi('kids:report:query')")
    @GetMapping("/report")
    public AjaxResult report()
    {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("feeTrend", statMapper.feeTrend());
        data.put("attendanceByClazz", statMapper.attendanceByClazz(PRESENT));
        data.put("attendanceToday", statMapper.attendanceToday());
        data.put("clazzDistribution", statMapper.clazzDistribution());
        data.put("childCount", statMapper.countChild());
        data.put("todayTotal", statMapper.countTodayAttendance());
        data.put("stockWarn", statMapper.countStockWarn());
        data.put("logCount", statMapper.countLog());
        data.put("unpaidAmount", statMapper.sumUnpaidAmount());
        data.put("unpaidCount", statMapper.countUnpaid());
        data.put("monthIncome", statMapper.sumPaidByMonth(YearMonth.now().toString()));
        return AjaxResult.success(data);
    }
}
