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
import com.kids.care.domain.MenuPlan;
import com.kids.care.service.IMenuPlanService;

/**
 * 每周食谱 控制器
 *
 * @author kids
 * @date 2026-10-18
 */
@RestController
@RequestMapping("/kids/menu")
public class MenuPlanController extends BaseController
{
    @Autowired
    private IMenuPlanService menuPlanService;

    /** 查询列表 */
    @PreAuthorize("@ss.hasPermi('kids:menu:list')")
    @GetMapping("/list")
    public TableDataInfo list(MenuPlan menuPlan)
    {
        startPage();
        List<MenuPlan> list = menuPlanService.selectMenuPlanList(menuPlan);
        return getDataTable(list);
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('kids:menu:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(menuPlanService.selectMenuPlanById(id));
    }

    /** 周视图：weekStart 为周一，如 2026-10-12 */
    @PreAuthorize("@ss.hasPermi('kids:menu:list')")
    @GetMapping("/week")
    public AjaxResult week(@RequestParam String weekStart)
    {
        return success(menuPlanService.listWeek(weekStart));
    }

    /** 一键复制上周食谱到本周，返回复制条数 */
    @PreAuthorize("@ss.hasPermi('kids:menu:copy')")
    @Log(title = "每周食谱", businessType = BusinessType.INSERT)
    @PostMapping("/copy-last-week")
    public AjaxResult copyLastWeek(@RequestParam String weekStart)
    {
        int copied = menuPlanService.copyFromLastWeek(weekStart);
        return success(copied);
    }

    /** 新增 */
    @PreAuthorize("@ss.hasPermi('kids:menu:add')")
    @Log(title = "每周食谱", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MenuPlan menuPlan)
    {
        return toAjax(menuPlanService.insertMenuPlan(menuPlan));
    }

    /** 修改 */
    @PreAuthorize("@ss.hasPermi('kids:menu:edit')")
    @Log(title = "每周食谱", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MenuPlan menuPlan)
    {
        return toAjax(menuPlanService.updateMenuPlan(menuPlan));
    }

    /** 删除 */
    @PreAuthorize("@ss.hasPermi('kids:menu:remove')")
    @Log(title = "每周食谱", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(menuPlanService.deleteMenuPlanByIds(ids));
    }
}
