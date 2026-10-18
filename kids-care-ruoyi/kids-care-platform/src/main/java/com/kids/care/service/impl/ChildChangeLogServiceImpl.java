package com.kids.care.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kids.care.domain.ChildChangeLog;
import com.kids.care.mapper.ChildChangeLogMapper;
import com.kids.care.service.IChildChangeLogService;

/**
 * 档案变更记录 Service 实现
 *
 * @author kids
 * @date 2026-10-18
 */
@Service
public class ChildChangeLogServiceImpl implements IChildChangeLogService
{
    @Autowired
    private ChildChangeLogMapper childChangeLogMapper;

    @Override
    public List<ChildChangeLog> selectChildChangeLogList(ChildChangeLog childChangeLog)
    {
        return childChangeLogMapper.selectChildChangeLogList(childChangeLog);
    }

    @Override
    public ChildChangeLog selectChildChangeLogById(Long id)
    {
        return childChangeLogMapper.selectChildChangeLogById(id);
    }

    @Override
    public int insertChildChangeLog(ChildChangeLog childChangeLog)
    {
        return childChangeLogMapper.insertChildChangeLog(childChangeLog);
    }

    @Override
    public int updateChildChangeLog(ChildChangeLog childChangeLog)
    {
        return childChangeLogMapper.updateChildChangeLog(childChangeLog);
    }

    @Override
    public int deleteChildChangeLogByIds(Long[] ids)
    {
        return childChangeLogMapper.deleteChildChangeLogByIds(ids);
    }
}
