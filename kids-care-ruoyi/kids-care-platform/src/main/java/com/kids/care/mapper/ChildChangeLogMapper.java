package com.kids.care.mapper;

import java.util.List;
import com.kids.care.domain.ChildChangeLog;

/**
 * ChildChangeLog Mapper 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface ChildChangeLogMapper
{
    ChildChangeLog selectChildChangeLogById(Long id);

    List<ChildChangeLog> selectChildChangeLogList(ChildChangeLog childChangeLog);

    int insertChildChangeLog(ChildChangeLog childChangeLog);

    int updateChildChangeLog(ChildChangeLog childChangeLog);

    int deleteChildChangeLogById(Long id);

    int deleteChildChangeLogByIds(Long[] ids);

    /** 按幼儿查变更记录，时间倒序 */
    List<ChildChangeLog> selectChangeLogByChildId(Long childId);
}
