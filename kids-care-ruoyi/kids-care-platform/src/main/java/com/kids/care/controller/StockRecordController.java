package com.kids.care.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.kids.common.annotation.Log;
import com.kids.common.core.controller.BaseController;
import com.kids.common.core.domain.AjaxResult;
import com.kids.common.core.page.TableDataInfo;
import com.kids.common.enums.BusinessType;
import com.kids.care.domain.StockRecord;
import com.kids.care.service.IStockRecordService;

/**
 * 出入库流水 控制器
 *
 * @author kids
 * @date 2026-10-18
 */
@RestController
@RequestMapping("/kids/food/record")
public class StockRecordController extends BaseController
{
    @Autowired
    private IStockRecordService stockRecordService;

    /** 查询列表 */
    @PreAuthorize("@ss.hasPermi('kids:food:list')")
    @GetMapping("/list")
    public TableDataInfo list(StockRecord stockRecord)
    {
        startPage();
        List<StockRecord> list = stockRecordService.selectStockRecordList(stockRecord);
        return getDataTable(list);
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('kids:food:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(stockRecordService.selectStockRecordById(id));
    }

    /** 新增 */
    @PreAuthorize("@ss.hasPermi('kids:food:add')")
    @Log(title = "出入库流水", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody StockRecord stockRecord)
    {
        return toAjax(stockRecordService.insertStockRecord(stockRecord));
    }

    /** 修改 */
    @PreAuthorize("@ss.hasPermi('kids:food:edit')")
    @Log(title = "出入库流水", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody StockRecord stockRecord)
    {
        return toAjax(stockRecordService.updateStockRecord(stockRecord));
    }

    /** 删除 */
    @PreAuthorize("@ss.hasPermi('kids:food:remove')")
    @Log(title = "出入库流水", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(stockRecordService.deleteStockRecordByIds(ids));
    }
}
