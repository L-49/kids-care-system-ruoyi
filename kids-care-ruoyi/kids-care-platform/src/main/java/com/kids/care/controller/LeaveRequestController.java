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
import com.kids.care.domain.LeaveRequest;
import com.kids.care.service.ILeaveRequestService;

/**
 * 请假申请 控制器
 *
 * @author kids
 * @date 2026-10-18
 */
@RestController
@RequestMapping("/kids/leave")
public class LeaveRequestController extends BaseController
{
    @Autowired
    private ILeaveRequestService leaveRequestService;

    /** 查询列表 */
    @PreAuthorize("@ss.hasPermi('kids:leave:list')")
    @GetMapping("/list")
    public TableDataInfo list(LeaveRequest leaveRequest)
    {
        startPage();
        List<LeaveRequest> list = leaveRequestService.selectLeaveRequestList(leaveRequest);
        return getDataTable(list);
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('kids:leave:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(leaveRequestService.selectLeaveRequestById(id));
    }

    /** 新增 */
    @PreAuthorize("@ss.hasPermi('kids:leave:add')")
    @Log(title = "请假申请", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LeaveRequest leaveRequest)
    {
        return toAjax(leaveRequestService.insertLeaveRequest(leaveRequest));
    }

    /** 修改 */
    @PreAuthorize("@ss.hasPermi('kids:leave:edit')")
    @Log(title = "请假申请", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LeaveRequest leaveRequest)
    {
        return toAjax(leaveRequestService.updateLeaveRequest(leaveRequest));
    }

    /** 删除 */
    @PreAuthorize("@ss.hasPermi('kids:leave:remove')")
    @Log(title = "请假申请", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(leaveRequestService.deleteLeaveRequestByIds(ids));
    }

    /** 审批：pass=true 通过，false 驳回 */
    @PreAuthorize("@ss.hasPermi('kids:leave:approve')")
    @Log(title = "请假申请", businessType = BusinessType.UPDATE)
    @PutMapping("/approve/{id:\\d+}")
    public AjaxResult approve(@PathVariable Long id, @RequestParam boolean pass)
    {
        return toAjax(leaveRequestService.approve(id, pass));
    }
}
