package com.kids.care.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kids.common.annotation.Excel;
import com.kids.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收费记录对象 fee
 *
 * @author kids
 * @date 2026-10-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "收费记录")
public class Fee extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    @Excel(name = "id")
    @Schema(description = "id")
    private Long id;

    /** 幼儿ID */
    @Excel(name = "幼儿ID")
    @Schema(description = "幼儿ID")
    private Long childId;

    /** 保教费/餐费/校车费/其他 */
    @Excel(name = "保教费/餐费/校车费/其他")
    @Schema(description = "保教费/餐费/校车费/其他")
    private String feeType;

    /** 金额 */
    @Excel(name = "金额")
    @Schema(description = "金额")
    private BigDecimal amount;

    /** 所属月份 yyyy-MM */
    @Excel(name = "所属月份 yyyy-MM")
    @Schema(description = "所属月份 yyyy-MM")
    private String feeMonth;

    /** 0未缴 1已缴 */
    @Excel(name = "0未缴 1已缴")
    @Schema(description = "0未缴 1已缴")
    private Integer payStatus;

    /** 缴费时间 */
    @Excel(name = "缴费时间", dateFormat = "yyyy-MM-dd")
    @Schema(description = "缴费时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payTime;

    /** deleted */
    @Excel(name = "deleted")
    @Schema(description = "deleted")
    private Integer deleted;

    /** 幼儿姓名（关联查询填充） */
    @Schema(description = "幼儿姓名")
    private String childName;
}
