package com.kids.care.service;

import java.util.List;
import com.kids.care.domain.ChildChangeLog;

/**
 * 档案变更记录 Service 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface IChildChangeLogService
{
    List<ChildChangeLog> selectChildChangeLogList(ChildChangeLog childChangeLog);

    ChildChangeLog selectChildChangeLogById(Long id);

    int insertChildChangeLog(ChildChangeLog childChangeLog);

    int updateChildChangeLog(ChildChangeLog childChangeLog);

    int deleteChildChangeLogByIds(Long[] ids);
}
