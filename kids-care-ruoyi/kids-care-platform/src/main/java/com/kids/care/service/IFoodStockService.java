package com.kids.care.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.kids.care.domain.FoodStock;
import com.kids.care.domain.StockRecord;

/**
 * 食材库存 Service 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface IFoodStockService
{
    List<FoodStock> selectFoodStockList(FoodStock foodStock);

    FoodStock selectFoodStockById(Long id);

    int insertFoodStock(FoodStock foodStock);

    int updateFoodStock(FoodStock foodStock);

    int deleteFoodStockByIds(Long[] ids);

    /**
     * 出入库：delta 为正表示入库、为负表示出库。
     * 库存扣成负数时抛业务异常；成功后写一条 stock_record 流水。
     */
    void changeStock(Long foodId, BigDecimal delta, String remark);

    /** 出入库流水查询 */
    List<StockRecord> selectRecordList(StockRecord query);

    /** 库存概览：品类数、预警数、本月出入库笔数与数量 */
    Map<String, Object> overview();
}
