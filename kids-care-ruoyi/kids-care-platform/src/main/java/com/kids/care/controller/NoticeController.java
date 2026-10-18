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
import com.kids.care.domain.Notice;
import com.kids.care.service.INoticeService;

/**
 * 通知公告 控制器
 *
 * @author kids
 * @date 2026-10-18
 */
@RestController
@RequestMapping("/kids/notice")
public class NoticeController extends BaseController
{
    @Autowired
    private INoticeService noticeService;

    /** 查询列表 */
    @PreAuthorize("@ss.hasPermi('kids:notice:list')")
    @GetMapping("/list")
    public TableDataInfo list(Notice notice)
    {
        startPage();
        List<Notice> list = noticeService.selectNoticeList(notice);
        return getDataTable(list);
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('kids:notice:query')")
    @GetMapping("/{id:\\d+}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(noticeService.selectNoticeById(id));
    }

    /** 新增 */
    @PreAuthorize("@ss.hasPermi('kids:notice:add')")
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Notice notice)
    {
        return toAjax(noticeService.insertNotice(notice));
    }

    /** 修改 */
    @PreAuthorize("@ss.hasPermi('kids:notice:edit')")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Notice notice)
    {
        return toAjax(noticeService.updateNotice(notice));
    }

    /** 删除 */
    @PreAuthorize("@ss.hasPermi('kids:notice:remove')")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(noticeService.deleteNoticeByIds(ids));
    }

    /** 立即发布 */
    @PreAuthorize("@ss.hasPermi('kids:notice:publish')")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PostMapping("/{id:\\d+}/publish")
    public AjaxResult publish(@PathVariable Long id)
    {
        noticeService.publish(id);
        return success();
    }

    /** 撤回 */
    @PreAuthorize("@ss.hasPermi('kids:notice:revoke')")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PostMapping("/{id:\\d+}/revoke")
    public AjaxResult revoke(@PathVariable Long id)
    {
        noticeService.revoke(id);
        return success();
    }

    /** 家长端：已发布且可见范围内的公告 */
    @PreAuthorize("@ss.hasPermi('kids:notice:list')")
    @GetMapping("/all")
    public AjaxResult all()
    {
        return success(noticeService.selectVisibleNotices());
    }
}
