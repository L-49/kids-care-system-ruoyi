package com.kids.care.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kids.care.domain.StockRecord;
import com.kids.care.mapper.StockRecordMapper;
import com.kids.care.service.IStockRecordService;

/**
 * 出入库流水 Service 实现
 *
 * @author kids
 * @date 2026-10-18
 */
@Service
public class StockRecordServiceImpl implements IStockRecordService
{
    @Autowired
    private StockRecordMapper stockRecordMapper;

    @Override
    public List<StockRecord> selectStockRecordList(StockRecord stockRecord)
    {
        return stockRecordMapper.selectStockRecordList(stockRecord);
    }

    @Override
    public StockRecord selectStockRecordById(Long id)
    {
        return stockRecordMapper.selectStockRecordById(id);
    }

    @Override
    public int insertStockRecord(StockRecord stockRecord)
    {
        return stockRecordMapper.insertStockRecord(stockRecord);
    }

    @Override
    public int updateStockRecord(StockRecord stockRecord)
    {
        return stockRecordMapper.updateStockRecord(stockRecord);
    }

    @Override
    public int deleteStockRecordByIds(Long[] ids)
    {
        return stockRecordMapper.deleteStockRecordByIds(ids);
    }
}
