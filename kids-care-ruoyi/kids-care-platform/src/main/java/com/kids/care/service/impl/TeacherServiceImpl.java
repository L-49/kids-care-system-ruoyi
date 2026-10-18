package com.kids.care.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kids.care.domain.Clazz;
import com.kids.care.domain.Course;
import com.kids.care.domain.Teacher;
import com.kids.care.mapper.ChildMapper;
import com.kids.care.mapper.ClazzMapper;
import com.kids.care.mapper.CourseMapper;
import com.kids.care.mapper.TeacherMapper;
import com.kids.care.service.ITeacherService;
import com.kids.common.exception.ServiceException;

/**
 * 教职工 Service 实现
 *
 * <p>业务规则：</p>
 * <ul>
 *   <li>班主任与「任课教师」是两种关系：前者落在 clazz.head_teacher_id，后者由课程记录推导；</li>
 *   <li>一个班级同时只能有一位班主任，替换需要显式确认（replace=true）；</li>
 *   <li>离职教职工不能设为班主任，担任班主任的教职工不允许删除。</li>
 * </ul>
 *
 * @author kids
 * @date 2026-10-18
 */
@Service
public class TeacherServiceImpl implements ITeacherService
{
    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private ClazzMapper clazzMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private ChildMapper childMapper;

    @Override
    public List<Teacher> selectTeacherList(Teacher teacher)
    {
        return teacherMapper.selectTeacherList(teacher);
    }

    @Override
    public Teacher selectTeacherById(Long id)
    {
        return teacherMapper.selectTeacherById(id);
    }

    @Override
    public int insertTeacher(Teacher teacher)
    {
        return teacherMapper.insertTeacher(teacher);
    }

    @Override
    public int updateTeacher(Teacher teacher)
    {
        return teacherMapper.updateTeacher(teacher);
    }

    @Override
    public int deleteTeacherByIds(Long[] ids)
    {
        for (Long id : ids)
        {
            List<Clazz> headClazzes = clazzMapper.selectByHeadTeacherId(id);
            if (headClazzes != null && !headClazzes.isEmpty())
            {
                Teacher teacher = teacherMapper.selectTeacherById(id);
                StringBuilder names = new StringBuilder();
                for (Clazz c : headClazzes)
                {
                    names.append(names.length() == 0 ? "" : "、").append(c.getName());
                }
                throw new ServiceException(String.format("【%s】是班级【%s】的班主任，请先调整班主任后再删除",
                        teacher == null ? id : teacher.getName(), names));
            }
        }
        return teacherMapper.deleteTeacherByIds(ids);
    }

    @Override
    public Map<String, Object> overview()
    {
        List<Teacher> all = teacherMapper.selectTeacherList(new Teacher());
        long total = all.size();
        long active = 0;
        for (Teacher t : all)
        {
            if (t.getStatus() != null && t.getStatus() == 1)
            {
                active++;
            }
        }
        Set<Long> headIds = new HashSet<>();
        for (Clazz c : clazzMapper.selectClazzList(new Clazz()))
        {
            if (c.getHeadTeacherId() != null)
            {
                headIds.add(c.getHeadTeacherId());
            }
        }
        long headCount = 0;
        for (Teacher t : all)
        {
            if (headIds.contains(t.getId()))
            {
                headCount++;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("active", active);
        result.put("inactive", total - active);
        result.put("headTeachers", headCount);
        return result;
    }

    @Override
    public List<Map<String, Object>> classesOf(Long teacherId)
    {
        Teacher teacher = teacherMapper.selectTeacherById(teacherId);
        if (teacher == null)
        {
            throw new ServiceException("教职工不存在或已删除");
        }

        List<Clazz> headClasses = clazzMapper.selectByHeadTeacherId(teacherId);
        Set<Long> headIds = new HashSet<>();
        for (Clazz c : headClasses)
        {
            headIds.add(c.getId());
        }

        // 任课班级：由课程记录推导，排除已作为班主任的班级，避免重复
        Course query = new Course();
        query.setTeacherId(teacherId);
        List<Course> courses = courseMapper.selectCourseList(query);
        Set<Long> taughtIds = new HashSet<>();
        for (Course c : courses)
        {
            if (c.getClazzId() != null && !headIds.contains(c.getClazzId()))
            {
                taughtIds.add(c.getClazzId());
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Clazz c : headClasses)
        {
            rows.add(rowOfClazz(c, "担任班主任", true));
        }
        for (Long clazzId : taughtIds)
        {
            Clazz c = clazzMapper.selectClazzById(clazzId);
            if (c != null)
            {
                rows.add(rowOfClazz(c, "任课教师", false));
            }
        }
        return rows;
    }

    private Map<String, Object> rowOfClazz(Clazz c, String source, boolean isHead)
    {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("clazzId", c.getId());
        row.put("clazzName", c.getName());
        row.put("source", source);
        row.put("isHead", isHead);
        row.put("room", c.getRoom());
        row.put("capacity", c.getCapacity());
        row.put("childCount", childMapper.countInClazz(c.getId(), null));
        return row;
    }

    @Override
    public List<Course> coursesOf(Long teacherId)
    {
        Teacher teacher = teacherMapper.selectTeacherById(teacherId);
        if (teacher == null)
        {
            throw new ServiceException("教职工不存在或已删除");
        }
        Course query = new Course();
        query.setTeacherId(teacherId);
        List<Course> list = courseMapper.selectCourseList(query);
        Map<Long, String> clazzNames = new HashMap<>();
        for (Course c : list)
        {
            c.setTeacherName(teacher.getName());
            if (c.getClazzId() != null)
            {
                if (!clazzNames.containsKey(c.getClazzId()))
                {
                    Clazz clazz = clazzMapper.selectClazzById(c.getClazzId());
                    clazzNames.put(c.getClazzId(), clazz == null ? null : clazz.getName());
                }
                c.setClazzName(clazzNames.get(c.getClazzId()));
            }
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setHeadTeacher(Long teacherId, Long clazzId, boolean replace)
    {
        Teacher teacher = teacherMapper.selectTeacherById(teacherId);
        if (teacher == null)
        {
            throw new ServiceException("教职工不存在或已删除");
        }
        if (teacher.getStatus() == null || teacher.getStatus() != 1)
        {
            throw new ServiceException("离职教职工不能设为班主任");
        }
        Clazz clazz = clazzMapper.selectClazzById(clazzId);
        if (clazz == null)
        {
            throw new ServiceException("班级不存在或已删除");
        }
        if (Objects.equals(teacherId, clazz.getHeadTeacherId()))
        {
            // 已是该班班主任，幂等返回
            return;
        }
        if (clazz.getHeadTeacherId() != null && !replace)
        {
            Teacher current = teacherMapper.selectTeacherById(clazz.getHeadTeacherId());
            throw new ServiceException(String.format("班级【%s】的班主任已是【%s】，如需替换请确认",
                    clazz.getName(), current == null ? "其他教职工" : current.getName()));
        }
        clazzMapper.updateHeadTeacher(clazzId, teacherId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeHeadTeacher(Long teacherId, Long clazzId)
    {
        Clazz clazz = clazzMapper.selectClazzById(clazzId);
        if (clazz == null)
        {
            throw new ServiceException("班级不存在或已删除");
        }
        if (!Objects.equals(teacherId, clazz.getHeadTeacherId()))
        {
            throw new ServiceException("该班级的班主任不是该教职工，无法取消");
        }
        clazzMapper.updateHeadTeacher(clazzId, null);
    }
}
