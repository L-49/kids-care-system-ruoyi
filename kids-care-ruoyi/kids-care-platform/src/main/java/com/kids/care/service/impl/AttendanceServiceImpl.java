package com.kids.care.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kids.care.domain.Attendance;
import com.kids.care.domain.Child;
import com.kids.care.domain.Clazz;
import com.kids.care.mapper.AttendanceMapper;
import com.kids.care.mapper.ChildMapper;
import com.kids.care.mapper.ClazzMapper;
import com.kids.care.service.IAttendanceService;
import com.kids.care.utils.ScopeUtils;
import com.kids.common.exception.ServiceException;

/**
 * 考勤记录 Service 实现
 *
 * <p>attendance 上有 (child_id, att_date) 唯一键且为软删表：
 * 同日重录会撞唯一键，因此新增先查软删记录并「复活」。</p>
 *
 * @author kids
 * @date 2026-10-18
 */
@Service
public class AttendanceServiceImpl implements IAttendanceService
{
    @Autowired
    private AttendanceMapper attendanceMapper;

    @Autowired
    private ChildMapper childMapper;

    @Autowired
    private ClazzMapper clazzMapper;

    @Override
    public List<Attendance> selectAttendanceList(Attendance attendance)
    {
        // 家长只能看到自己孩子的考勤
        Long scope = ScopeUtils.scopeChildId();
        if (scope != null)
        {
            attendance.setChildId(scope);
        }
        List<Attendance> list = attendanceMapper.selectAttendanceList(attendance);
        Map<Long, Child> childCache = new HashMap<>();
        Map<Long, String> clazzCache = new HashMap<>();
        for (Attendance item : list)
        {
            fillNames(item, childCache, clazzCache);
        }
        return list;
    }

    @Override
    public Attendance selectAttendanceById(Long id)
    {
        Attendance attendance = attendanceMapper.selectAttendanceById(id);
        if (attendance == null)
        {
            throw new ServiceException("考勤记录不存在或已删除");
        }
        ScopeUtils.checkChildAccessible(attendance.getChildId());
        fillNames(attendance, new HashMap<>(), new HashMap<>());
        return attendance;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAttendance(Attendance attendance)
    {
        if (attendance.getChildId() == null)
        {
            throw new ServiceException("请选择幼儿");
        }
        if (attendance.getAttDate() == null)
        {
            throw new ServiceException("考勤日期不能为空");
        }
        // 班级由幼儿档案决定，避免手工选错导致班级统计错乱
        Child child = childMapper.selectChildById(attendance.getChildId());
        if (child == null)
        {
            throw new ServiceException("幼儿档案不存在或已删除");
        }
        attendance.setClazzId(child.getClazzId());
        if (attendanceMapper.countByChildAndDate(attendance.getChildId(), attendance.getAttDate()) > 0)
        {
            throw new ServiceException("该幼儿当天已有考勤记录，请直接修改");
        }
        Long deletedId = attendanceMapper.selectDeletedId(attendance.getChildId(), attendance.getAttDate());
        if (deletedId != null)
        {
            return attendanceMapper.reviveAttendance(deletedId, attendance.getClazzId(),
                    attendance.getStatus(), attendance.getCheckInTime(), attendance.getRemark());
        }
        return attendanceMapper.insertAttendance(attendance);
    }

    @Override
    public int updateAttendance(Attendance attendance)
    {
        // 修改时不接受前端传的班级，仍以档案为准
        if (attendance.getChildId() != null)
        {
            Child child = childMapper.selectChildById(attendance.getChildId());
            if (child != null)
            {
                attendance.setClazzId(child.getClazzId());
            }
        }
        return attendanceMapper.updateAttendance(attendance);
    }

    @Override
    public int deleteAttendanceByIds(Long[] ids)
    {
        return attendanceMapper.deleteAttendanceByIds(ids);
    }

    private void fillNames(Attendance item, Map<Long, Child> childCache, Map<Long, String> clazzCache)
    {
        if (item.getChildId() != null)
        {
            Child child = childCache.get(item.getChildId());
            if (child == null)
            {
                child = childMapper.selectChildById(item.getChildId());
                if (child != null)
                {
                    childCache.put(item.getChildId(), child);
                }
            }
            item.setChildName(child == null ? null : child.getName());
        }
        Long clazzId = item.getClazzId();
        if (clazzId != null)
        {
            if (!clazzCache.containsKey(clazzId))
            {
                Clazz clazz = clazzMapper.selectClazzById(clazzId);
                clazzCache.put(clazzId, clazz == null ? null : clazz.getName());
            }
            item.setClazzName(clazzCache.get(clazzId));
        }
    }
}
