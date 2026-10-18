package com.kids.care.mapper;

import java.util.List;
import com.kids.care.domain.Teacher;

/**
 * Teacher Mapper 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface TeacherMapper
{
    Teacher selectTeacherById(Long id);

    List<Teacher> selectTeacherList(Teacher teacher);

    int insertTeacher(Teacher teacher);

    int updateTeacher(Teacher teacher);

    int deleteTeacherById(Long id);

    int deleteTeacherByIds(Long[] ids);
}
