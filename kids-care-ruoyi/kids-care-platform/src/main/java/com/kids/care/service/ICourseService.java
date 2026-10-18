package com.kids.care.service;

import java.util.List;
import com.kids.care.domain.Course;

/**
 * 课程 Service 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface ICourseService
{
    List<Course> selectCourseList(Course course);

    Course selectCourseById(Long id);

    int insertCourse(Course course);

    int updateCourse(Course course);

    int deleteCourseByIds(Long[] ids);
}
