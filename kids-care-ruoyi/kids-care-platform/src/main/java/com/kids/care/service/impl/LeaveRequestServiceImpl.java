package com.kids.care.service.impl;

import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kids.care.domain.Child;
import com.kids.care.domain.LeaveRequest;
import com.kids.care.mapper.ChildMapper;
import com.kids.care.mapper.LeaveRequestMapper;
import com.kids.care.service.ILeaveRequestService;
import com.kids.care.utils.ScopeUtils;
import com.kids.common.exception.ServiceException;

/**
 * 请假申请 Service 实现
 *
 * <p>状态机：0 待审批 → 1 已通过 / 2 已驳回；审批人取当前登录人，前端不可伪造。</p>
 *
 * @author kids
 * @date 2026-10-18
 */
@Service
public class LeaveRequestServiceImpl implements ILeaveRequestService
{
    @Autowired
    private LeaveRequestMapper leaveRequestMapper;

    @Autowired
    private ChildMapper childMapper;

    @Override
    public List<LeaveRequest> selectLeaveRequestList(LeaveRequest leaveRequest)
    {
        // 家长只能看到自己孩子的请假记录
        Long scope = ScopeUtils.scopeChildId();
        if (scope != null)
        {
            leaveRequest.setChildId(scope);
        }
        List<LeaveRequest> list = leaveRequestMapper.selectLeaveRequestList(leaveRequest);
        for (LeaveRequest item : list)
        {
            if (item.getChildId() != null)
            {
                Child child = childMapper.selectChildById(item.getChildId());
                item.setChildName(child == null ? null : child.getName());
            }
        }
        return list;
    }

    @Override
    public LeaveRequest selectLeaveRequestById(Long id)
    {
        LeaveRequest leave = leaveRequestMapper.selectLeaveRequestById(id);
        if (leave == null)
        {
            throw new ServiceException("请假记录不存在或已删除");
        }
        ScopeUtils.checkChildAccessible(leave.getChildId());
        return leave;
    }

    @Override
    public int insertLeaveRequest(LeaveRequest leaveRequest)
    {
        // 家长提交请假时忽略请求体中的 childId，强制用自己的孩子
        Long scope = ScopeUtils.scopeChildId();
        if (scope != null)
        {
            leaveRequest.setChildId(scope);
        }
        if (leaveRequest.getChildId() == null)
        {
            throw new ServiceException("请选择幼儿");
        }
        if (leaveRequest.getStartDate() == null || leaveRequest.getEndDate() == null)
        {
            throw new ServiceException("请假起止日期不能为空");
        }
        if (leaveRequest.getEndDate().before(leaveRequest.getStartDate()))
        {
            throw new ServiceException("结束日期不能早于开始日期");
        }
        if (leaveRequest.getDays() == null)
        {
            long days = ChronoUnit.DAYS.between(
                    leaveRequest.getStartDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                    leaveRequest.getEndDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()) + 1;
            leaveRequest.setDays((int) days);
        }
        leaveRequest.setStatus(leaveRequest.getStatus() == null ? 0 : leaveRequest.getStatus());
        return leaveRequestMapper.insertLeaveRequest(leaveRequest);
    }

    @Override
    public int updateLeaveRequest(LeaveRequest leaveRequest)
    {
        return leaveRequestMapper.updateLeaveRequest(leaveRequest);
    }

    @Override
    public int deleteLeaveRequestByIds(Long[] ids)
    {
        return leaveRequestMapper.deleteLeaveRequestByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approve(Long id, boolean pass)
    {
        LeaveRequest leave = leaveRequestMapper.selectLeaveRequestById(id);
        if (leave == null)
        {
            throw new ServiceException("请假记录不存在或已删除");
        }
        if (leave.getStatus() != null && leave.getStatus() != 0)
        {
            throw new ServiceException("该请假已审批，无需重复操作");
        }
        int rows = leaveRequestMapper.approveLeave(id, pass ? 1 : 2, ScopeUtils.currentUsername());
        if (rows == 0)
        {
            throw new ServiceException("该请假已审批，无需重复操作");
        }
        return rows;
    }
}
