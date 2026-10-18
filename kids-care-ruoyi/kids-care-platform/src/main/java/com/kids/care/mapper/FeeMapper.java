package com.kids.care.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.kids.care.domain.Fee;

/**
 * Fee Mapper 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface FeeMapper
{
    Fee selectFeeById(Long id);

    List<Fee> selectFeeList(Fee fee);

    int insertFee(Fee fee);

    int updateFee(Fee fee);

    int deleteFeeById(Long id);

    int deleteFeeByIds(Long[] ids);

    /** 缴费登记：只有未缴记录会被更新，返回 0 表示已缴费或不存在 */
    int payFee(@Param("id") Long id, @Param("operator") String operator);
}
