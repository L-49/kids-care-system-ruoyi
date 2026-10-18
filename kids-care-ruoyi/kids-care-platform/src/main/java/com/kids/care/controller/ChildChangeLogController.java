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
import com.kids.care.domain.ChildChangeLog;
import com.kids.care.service.IChildChangeLogService;

/**
 * 档案变更记录 控制器
 *
 * @author kids
 * @date 2026-10-18
 */
@RestController
@RequestMapping("/kids/child/history")
public class ChildChangeLogController extends BaseController
{
    @Autowired
    private IChildChangeLogService childChangeLogService;

    /** 查询列表 */
    @PreAuthorize("@ss.hasPermi('kids:child:list')")
    @GetMapping("/list")
    public TableDataInfo list(ChildChangeLog childChangeLog)
    {
        startPage();
        List<ChildChangeLog> list = childChangeLogService.selectChildChangeLogList(childChangeLog);
        return getDataTable(list);
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('kids:child:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(childChangeLogService.selectChildChangeLogById(id));
    }

    /** 新增 */
    @PreAuthorize("@ss.hasPermi('kids:child:add')")
    @Log(title = "档案变更记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ChildChangeLog childChangeLog)
    {
        return toAjax(childChangeLogService.insertChildChangeLog(childChangeLog));
    }

    /** 修改 */
    @PreAuthorize("@ss.hasPermi('kids:child:edit')")
    @Log(title = "档案变更记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ChildChangeLog childChangeLog)
    {
        return toAjax(childChangeLogService.updateChildChangeLog(childChangeLog));
    }

    /** 删除 */
    @PreAuthorize("@ss.hasPermi('kids:child:remove')")
    @Log(title = "档案变更记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(childChangeLogService.deleteChildChangeLogByIds(ids));
    }
}
