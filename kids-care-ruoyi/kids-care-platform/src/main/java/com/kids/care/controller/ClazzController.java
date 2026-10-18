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
import com.kids.care.domain.Clazz;
import com.kids.care.service.IClazzService;

/**
 * 班级 控制器
 *
 * @author kids
 * @date 2026-10-18
 */
@RestController
@RequestMapping("/kids/clazz")
public class ClazzController extends BaseController
{
    @Autowired
    private IClazzService clazzService;

    /** 查询列表 */
    @PreAuthorize("@ss.hasPermi('kids:clazz:list')")
    @GetMapping("/list")
    public TableDataInfo list(Clazz clazz)
    {
        startPage();
        List<Clazz> list = clazzService.selectClazzList(clazz);
        return getDataTable(list);
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('kids:clazz:query')")
    @GetMapping("/{id:\\d+}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(clazzService.selectClazzById(id));
    }

    /** 班级概览：班级数 / 在园幼儿 / 满员率 / 超额班级 */
    @PreAuthorize("@ss.hasPermi('kids:clazz:list')")
    @GetMapping("/overview")
    public AjaxResult overview()
    {
        return success(clazzService.overview());
    }

    /** 班级幼儿名单（含离园） */
    @PreAuthorize("@ss.hasPermi('kids:clazz:query')")
    @GetMapping("/{id}/children")
    public AjaxResult children(@PathVariable Long id)
    {
        return success(clazzService.childrenOf(id));
    }

    /** 新增 */
    @PreAuthorize("@ss.hasPermi('kids:clazz:add')")
    @Log(title = "班级", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Clazz clazz)
    {
        return toAjax(clazzService.insertClazz(clazz));
    }

    /** 修改 */
    @PreAuthorize("@ss.hasPermi('kids:clazz:edit')")
    @Log(title = "班级", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Clazz clazz)
    {
        return toAjax(clazzService.updateClazz(clazz));
    }

    /** 删除 */
    @PreAuthorize("@ss.hasPermi('kids:clazz:remove')")
    @Log(title = "班级", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(clazzService.deleteClazzByIds(ids));
    }
}
