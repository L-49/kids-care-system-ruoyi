package com.kids.care.mapper;

import java.util.List;
import com.kids.care.domain.Course;

/**
 * Course Mapper 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface CourseMapper
{
    Course selectCourseById(Long id);

    List<Course> selectCourseList(Course course);

    int insertCourse(Course course);

    int updateCourse(Course course);

    int deleteCourseById(Long id);

    int deleteCourseByIds(Long[] ids);
}
