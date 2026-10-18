package com.kids.care.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kids.care.domain.Course;
import com.kids.care.mapper.CourseMapper;
import com.kids.care.service.ICourseService;

/**
 * 课程 Service 实现
 *
 * @author kids
 * @date 2026-10-18
 */
@Service
public class CourseServiceImpl implements ICourseService
{
    @Autowired
    private CourseMapper courseMapper;

    @Override
    public List<Course> selectCourseList(Course course)
    {
        return courseMapper.selectCourseList(course);
    }

    @Override
    public Course selectCourseById(Long id)
    {
        return courseMapper.selectCourseById(id);
    }

    @Override
    public int insertCourse(Course course)
    {
        return courseMapper.insertCourse(course);
    }

    @Override
    public int updateCourse(Course course)
    {
        return courseMapper.updateCourse(course);
    }

    @Override
    public int deleteCourseByIds(Long[] ids)
    {
        return courseMapper.deleteCourseByIds(ids);
    }
}
