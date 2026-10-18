package com.kids.care.domain;

import com.kids.common.annotation.Excel;
import com.kids.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 出入库流水对象 stock_record
 *
 * @author kids
 * @date 2026-10-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "出入库流水")
public class StockRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    @Excel(name = "id")
    @Schema(description = "id")
    private Long id;

    /** 食材ID */
    @Excel(name = "食材ID")
    @Schema(description = "食材ID")
    private Long foodId;

    /** 食材名称（冗余） */
    @Excel(name = "食材名称（冗余）")
    @Schema(description = "食材名称（冗余）")
    private String foodName;

    /** IN入库 / OUT出库 */
    @Excel(name = "IN入库 / OUT出库")
    @Schema(description = "IN入库 / OUT出库")
    private String type;

    /** 本次数量 */
    @Excel(name = "本次数量")
    @Schema(description = "本次数量")
    private BigDecimal quantity;

    /** 操作前库存 */
    @Excel(name = "操作前库存")
    @Schema(description = "操作前库存")
    private BigDecimal beforeQty;

    /** 操作后库存 */
    @Excel(name = "操作后库存")
    @Schema(description = "操作后库存")
    private BigDecimal afterQty;

    /** 单位 */
    @Excel(name = "单位")
    @Schema(description = "单位")
    private String unit;

    /** 操作人 */
    @Excel(name = "操作人")
    @Schema(description = "操作人")
    private String operator;
}
