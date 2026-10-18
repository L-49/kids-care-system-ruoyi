package com.kids.care.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kids.care.domain.Child;
import com.kids.care.domain.Clazz;
import com.kids.care.domain.Teacher;
import com.kids.care.mapper.ChildMapper;
import com.kids.care.mapper.ClazzMapper;
import com.kids.care.mapper.TeacherMapper;
import com.kids.care.service.IClazzService;
import com.kids.care.utils.ScopeUtils;
import com.kids.common.exception.ServiceException;
import com.kids.common.utils.StringUtils;

/**
 * 班级 Service 实现。
 *
 * <p>满员口径：在园幼儿数（child.status=1 且未删除）与额定人数比较，
 * 等于额定为「已满」，大于额定为「超额」。超额只可能由历史数据或事后调小容量造成，
 * 因此列表要能看到而不是直接拦住。</p>
 *
 * @author kids
 * @date 2026-10-18
 */
@Service
public class ClazzServiceImpl implements IClazzService
{
    @Autowired
    private ClazzMapper clazzMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private ChildMapper childMapper;

    @Override
    public List<Clazz> selectClazzList(Clazz query)
    {
        List<Clazz> list = clazzMapper.selectClazzList(query);
        fillDerived(list);
        return list;
    }

    @Override
    public Clazz selectClazzById(Long id)
    {
        Clazz clazz = clazzMapper.selectClazzById(id);
        if (clazz == null)
        {
            throw new ServiceException("班级不存在或已删除");
        }
        fillDerived(java.util.Collections.singletonList(clazz));
        return clazz;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertClazz(Clazz clazz)
    {
        if (StringUtils.isEmpty(clazz.getName()))
        {
            throw new ServiceException("班级名称不能为空");
        }
        if (clazz.getCapacity() == null || clazz.getCapacity() <= 0)
        {
            throw new ServiceException("额定人数必须大于 0");
        }
        Clazz exist = clazzMapper.selectByName(clazz.getName());
        if (exist != null)
        {
            throw new ServiceException("班级名称【" + clazz.getName() + "】已存在");
        }
        return clazzMapper.insertClazz(clazz);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateClazz(Clazz clazz)
    {
        Clazz old = clazzMapper.selectClazzById(clazz.getId());
        if (old == null)
        {
            throw new ServiceException("班级不存在或已删除");
        }
        if (StringUtils.isNotEmpty(clazz.getName()))
        {
            Clazz exist = clazzMapper.selectByName(clazz.getName());
            if (exist != null && !exist.getId().equals(clazz.getId()))
            {
                throw new ServiceException("班级名称【" + clazz.getName() + "】已存在");
            }
        }
        // 容量调小到低于在园人数时给出明确提示，但不阻断（历史数据处理）
        if (clazz.getCapacity() != null)
        {
            long in = childMapper.countInClazz(clazz.getId(), null);
            if (clazz.getCapacity() < in)
            {
                throw new ServiceException(String.format(
                        "该班在园 %d 人，额定人数不能小于在园人数", in));
            }
        }
        return clazzMapper.updateClazz(clazz);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteClazzByIds(Long[] ids)
    {
        if (ids == null || ids.length == 0)
        {
            throw new ServiceException("请选择要删除的班级");
        }
        for (Long id : ids)
        {
            long in = childMapper.countInClazz(id, null);
            Clazz clazz = clazzMapper.selectClazzById(id);
            if (in > 0)
            {
                throw new ServiceException(String.format("【%s】还有 %d 名在园幼儿，请先调班后再删除",
                        clazz == null ? id : clazz.getName(), in));
            }
        }
        return clazzMapper.deleteClazzByIds(ids);
    }

    /**
     * 班级概览：班级总数、在园幼儿、整体满员率、超额班级数。
     */
    @Override
    public Map<String, Object> overview()
    {
        List<Clazz> all = clazzMapper.selectClazzList(new Clazz());
        fillDerived(all);
        long total = all.size();
        long children = 0;
        long capacity = 0;
        long over = 0;
        for (Clazz c : all)
        {
            children += c.getChildCount() == null ? 0 : c.getChildCount();
            capacity += c.getCapacity() == null ? 0 : c.getCapacity();
            if ("超额".equals(c.getFullStatus()))
            {
                over++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("clazzCount", total);
        result.put("childCount", children);
        result.put("capacity", capacity);
        result.put("rate", capacity == 0 ? 0 : Math.round(children * 100.0 / capacity));
        result.put("overCount", over);
        return result;
    }

    /**
     * 某班幼儿名单（含离园），家长调用时仅返回自己孩子。
     */
    @Override
    public List<Child> childrenOf(Long clazzId)
    {
        Child query = new Child();
        query.setClazzId(clazzId);
        Long scope = ScopeUtils.scopeChildId();
        if (scope != null)
        {
            query.setId(scope);
        }
        List<Child> list = childMapper.selectChildList(query);
        Map<Long, String> names = new HashMap<>();
        Clazz clazz = clazzMapper.selectClazzById(clazzId);
        String name = clazz == null ? null : clazz.getName();
        for (Child c : list)
        {
            c.setClazzName(name);
        }
        return list;
    }

    // ------------------------------------------------------------------

    private void fillDerived(List<Clazz> list)
    {
        if (list == null || list.isEmpty())
        {
            return;
        }
        List<Clazz> result = new ArrayList<>();
        for (Clazz c : list)
        {
            long in = childMapper.countInClazz(c.getId(), null);
            c.setChildCount(in);
            Integer cap = c.getCapacity();
            if (cap != null)
            {
                if (in > cap)
                {
                    c.setFullStatus("超额");
                }
                else if (in == cap)
                {
                    c.setFullStatus("已满");
                }
                else
                {
                    c.setFullStatus("正常");
                }
            }
            if (c.getHeadTeacherId() != null)
            {
                Teacher t = teacherMapper.selectTeacherById(c.getHeadTeacherId());
                c.setHeadTeacherName(t == null ? null : t.getName());
            }
            result.add(c);
        }
    }
}
