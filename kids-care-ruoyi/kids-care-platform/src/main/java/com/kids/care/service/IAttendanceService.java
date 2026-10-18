package com.kids.care.service;

import java.util.List;
import com.kids.care.domain.Attendance;

/**
 * 考勤记录 Service 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface IAttendanceService
{
    List<Attendance> selectAttendanceList(Attendance attendance);

    Attendance selectAttendanceById(Long id);

    int insertAttendance(Attendance attendance);

    int updateAttendance(Attendance attendance);

    int deleteAttendanceByIds(Long[] ids);
}
