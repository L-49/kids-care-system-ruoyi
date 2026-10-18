package com.kids.care.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.kids.care.domain.Child;

/**
 * Child Mapper 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface ChildMapper
{
    Child selectChildById(Long id);

    List<Child> selectChildList(Child child);

    int insertChild(Child child);

    int updateChild(Child child);

    int deleteChildById(Long id);

    int deleteChildByIds(Long[] ids);

    /** 统计班级内在园幼儿数，excludeChildId 用于修改场景排除自己 */
    long countInClazz(@Param("clazzId") Long clazzId, @Param("excludeChildId") Long excludeChildId);

    /** 有过敏史的幼儿（膳食模块标注用） */
    List<Child> selectAllergyList();
}
