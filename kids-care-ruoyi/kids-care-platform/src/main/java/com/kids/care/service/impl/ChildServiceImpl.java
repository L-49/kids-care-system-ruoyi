package com.kids.care.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kids.care.domain.Child;
import com.kids.care.domain.ChildChangeLog;
import com.kids.care.domain.Clazz;
import com.kids.care.mapper.ChildChangeLogMapper;
import com.kids.care.mapper.ChildMapper;
import com.kids.care.mapper.ClazzMapper;
import com.kids.care.service.IChildService;
import com.kids.care.utils.ScopeUtils;
import com.kids.common.exception.ServiceException;
import com.kids.common.utils.StringUtils;
import com.kids.common.utils.poi.ExcelUtil;

/**
 * 幼儿档案 Service 实现。
 *
 * <p>业务规则：</p>
 * <ul>
 *   <li>家长只能读写自己绑定的幼儿（sys_user.child_id），越权直接拒绝；</li>
 *   <li>档案完整度按字段权重实时计算，不落库，避免历史数据与权重变更不一致；</li>
 *   <li>调班与新增都校验目标班级容量，满员不允许调入；</li>
 *   <li>新增、修改、调班都写一条 child_change_log 留痕。</li>
 * </ul>
 *
 * @author kids
 * @date 2026-10-18
 */
@Service
public class ChildServiceImpl implements IChildService
{
    /** 档案字段权重，合计 100；权重按业务风险分配，家长电话、过敏史缺失影响最大 */
    private static final Map<String, Integer> WEIGHTS = new LinkedHashMap<>();

    private static final Map<String, String> LABELS = new HashMap<>();

    static
    {
        WEIGHTS.put("name", 10);
        WEIGHTS.put("gender", 5);
        WEIGHTS.put("birthday", 10);
        WEIGHTS.put("clazz", 10);
        WEIGHTS.put("guardianName", 10);
        WEIGHTS.put("guardianPhone", 15);
        WEIGHTS.put("address", 10);
        WEIGHTS.put("allergy", 10);
        WEIGHTS.put("enrollDate", 10);
        WEIGHTS.put("status", 10);

        LABELS.put("name", "幼儿姓名");
        LABELS.put("gender", "性别");
        LABELS.put("birthday", "出生日期");
        LABELS.put("clazz", "所在班级");
        LABELS.put("guardianName", "家长姓名");
        LABELS.put("guardianPhone", "家长电话");
        LABELS.put("address", "家庭住址");
        LABELS.put("allergy", "过敏史");
        LABELS.put("enrollDate", "入园日期");
        LABELS.put("status", "在园状态");
    }

    @Autowired
    private ChildMapper childMapper;

    @Autowired
    private ClazzMapper clazzMapper;

    @Autowired
    private ChildChangeLogMapper changeLogMapper;

    // ------------------------------------------------------------------
    // 查询
    // ------------------------------------------------------------------

    @Override
    public List<Child> selectChildList(Child query)
    {
        applyScope(query);
        List<Child> list = childMapper.selectChildList(query);
        fillDerived(list);
        return list;
    }

    @Override
    public Child selectChildById(Long id)
    {
        ScopeUtils.checkChildAccessible(id);
        Child child = childMapper.selectChildById(id);
        if (child == null)
        {
            throw new ServiceException("幼儿档案不存在或已删除");
        }
        fillDerived(Collections.singletonList(child));
        return child;
    }

