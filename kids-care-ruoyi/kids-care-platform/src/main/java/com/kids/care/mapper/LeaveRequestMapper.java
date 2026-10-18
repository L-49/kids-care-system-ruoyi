package com.kids.care.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.kids.care.domain.LeaveRequest;

/**
 * LeaveRequest Mapper 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface LeaveRequestMapper
{
    LeaveRequest selectLeaveRequestById(Long id);

    List<LeaveRequest> selectLeaveRequestList(LeaveRequest leaveRequest);

    int insertLeaveRequest(LeaveRequest leaveRequest);

    int updateLeaveRequest(LeaveRequest leaveRequest);

    int deleteLeaveRequestById(Long id);

    int deleteLeaveRequestByIds(Long[] ids);

    /** 审批：仅待审批记录可被更新 */
    int approveLeave(@Param("id") Long id, @Param("status") Integer status, @Param("approver") String approver);
}
