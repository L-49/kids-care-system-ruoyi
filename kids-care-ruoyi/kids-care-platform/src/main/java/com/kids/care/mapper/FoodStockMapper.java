package com.kids.care.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.kids.care.domain.FoodStock;

/**
 * FoodStock Mapper 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface FoodStockMapper
{
    FoodStock selectFoodStockById(Long id);

    List<FoodStock> selectFoodStockList(FoodStock foodStock);

    int insertFoodStock(FoodStock foodStock);

    int updateFoodStock(FoodStock foodStock);

    int deleteFoodStockById(Long id);

    int deleteFoodStockByIds(Long[] ids);

    /**
     * 原子增减库存：条件里带 quantity + delta >= 0，扣成负数时影响行数为 0。
     *
     * @param id    食材ID
     * @param delta 增量（入库为正、出库为负）
     * @return 影响行数，0 表示库存不足或食材不存在
     */
    int updateQuantity(@Param("id") Long id, @Param("delta") BigDecimal delta);
}
