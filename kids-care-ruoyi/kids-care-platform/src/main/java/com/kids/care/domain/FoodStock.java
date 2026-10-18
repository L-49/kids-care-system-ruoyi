package com.kids.care.domain;

import com.kids.common.annotation.Excel;
import com.kids.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 食材库存对象 food_stock
 *
 * @author kids
 * @date 2026-10-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "食材库存")
public class FoodStock extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    @Excel(name = "id")
    @Schema(description = "id")
    private Long id;

    /** 食材名称 */
    @Excel(name = "食材名称")
    @Schema(description = "食材名称")
    private String name;

    /** 分类：主食/蔬菜/肉类/水果/其他 */
    @Excel(name = "分类：主食/蔬菜/肉类/水果/其他")
    @Schema(description = "分类：主食/蔬菜/肉类/水果/其他")
    private String category;

    /** 库存数量 */
    @Excel(name = "库存数量")
    @Schema(description = "库存数量")
    private BigDecimal quantity;

    /** 单位 */
    @Excel(name = "单位")
    @Schema(description = "单位")
    private String unit;

    /** 预警线 */
    @Excel(name = "预警线")
    @Schema(description = "预警线")
    private BigDecimal warnLine;

    /** 供应商 */
    @Excel(name = "供应商")
    @Schema(description = "供应商")
    private String supplier;

    /** deleted */
    @Excel(name = "deleted")
    @Schema(description = "deleted")
    private Integer deleted;

    /** 是否低于预警线（实时计算，非数据库字段） */
    @Schema(description = "是否低于预警线")
    private Boolean warning;

    /** 仅看预警项：查询条件 */
    @Schema(description = "仅看预警项（查询条件）")
    private Boolean warnOnly;
}
