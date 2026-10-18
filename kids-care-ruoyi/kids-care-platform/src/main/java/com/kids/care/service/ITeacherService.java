package com.kids.care.service;

import java.util.List;
import java.util.Map;
import com.kids.care.domain.Course;
import com.kids.care.domain.Teacher;

/**
 * 教职工 Service 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface ITeacherService
{
    List<Teacher> selectTeacherList(Teacher teacher);

    Teacher selectTeacherById(Long id);

    int insertTeacher(Teacher teacher);

    int updateTeacher(Teacher teacher);

    int deleteTeacherByIds(Long[] ids);

    /** 页面概览：教职工总数 / 在职 / 离职 / 担任班主任人数 */
    Map<String, Object> overview();

    /** 某教职工相关的班级（担任班主任 / 任课教师） */
    List<Map<String, Object>> classesOf(Long teacherId);

    /** 某教职工的任教课程 */
    List<Course> coursesOf(Long teacherId);

    /** 设为班主任，replace=true 时替换现任班主任 */
    void setHeadTeacher(Long teacherId, Long clazzId, boolean replace);

    /** 取消班主任 */
    void removeHeadTeacher(Long teacherId, Long clazzId);
}
