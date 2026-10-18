package com.kids.care.service;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import com.kids.care.domain.Child;
import com.kids.care.domain.ChildChangeLog;

/**
 * 幼儿档案 Service 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface IChildService
{
    List<Child> selectChildList(Child child);

    Child selectChildById(Long id);

    int insertChild(Child child);

    int updateChild(Child child);

    int deleteChildByIds(Long[] ids);

    /** 调班：校验目标班级容量后更新班级并留痕 */
    void transferChild(Long childId, Long targetClazzId);

    /** 档案变更记录 */
    List<ChildChangeLog> selectChangeLog(Long childId);

    /** 有过敏史的幼儿（膳食模块标注用） */
    List<Child> selectAllergyList();

    /** Excel 批量导入：逐行校验，成功的入库，失败的返回行号与原因 */
    Map<String, Object> importChildren(MultipartFile file);

    /** 按当前筛选条件导出 Excel */
    void exportChildren(Child query, HttpServletResponse response);
}
