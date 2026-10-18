package com.kids.care.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.kids.common.annotation.Log;
import com.kids.common.core.controller.BaseController;
import com.kids.common.core.domain.AjaxResult;
import com.kids.common.core.page.TableDataInfo;
import com.kids.common.enums.BusinessType;
import com.kids.care.domain.FoodStock;
import com.kids.care.domain.StockRecord;
import com.kids.care.service.IFoodStockService;

/**
 * 食材库存 控制器
 *
 * @author kids
 * @date 2026-10-18
 */
@RestController
@RequestMapping("/kids/food")
public class FoodStockController extends BaseController
{
    @Autowired
    private IFoodStockService foodStockService;

    /** 查询列表 */
    @PreAuthorize("@ss.hasPermi('kids:food:list')")
    @GetMapping("/list")
    public TableDataInfo list(FoodStock foodStock)
    {
        startPage();
        List<FoodStock> list = foodStockService.selectFoodStockList(foodStock);
        return getDataTable(list);
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('kids:food:query')")
    @GetMapping("/{id:\\d+}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(foodStockService.selectFoodStockById(id));
    }

    /** 新增 */
    @PreAuthorize("@ss.hasPermi('kids:food:add')")
    @Log(title = "食材库存", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FoodStock foodStock)
    {
        return toAjax(foodStockService.insertFoodStock(foodStock));
    }

    /** 修改 */
    @PreAuthorize("@ss.hasPermi('kids:food:edit')")
    @Log(title = "食材库存", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FoodStock foodStock)
    {
        return toAjax(foodStockService.updateFoodStock(foodStock));
    }

    /** 删除 */
    @PreAuthorize("@ss.hasPermi('kids:food:remove')")
    @Log(title = "食材库存", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(foodStockService.deleteFoodStockByIds(ids));
    }

    /** 库存概览：品类数 / 预警数 / 本月出入库 */
    @PreAuthorize("@ss.hasPermi('kids:food:list')")
    @GetMapping("/overview")
    public AjaxResult overview()
    {
        return success(foodStockService.overview());
    }

    /** 出入库：{ "type": "IN|OUT", "quantity": 20, "remark": "补充采购" } */
    @PreAuthorize("@ss.hasPermi('kids:food:stock')")
    @Log(title = "食材库存", businessType = BusinessType.UPDATE)
    @PostMapping("/{id:\\d+}/stock")
    public AjaxResult stock(@PathVariable Long id, @RequestBody Map<String, Object> body)
    {
        if (body == null || body.get("quantity") == null)
        {
            throw new com.kids.common.exception.ServiceException("请填写出入库数量");
        }
        String type = String.valueOf(body.getOrDefault("type", "IN"));
        BigDecimal qty = new BigDecimal(String.valueOf(body.get("quantity")));
        if (qty.signum() <= 0)
        {
            throw new com.kids.common.exception.ServiceException("出入库数量必须大于 0");
        }
        BigDecimal delta = "OUT".equalsIgnoreCase(type) ? qty.negate() : qty;
        foodStockService.changeStock(id, delta,
                body.get("remark") == null ? null : String.valueOf(body.get("remark")));
        return success();
    }
}
