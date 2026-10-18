package com.kids.care.service;

import java.util.List;
import com.kids.care.domain.StockRecord;

/**
 * 出入库流水 Service 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface IStockRecordService
{
    List<StockRecord> selectStockRecordList(StockRecord stockRecord);

    StockRecord selectStockRecordById(Long id);

    int insertStockRecord(StockRecord stockRecord);

    int updateStockRecord(StockRecord stockRecord);

    int deleteStockRecordByIds(Long[] ids);
}
