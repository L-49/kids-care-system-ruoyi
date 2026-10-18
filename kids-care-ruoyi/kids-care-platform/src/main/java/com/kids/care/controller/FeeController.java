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
import com.kids.care.domain.Fee;
import com.kids.care.service.IFeeService;

/**
 * 收费记录 控制器
 *
 * @author kids
 * @date 2026-10-18
 */
@RestController
@RequestMapping("/kids/fee")
public class FeeController extends BaseController
{
    @Autowired
    private IFeeService feeService;

    /** 查询列表 */
    @PreAuthorize("@ss.hasPermi('kids:fee:list')")
    @GetMapping("/list")
    public TableDataInfo list(Fee fee)
    {
        startPage();
        List<Fee> list = feeService.selectFeeList(fee);
        return getDataTable(list);
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('kids:fee:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(feeService.selectFeeById(id));
    }

    /** 新增 */
    @PreAuthorize("@ss.hasPermi('kids:fee:add')")
    @Log(title = "收费记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Fee fee)
    {
        return toAjax(feeService.insertFee(fee));
    }

    /** 修改 */
    @PreAuthorize("@ss.hasPermi('kids:fee:edit')")
    @Log(title = "收费记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Fee fee)
    {
        return toAjax(feeService.updateFee(fee));
    }

    /** 删除 */
    @PreAuthorize("@ss.hasPermi('kids:fee:remove')")
    @Log(title = "收费记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(feeService.deleteFeeByIds(ids));
    }

    /** 缴费登记 */
    @PreAuthorize("@ss.hasPermi('kids:fee:pay')")
    @Log(title = "收费记录", businessType = BusinessType.UPDATE)
    @PutMapping("/pay/{id}")
    public AjaxResult pay(@PathVariable Long id)
    {
        return toAjax(feeService.pay(id));
    }
}
