package com.kids.care.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kids.care.domain.Child;
import com.kids.care.domain.Fee;
import com.kids.care.mapper.ChildMapper;
import com.kids.care.mapper.FeeMapper;
import com.kids.care.service.IFeeService;
import com.kids.care.utils.ScopeUtils;
import com.kids.common.exception.ServiceException;

/**
 * 收费记录 Service 实现
 *
 * @author kids
 * @date 2026-10-18
 */
@Service
public class FeeServiceImpl implements IFeeService
{
    @Autowired
    private FeeMapper feeMapper;

    @Autowired
    private ChildMapper childMapper;

    @Override
    public List<Fee> selectFeeList(Fee fee)
    {
        // 家长只能看到自己孩子的账单
        Long scope = ScopeUtils.scopeChildId();
        if (scope != null)
        {
            fee.setChildId(scope);
        }
        List<Fee> list = feeMapper.selectFeeList(fee);
        for (Fee f : list)
        {
            if (f.getChildId() != null)
            {
                Child child = childMapper.selectChildById(f.getChildId());
                f.setChildName(child == null ? null : child.getName());
            }
        }
        return list;
    }

    @Override
    public Fee selectFeeById(Long id)
    {
        Fee fee = feeMapper.selectFeeById(id);
        if (fee == null)
        {
            throw new ServiceException("收费记录不存在或已删除");
        }
        ScopeUtils.checkChildAccessible(fee.getChildId());
        return fee;
    }

    @Override
    public int insertFee(Fee fee)
    {
        if (fee.getChildId() == null)
        {
            throw new ServiceException("请选择幼儿");
        }
        if (fee.getAmount() == null)
        {
            throw new ServiceException("金额不能为空");
        }
        fee.setPayStatus(fee.getPayStatus() == null ? 0 : fee.getPayStatus());
        return feeMapper.insertFee(fee);
    }

    @Override
    public int updateFee(Fee fee)
    {
        return feeMapper.updateFee(fee);
    }

    @Override
    public int deleteFeeByIds(Long[] ids)
    {
        return feeMapper.deleteFeeByIds(ids);
    }

    @Override
    public int pay(Long id)
    {
        Fee fee = feeMapper.selectFeeById(id);
        if (fee == null)
        {
            throw new ServiceException("收费记录不存在或已删除");
        }
        int rows = feeMapper.payFee(id, ScopeUtils.currentUsername());
        if (rows == 0)
        {
            throw new ServiceException("该账单已缴费，无需重复操作");
        }
        return rows;
    }
}
