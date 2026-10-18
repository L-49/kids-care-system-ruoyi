package com.kids.care.service;

import java.util.List;
import com.kids.care.domain.Fee;

/**
 * 收费记录 Service 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface IFeeService
{
    List<Fee> selectFeeList(Fee fee);

    Fee selectFeeById(Long id);

    int insertFee(Fee fee);

    int updateFee(Fee fee);

    int deleteFeeByIds(Long[] ids);

    /** 缴费登记：已缴费的记录不允许重复操作 */
    int pay(Long id);
}
