package com.kids.care.mapper;

import java.util.List;
import java.util.Map;
import com.kids.care.domain.StockRecord;

/**
 * StockRecord Mapper 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface StockRecordMapper
{
    StockRecord selectStockRecordById(Long id);

    List<StockRecord> selectStockRecordList(StockRecord stockRecord);

    int insertStockRecord(StockRecord stockRecord);

    int updateStockRecord(StockRecord stockRecord);

    int deleteStockRecordById(Long id);

    int deleteStockRecordByIds(Long[] ids);

    /** 库存概览：品类数、预警数、本月出入库笔数与数量 */
    Map<String, Object> selectOverview();
}
