package com.kids.care.service;

import java.util.List;
import java.util.Map;
import com.kids.care.domain.Child;
import com.kids.care.domain.Clazz;

/**
 * 班级 Service 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface IClazzService
{
    List<Clazz> selectClazzList(Clazz clazz);

    Clazz selectClazzById(Long id);

    int insertClazz(Clazz clazz);

    int updateClazz(Clazz clazz);

    int deleteClazzByIds(Long[] ids);

    /** 班级概览：班级数、在园幼儿、满员率、超额班级数 */
    Map<String, Object> overview();

    /** 班级幼儿名单（含离园） */
    List<Child> childrenOf(Long clazzId);
}
