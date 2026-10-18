package com.kids.care.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kids.care.domain.FoodStock;
import com.kids.care.domain.StockRecord;
import com.kids.care.mapper.FoodStockMapper;
import com.kids.care.mapper.StockRecordMapper;
import com.kids.care.service.IFoodStockService;
import com.kids.care.utils.ScopeUtils;
import com.kids.common.exception.ServiceException;
import com.kids.common.utils.StringUtils;

/**
 * 食材库存 Service 实现。
 *
 * <p>库存扣减不采用「先查后改」：update 语句自带 {@code quantity + delta >= 0} 条件，
 * 并发下由数据库行锁保证不会扣成负数，影响行数为 0 即判定库存不足。
 * 每次变更写一条只增不改的 stock_record，保存操作前后数量以便追溯。</p>
 *
 * @author kids
 * @date 2026-10-18
 */
@Service
public class FoodStockServiceImpl implements IFoodStockService
{
    private static final String TYPE_IN = "IN";

    private static final String TYPE_OUT = "OUT";

    @Autowired
    private FoodStockMapper foodStockMapper;

    @Autowired
    private StockRecordMapper stockRecordMapper;

    @Override
    public List<FoodStock> selectFoodStockList(FoodStock query)
    {
        List<FoodStock> list = foodStockMapper.selectFoodStockList(query);
        for (FoodStock f : list)
        {
            f.setWarning(isWarning(f));
        }
        return list;
    }

    @Override
    public FoodStock selectFoodStockById(Long id)
    {
        FoodStock food = foodStockMapper.selectFoodStockById(id);
        if (food == null)
        {
            throw new ServiceException("食材不存在或已删除");
        }
        food.setWarning(isWarning(food));
        return food;
    }

    @Override
    public int insertFoodStock(FoodStock food)
    {
        if (StringUtils.isEmpty(food.getName()))
        {
            throw new ServiceException("食材名称不能为空");
        }
        if (food.getQuantity() == null)
        {
            food.setQuantity(BigDecimal.ZERO);
        }
        if (food.getQuantity().signum() < 0)
        {
            throw new ServiceException("库存数量不能为负数");
        }
        return foodStockMapper.insertFoodStock(food);
    }

    @Override
    public int updateFoodStock(FoodStock food)
    {
        // 台账数量只能通过出入库变更，避免直接改数绕过流水
        if (food.getQuantity() != null)
        {
            FoodStock old = foodStockMapper.selectFoodStockById(food.getId());
            if (old != null && old.getQuantity() != null
                    && food.getQuantity().compareTo(old.getQuantity()) != 0)
            {
                throw new ServiceException("库存数量请通过入库/出库操作变更，以便留下流水");
            }
        }
        return foodStockMapper.updateFoodStock(food);
    }

    @Override
    public int deleteFoodStockByIds(Long[] ids)
    {
        return foodStockMapper.deleteFoodStockByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStock(Long foodId, BigDecimal delta, String remark)
    {
        if (delta == null || delta.signum() == 0)
        {
            throw new ServiceException("出入库数量必须大于 0");
        }
        FoodStock food = foodStockMapper.selectFoodStockById(foodId);
        if (food == null)
        {
            throw new ServiceException("食材不存在或已删除");
        }
        BigDecimal before = food.getQuantity() == null ? BigDecimal.ZERO : food.getQuantity();
        BigDecimal after = before.add(delta);
        if (after.signum() < 0)
        {
            throw new ServiceException(String.format("库存不足：「%s」当前库存 %s%s，无法出库 %s%s",
                    food.getName(), trim(before), nullToEmpty(food.getUnit()),
                    trim(delta.abs()), nullToEmpty(food.getUnit())));
        }

        int rows = foodStockMapper.updateQuantity(foodId, delta);
        if (rows == 0)
        {
            // 并发下被其他请求抢先扣减，库存已不足
            throw new ServiceException(String.format("「%s」库存不足，请刷新后重试", food.getName()));
        }

        StockRecord record = new StockRecord();
        record.setFoodId(foodId);
        record.setFoodName(food.getName());
        record.setType(delta.signum() > 0 ? TYPE_IN : TYPE_OUT);
        record.setQuantity(delta.abs());
        record.setBeforeQty(before);
        record.setAfterQty(after);
        record.setUnit(food.getUnit());
        record.setOperator(ScopeUtils.currentUsername());
        record.setRemark(remark);
        stockRecordMapper.insertStockRecord(record);
    }

    @Override
    public List<StockRecord> selectRecordList(StockRecord query)
    {
        return stockRecordMapper.selectStockRecordList(query);
    }

    @Override
    public Map<String, Object> overview()
    {
        return stockRecordMapper.selectOverview();
    }

    // ------------------------------------------------------------------

    private boolean isWarning(FoodStock f)
    {
        if (f.getQuantity() == null || f.getWarnLine() == null)
        {
            return false;
        }
        return f.getQuantity().compareTo(f.getWarnLine()) < 0;
    }

    private String trim(BigDecimal v)
    {
        return v.stripTrailingZeros().toPlainString();
    }

    private String nullToEmpty(String s)
    {
        return s == null ? "" : s;
    }
}
