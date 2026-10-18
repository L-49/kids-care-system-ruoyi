package com.kids.care.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.kids.care.domain.Clazz;

/**
 * Clazz Mapper 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface ClazzMapper
{
    Clazz selectClazzById(Long id);

    List<Clazz> selectClazzList(Clazz clazz);

    int insertClazz(Clazz clazz);

    int updateClazz(Clazz clazz);

    int deleteClazzById(Long id);

    int deleteClazzByIds(Long[] ids);

    /** 按名称精确查（唯一校验用） */
    Clazz selectByName(String name);

    /** 设置/取消班主任，teacherId 为 null 表示取消 */
    int updateHeadTeacher(@Param("clazzId") Long clazzId, @Param("teacherId") Long teacherId);

    /** 某教职工担任班主任的班级 */
    List<Clazz> selectByHeadTeacherId(@Param("teacherId") Long teacherId);
}