    // ------------------------------------------------------------------
    // 写入
    // ------------------------------------------------------------------

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertChild(Child child)
    {
        if (StringUtils.isEmpty(child.getName()))
        {
            throw new ServiceException("幼儿姓名不能为空");
        }
        checkCapacity(child.getClazzId(), null);
        child.setStatus(child.getStatus() == null ? 1 : child.getStatus());
        int rows = childMapper.insertChild(child);
        writeLog(child, "新增", null, null, "建立幼儿档案", null);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateChild(Child child)
    {
        Child old = childMapper.selectChildById(child.getId());
        if (old == null)
        {
            throw new ServiceException("幼儿档案不存在或已删除");
        }
        ScopeUtils.checkChildAccessible(old.getId());
        if (child.getClazzId() != null && !Objects.equals(child.getClazzId(), old.getClazzId()))
        {
            checkCapacity(child.getClazzId(), child.getId());
        }
        int rows = childMapper.updateChild(child);
        diffAndLog(old, child);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteChildByIds(Long[] ids)
    {
        if (ids == null || ids.length == 0)
        {
            throw new ServiceException("请选择要删除的幼儿");
        }
        for (Long id : ids)
        {
            ScopeUtils.checkChildAccessible(id);
            Child old = childMapper.selectChildById(id);
            if (old != null)
            {
                writeLog(old, "删除", null, null, "删除幼儿档案", null);
            }
        }
        return childMapper.deleteChildByIds(ids);
    }

    // ------------------------------------------------------------------
    // 调班
    // ------------------------------------------------------------------

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferChild(Long childId, Long targetClazzId)
    {
        Child child = childMapper.selectChildById(childId);
        if (child == null)
        {
            throw new ServiceException("幼儿档案不存在或已删除");
        }
        ScopeUtils.checkChildAccessible(childId);
        if (targetClazzId == null)
        {
            throw new ServiceException("请选择目标班级");
        }
        if (Objects.equals(targetClazzId, child.getClazzId()))
        {
            throw new ServiceException("该幼儿已在目标班级中，无需调班");
        }
        checkCapacity(targetClazzId, childId);

        Clazz target = clazzMapper.selectClazzById(targetClazzId);
        String oldClazz = child.getClazzId() == null ? null : clazzName(child.getClazzId());

        Child patch = new Child();
        patch.setId(childId);
        patch.setClazzId(targetClazzId);
        childMapper.updateChild(patch);

        writeLog(child, "调班", "clazzId", oldClazz, target == null ? null : target.getName(), null);
    }

    // ------------------------------------------------------------------
    // 变更记录
    // ------------------------------------------------------------------

    @Override
    public List<ChildChangeLog> selectChangeLog(Long childId)
    {
        ScopeUtils.checkChildAccessible(childId);
        return changeLogMapper.selectChangeLogByChildId(childId);
    }

    // ------------------------------------------------------------------
    // 内部方法
    // ------------------------------------------------------------------

    /** 家长数据范围：只能看自己孩子，园长与教师不加限制 */
    private void applyScope(Child query)
    {
        Long scope = ScopeUtils.scopeChildId();
        if (scope != null)
        {
            query.setId(scope);
        }
    }

    /** 补齐非数据库字段：班级名、完整度、待补全说明 */
    private void fillDerived(List<Child> list)
    {
        if (list == null || list.isEmpty())
        {
            return;
        }
        Map<Long, String> clazzNames = new HashMap<>();
        for (Child c : list)
        {
            if (c.getClazzId() != null && !clazzNames.containsKey(c.getClazzId()))
            {
                clazzNames.put(c.getClazzId(), clazzName(c.getClazzId()));
            }
        }
        for (Child c : list)
        {
            if (c.getClazzId() != null)
            {
                c.setClazzName(clazzNames.get(c.getClazzId()));
            }
            List<String> missing = new ArrayList<>();
            int filled = 0;
            for (Map.Entry<String, Integer> e : WEIGHTS.entrySet())
            {
                if (hasValue(c, e.getKey()))
                {
                    filled += e.getValue();
                }
                else
                {
                    missing.add(LABELS.get(e.getKey()));
                }
            }
            c.setCompleteness(filled);
            c.setMissingItems(missing.isEmpty() ? "" : String.join("、", missing));
        }
    }

    private boolean hasValue(Child c, String key)
    {
        switch (key)
        {
            case "name":
                return StringUtils.isNotEmpty(c.getName());
            case "gender":
                return StringUtils.isNotEmpty(c.getGender());
            case "birthday":
                return c.getBirthday() != null;
            case "clazz":
                return c.getClazzId() != null;
            case "guardianName":
                return StringUtils.isNotEmpty(c.getGuardianName());
            case "guardianPhone":
                return StringUtils.isNotEmpty(c.getGuardianPhone());
            case "address":
                return StringUtils.isNotEmpty(c.getAddress());
            case "allergy":
                return StringUtils.isNotEmpty(c.getAllergy());
            case "enrollDate":
                return c.getEnrollDate() != null;
            case "status":
                return c.getStatus() != null;
            default:
                return false;
        }
    }

    private String clazzName(Long clazzId)
    {
        Clazz clazz = clazzMapper.selectClazzById(clazzId);
        return clazz == null ? null : clazz.getName();
    }

    /** 班级容量校验：在园幼儿数达到额定人数即拒绝，excludeChildId 用于修改场景排除自己 */
    private void checkCapacity(Long clazzId, Long excludeChildId)
    {
        if (clazzId == null)
        {
            return;
        }
        Clazz clazz = clazzMapper.selectClazzById(clazzId);
        if (clazz == null)
        {
            throw new ServiceException("目标班级不存在");
        }
        long inClazz = childMapper.countInClazz(clazzId, excludeChildId);
        Integer capacity = clazz.getCapacity();
        if (capacity != null && inClazz >= capacity)
        {
            throw new ServiceException(String.format("【%s】已满 %d 人，无法调入", clazz.getName(), capacity));
        }
    }

    /** 字段级留痕：只记录真正发生变化的字段 */
    private void diffAndLog(Child old, Child now)
    {
        compareAndLog(old, now, "name", "姓名", old.getName(), now.getName());
        compareAndLog(old, now, "gender", "性别", old.getGender(), now.getGender());
        compareAndLog(old, now, "clazzId", "所在班级",
                old.getClazzId() == null ? null : clazzName(old.getClazzId()),
                now.getClazzId() == null ? null : clazzName(now.getClazzId()));
        compareAndLog(old, now, "guardianName", "家长姓名", old.getGuardianName(), now.getGuardianName());
        compareAndLog(old, now, "guardianPhone", "家长电话", old.getGuardianPhone(), now.getGuardianPhone());
        compareAndLog(old, now, "address", "家庭住址", old.getAddress(), now.getAddress());
        compareAndLog(old, now, "allergy", "过敏史", old.getAllergy(), now.getAllergy());
        compareAndLog(old, now, "status", "在园状态",
                old.getStatus() == null ? null : String.valueOf(old.getStatus()),
                now.getStatus() == null ? null : String.valueOf(now.getStatus()));
    }

    private void compareAndLog(Child old, Child now, String field, String label, String before, String after)
    {
        if (after == null)
        {
            // 未提交该字段（null 表示不修改），不参与比较
            return;
        }
        if (!after.equals(before))
        {
            writeLog(old, "修改", field, before, after, null);
        }
    }

    private void writeLog(Child child, String changeType, String fieldName, String oldValue,
                          String newValue, String batchId)
    {
        ChildChangeLog log = new ChildChangeLog();
        log.setChildId(child.getId());
        log.setChildName(child.getName());
        log.setChangeType(changeType);
        log.setFieldName(fieldName);
        log.setFieldLabel(fieldName == null ? null : labelOf(fieldName));
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setOperator(ScopeUtils.currentUsername());
        log.setBatchId(batchId);
        changeLogMapper.insertChildChangeLog(log);
    }

    @Override
    public List<Child> selectAllergyList()
    {
        List<Child> list = childMapper.selectAllergyList();
        fillDerived(list);
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importChildren(MultipartFile file)
    {
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("请选择要导入的 Excel 文件");
        }
        List<Child> rows;
        try (InputStream in = file.getInputStream())
        {
            // 若依模板首行为标题，从第 2 行开始解析
            rows = new ExcelUtil<Child>(Child.class).importExcel(in, 1);
        }
        catch (Exception e)
        {
            throw new ServiceException("Excel 解析失败：" + e.getMessage());
        }
        if (rows == null || rows.isEmpty())
        {
            throw new ServiceException("Excel 中没有可导入的数据");
        }

        int success = 0;
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++)
        {
            Child row = rows.get(i);
            int lineNo = i + 2;
            try
            {
                if (StringUtils.isEmpty(row.getName()))
                {
                    throw new ServiceException("姓名不能为空");
                }
                // 模板里填的是班级名称，这里换成班级ID并校验容量
                if (StringUtils.isNotEmpty(row.getClazzName()))
                {
                    Clazz clazz = clazzMapper.selectByName(row.getClazzName().trim());
                    if (clazz == null)
                    {
                        throw new ServiceException("班级【" + row.getClazzName() + "】不存在，请先在班级管理中创建");
                    }
                    row.setClazzId(clazz.getId());
                    checkCapacity(row.getClazzId(), null);
                }
                row.setStatus(row.getStatus() == null ? 1 : row.getStatus());
                childMapper.insertChild(row);
                writeLog(row, "导入", null, null, "Excel 批量导入", null);
                success++;
            }
            catch (Exception e)
            {
                errors.add("第 " + lineNo + " 行：" + e.getMessage());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", success);
        result.put("fail", errors.size());
        // 失败原因过多时只回传前 20 条，避免响应体过大
        result.put("errors", errors.size() > 20 ? new ArrayList<>(errors.subList(0, 20)) : errors);
        return result;
    }

    @Override
    public void exportChildren(Child query, HttpServletResponse response)
    {
        List<Child> list = selectChildList(query == null ? new Child() : query);
        new ExcelUtil<Child>(Child.class).exportExcel(response, list, "幼儿档案");
    }

    private String labelOf(String field)
    {
        switch (field)
        {
            case "name":
                return "幼儿姓名";
            case "gender":
                return "性别";
            case "clazzId":
                return "所在班级";
            case "guardianName":
                return "家长姓名";
            case "guardianPhone":
                return "家长电话";
            case "address":
                return "家庭住址";
            case "allergy":
                return "过敏史";
            case "status":
                return "在园状态";
            default:
                return field;
        }
    }
}
