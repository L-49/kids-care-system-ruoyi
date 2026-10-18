package com.kids.care.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.kids.common.annotation.Log;
import com.kids.common.core.controller.BaseController;
import com.kids.common.core.domain.AjaxResult;
import com.kids.common.core.page.TableDataInfo;
import com.kids.common.enums.BusinessType;
import com.kids.care.domain.Attendance;
import com.kids.care.service.IAttendanceService;

/**
 * 考勤记录 控制器
 *
 * @author kids
 * @date 2026-10-18
 */
@RestController
@RequestMapping("/kids/attendance")
public class AttendanceController extends BaseController
{
    @Autowired
    private IAttendanceService attendanceService;

    /** 查询列表 */
    @PreAuthorize("@ss.hasPermi('kids:attendance:list')")
    @GetMapping("/list")
    public TableDataInfo list(Attendance attendance)
    {
        startPage();
        List<Attendance> list = attendanceService.selectAttendanceList(attendance);
        return getDataTable(list);
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('kids:attendance:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(attendanceService.selectAttendanceById(id));
    }

    /** 新增 */
    @PreAuthorize("@ss.hasPermi('kids:attendance:add')")
    @Log(title = "考勤记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Attendance attendance)
    {
        return toAjax(attendanceService.insertAttendance(attendance));
    }

    /** 修改 */
    @PreAuthorize("@ss.hasPermi('kids:attendance:edit')")
    @Log(title = "考勤记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Attendance attendance)
    {
        return toAjax(attendanceService.updateAttendance(attendance));
    }

    /** 删除 */
    @PreAuthorize("@ss.hasPermi('kids:attendance:remove')")
    @Log(title = "考勤记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(attendanceService.deleteAttendanceByIds(ids));
    }
}
