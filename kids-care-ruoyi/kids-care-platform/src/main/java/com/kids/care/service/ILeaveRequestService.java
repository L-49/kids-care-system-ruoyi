package com.kids.care.service;

import java.util.List;
import com.kids.care.domain.LeaveRequest;

/**
 * 请假申请 Service 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface ILeaveRequestService
{
    List<LeaveRequest> selectLeaveRequestList(LeaveRequest leaveRequest);

    LeaveRequest selectLeaveRequestById(Long id);

    int insertLeaveRequest(LeaveRequest leaveRequest);

    int updateLeaveRequest(LeaveRequest leaveRequest);

    int deleteLeaveRequestByIds(Long[] ids);

    /** 审批：pass=true 通过，false 驳回；审批人取当前登录人 */
    int approve(Long id, boolean pass);
}
