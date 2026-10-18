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
import com.kids.care.domain.Teacher;
import com.kids.care.service.ITeacherService;

/**
 * 教职工 控制器
 *
 * @author kids
 * @date 2026-10-18
 */
@RestController
@RequestMapping("/kids/teacher")
public class TeacherController extends BaseController
{
    @Autowired
    private ITeacherService teacherService;

    /** 查询列表 */
    @PreAuthorize("@ss.hasPermi('kids:teacher:list')")
    @GetMapping("/list")
    public TableDataInfo list(Teacher teacher)
    {
        startPage();
        List<Teacher> list = teacherService.selectTeacherList(teacher);
        return getDataTable(list);
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('kids:teacher:query')")
    @GetMapping("/{id:\\d+}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(teacherService.selectTeacherById(id));
    }

    /** 页面概览：总数 / 在职 / 离职 / 担任班主任 */
    @PreAuthorize("@ss.hasPermi('kids:teacher:list')")
    @GetMapping("/overview")
    public AjaxResult overview()
    {
        return success(teacherService.overview());
    }

    /** 在职教职工（下拉框用，不分页） */
    @PreAuthorize("@ss.hasPermi('kids:teacher:list')")
    @GetMapping("/all")
    public AjaxResult all()
    {
        Teacher query = new Teacher();
        query.setStatus(1);
        return success(teacherService.selectTeacherList(query));
    }

    /** 某教职工相关的班级（班主任 / 任课） */
    @PreAuthorize("@ss.hasPermi('kids:teacher:query')")
    @GetMapping("/{id:\\d+}/classes")
    public AjaxResult classes(@PathVariable Long id)
    {
        return success(teacherService.classesOf(id));
    }

    /** 某教职工的任教课程 */
    @PreAuthorize("@ss.hasPermi('kids:teacher:query')")
    @GetMapping("/{id:\\d+}/courses")
    public AjaxResult courses(@PathVariable Long id)
    {
        return success(teacherService.coursesOf(id));
    }

    /** 设为班主任（replace=true 表示替换现任班主任） */
    @PreAuthorize("@ss.hasPermi('kids:teacher:edit')")
    @Log(title = "教职工", businessType = BusinessType.UPDATE)
    @PutMapping("/{id:\\d+}/head-teacher/{clazzId}")
    public AjaxResult setHeadTeacher(@PathVariable Long id, @PathVariable Long clazzId,
                                     @RequestParam(defaultValue = "false") boolean replace)
    {
        teacherService.setHeadTeacher(id, clazzId, replace);
        return success();
    }

    /** 取消班主任 */
    @PreAuthorize("@ss.hasPermi('kids:teacher:edit')")
    @Log(title = "教职工", businessType = BusinessType.UPDATE)
    @DeleteMapping("/{id:\\d+}/head-teacher/{clazzId}")
    public AjaxResult removeHeadTeacher(@PathVariable Long id, @PathVariable Long clazzId)
    {
        teacherService.removeHeadTeacher(id, clazzId);
        return success();
    }

    /** 新增 */
    @PreAuthorize("@ss.hasPermi('kids:teacher:add')")
    @Log(title = "教职工", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Teacher teacher)
    {
        return toAjax(teacherService.insertTeacher(teacher));
    }

    /** 修改 */
    @PreAuthorize("@ss.hasPermi('kids:teacher:edit')")
    @Log(title = "教职工", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Teacher teacher)
    {
        return toAjax(teacherService.updateTeacher(teacher));
    }

    /** 删除 */
    @PreAuthorize("@ss.hasPermi('kids:teacher:remove')")
    @Log(title = "教职工", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(teacherService.deleteTeacherByIds(ids));
    }
}
