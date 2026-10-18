package com.kids.care.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kids.care.domain.Attendance;
import com.kids.care.domain.Child;
import com.kids.care.domain.Clazz;
import com.kids.care.domain.Fee;
import com.kids.care.domain.LeaveRequest;
import com.kids.care.mapper.AttendanceMapper;
import com.kids.care.mapper.ChildMapper;
import com.kids.care.mapper.FeeMapper;
import com.kids.care.mapper.LeaveRequestMapper;
import com.kids.care.service.IClazzService;
import com.kids.care.service.ILeaveRequestService;
import com.kids.care.service.INoticeService;
import com.kids.care.utils.ScopeUtils;
import com.kids.common.core.controller.BaseController;
import com.kids.common.core.domain.AjaxResult;
import com.kids.common.exception.ServiceException;

/**
 * 家长端「我的孩子」聚合接口。
 *
 * <p>家长账号没有幼儿档案、考勤、收费的列表权限，避免他们通过通用列表接口翻到别人家孩子；
 * 需要的数据统一由本接口按 sys_user.child_id 精确取回，一次返回首页所需全部内容。</p>
 *
 * @author kids
 */
@RestController
@RequestMapping("/kids/my-child")
public class MyChildController extends BaseController
{
    private static final int ATTENDANCE_LIMIT = 14;

    private static final int LEAVE_LIMIT = 5;

    @Autowired
    private ChildMapper childMapper;

    @Autowired
    private IClazzService clazzService;

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Autowired
    private FeeMapper feeMapper;

    @Autowired
    private LeaveRequestMapper leaveRequestMapper;

    @Autowired
    private INoticeService noticeService;

    @Autowired
    private ILeaveRequestService leaveRequestService;

    /** 孩子档案 + 班级 + 近期考勤 + 待缴费用 + 请假记录 + 可见公告 */
    @PreAuthorize("@ss.hasPermi('kids:mychild:view')")
    @GetMapping("/info")
    public AjaxResult info()
    {
        Long childId = ScopeUtils.requireParentChildId();
        Child child = childMapper.selectChildById(childId);
        if (child == null)
        {
            throw new ServiceException("孩子档案不存在，请联系园方");
        }
        Clazz clazz = child.getClazzId() == null ? null : clazzService.selectClazzById(child.getClazzId());

        Attendance aq = new Attendance();
        aq.setChildId(childId);
        List<Attendance> attendances = attendanceMapper.selectAttendanceList(aq);

        Fee fq = new Fee();
        fq.setChildId(childId);
        fq.setPayStatus(0);
        List<Fee> unpaidFees = feeMapper.selectFeeList(fq);

        LeaveRequest lq = new LeaveRequest();
        lq.setChildId(childId);
        List<LeaveRequest> leaves = leaveRequestMapper.selectLeaveRequestList(lq);

        Map<String, Object> data = new HashMap<>();
        data.put("child", child);
        data.put("clazz", clazz);
        data.put("attendances", head(attendances, ATTENDANCE_LIMIT));
        data.put("unpaidFees", unpaidFees);
        data.put("leaves", head(leaves, LEAVE_LIMIT));
        data.put("notices", noticeService.selectVisibleNotices());
        return success(data);
    }

    /** 仅供家长打卡查看的考勤明细 */
    @PreAuthorize("@ss.hasPermi('kids:mychild:view')")
    @GetMapping("/attendance")
    public AjaxResult attendance()
    {
        Long childId = ScopeUtils.requireParentChildId();
        Attendance query = new Attendance();
        query.setChildId(childId);
        return success(attendanceMapper.selectAttendanceList(query));
    }

    /** 家长为绑定的幼儿提交请假申请（孩子 ID 由登录账号决定，忽略请求体） */
    @PreAuthorize("@ss.hasPermi('kids:mychild:view')")
    @PostMapping("/leave")
    public AjaxResult applyLeave(@RequestBody LeaveRequest body)
    {
        LeaveRequest request = new LeaveRequest();
        request.setStartDate(body.getStartDate());
        request.setEndDate(body.getEndDate());
        request.setReason(body.getReason());
        leaveRequestService.insertLeaveRequest(request);
        return success();
    }

    private <T> List<T> head(List<T> list, int limit)
    {
        if (list == null)
        {
            return java.util.Collections.emptyList();
        }
        return list.size() <= limit ? list : list.subList(0, limit);
    }
}
