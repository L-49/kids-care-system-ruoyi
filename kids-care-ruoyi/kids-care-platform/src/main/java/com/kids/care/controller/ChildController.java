package com.kids.care.controller;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.kids.common.annotation.Log;
import com.kids.common.core.controller.BaseController;
import com.kids.common.core.domain.AjaxResult;
import com.kids.common.core.page.TableDataInfo;
import com.kids.common.enums.BusinessType;
import com.kids.common.utils.poi.ExcelUtil;
import com.kids.care.domain.Child;
import com.kids.care.service.IChildService;

/**
 * 幼儿档案 控制器
 *
 * @author kids
 * @date 2026-10-18
 */
@RestController
@RequestMapping("/kids/child")
public class ChildController extends BaseController
{
    @Autowired
    private IChildService childService;

    /** 查询列表 */
    @PreAuthorize("@ss.hasPermi('kids:child:list')")
    @GetMapping("/list")
    public TableDataInfo list(Child child)
    {
        startPage();
        List<Child> list = childService.selectChildList(child);
        return getDataTable(list);
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('kids:child:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(childService.selectChildById(id));
    }

    /** 新增 */
    @PreAuthorize("@ss.hasPermi('kids:child:add')")
    @Log(title = "幼儿档案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Child child)
    {
        return toAjax(childService.insertChild(child));
    }

    /** 修改 */
    @PreAuthorize("@ss.hasPermi('kids:child:edit')")
    @Log(title = "幼儿档案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Child child)
    {
        return toAjax(childService.updateChild(child));
    }

    /** 删除 */
    @PreAuthorize("@ss.hasPermi('kids:child:remove')")
    @Log(title = "幼儿档案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(childService.deleteChildByIds(ids));
    }

    /** 调班：{ "targetClazzId": 2 } */
    @PreAuthorize("@ss.hasPermi('kids:child:edit')")
    @Log(title = "幼儿档案", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/transfer")
    public AjaxResult transfer(@PathVariable Long id, @RequestBody Map<String, Long> body)
    {
        childService.transferChild(id, body == null ? null : body.get("targetClazzId"));
        return success();
    }

    /** 档案变更记录 */
    @PreAuthorize("@ss.hasPermi('kids:child:query')")
    @GetMapping("/{id}/history")
    public AjaxResult history(@PathVariable Long id)
    {
        return success(childService.selectChangeLog(id));
    }

    /** 全部在园幼儿（下拉框、调班选择等场景用，不分页） */
    @PreAuthorize("@ss.hasPermi('kids:child:list')")
    @GetMapping("/all")
    public AjaxResult all()
    {
        Child query = new Child();
        query.setStatus(1);
        return success(childService.selectChildList(query));
    }

    /** 有过敏史的幼儿（膳食模块标注用） */
    @PreAuthorize("@ss.hasPermi('kids:child:list')")
    @GetMapping("/allergy")
    public AjaxResult allergy()
    {
        return success(childService.selectAllergyList());
    }

    /** 下载导入模板 */
    @PreAuthorize("@ss.hasPermi('kids:child:import')")
    @PostMapping("/import/template")
    public void importTemplate(HttpServletResponse response)
    {
        new ExcelUtil<Child>(Child.class).importTemplateExcel(response, "幼儿档案");
    }

    /** Excel 批量导入 */
    @PreAuthorize("@ss.hasPermi('kids:child:import')")
    @Log(title = "幼儿档案", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public AjaxResult importData(@RequestParam("file") MultipartFile file)
    {
        return success(childService.importChildren(file));
    }

    /** 按当前筛选条件导出 */
    @PreAuthorize("@ss.hasPermi('kids:child:export')")
    @Log(title = "幼儿档案", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Child child)
    {
        childService.exportChildren(child, response);
    }
}
