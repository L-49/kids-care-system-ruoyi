package com.kids.care.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kids.common.annotation.Excel;
import com.kids.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 每周食谱对象 menu_plan
 *
 * @author kids
 * @date 2026-10-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "每周食谱")
public class MenuPlan extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    @Excel(name = "id")
    @Schema(description = "id")
    private Long id;

    /** 日期 */
    @Excel(name = "日期", dateFormat = "yyyy-MM-dd")
    @Schema(description = "日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date menuDate;

    /** 餐次：早餐/午餐/午点 */
    @Excel(name = "餐次：早餐/午餐/午点")
    @Schema(description = "餐次：早餐/午餐/午点")
    private String mealType;

    /** 菜品 */
    @Excel(name = "菜品")
    @Schema(description = "菜品")
    private String dishes;

    /** 备注/营养说明 */
    @Excel(name = "备注/营养说明")
    @Schema(description = "备注/营养说明")
    private String note;

    /** 发布人 */
    @Excel(name = "发布人")
    @Schema(description = "发布人")
    private String publisher;

    /** deleted */
    @Excel(name = "deleted")
    @Schema(description = "deleted")
    private Integer deleted;
}
