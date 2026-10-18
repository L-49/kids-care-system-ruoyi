package com.kids.care.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.kids.care.domain.Attendance;

/**
 * Attendance Mapper 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface AttendanceMapper
{
    Attendance selectAttendanceById(Long id);

    List<Attendance> selectAttendanceList(Attendance attendance);

    int insertAttendance(Attendance attendance);

    int updateAttendance(Attendance attendance);

    int deleteAttendanceById(Long id);

    int deleteAttendanceByIds(Long[] ids);

    /** 查已软删的同幼儿同日记录ID */
    Long selectDeletedId(@Param("childId") Long childId, @Param("attDate") Date attDate);

    /** 统计未删除的同幼儿同日记录数 */
    int countByChildAndDate(@Param("childId") Long childId, @Param("attDate") Date attDate);

    /** 复活软删记录 */
    int reviveAttendance(@Param("id") Long id, @Param("clazzId") Long clazzId,
                         @Param("status") String status, @Param("checkInTime") String checkInTime,
                         @Param("remark") String remark);
}
