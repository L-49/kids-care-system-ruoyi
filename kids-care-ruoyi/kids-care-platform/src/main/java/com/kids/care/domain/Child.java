package com.kids.care.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kids.common.annotation.Excel;
import com.kids.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 幼儿档案对象 child
 *
 * @author kids
 * @date 2026-10-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "幼儿档案")
public class Child extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    @Schema(description = "id")
    private Long id;

    /** 幼儿姓名 */
    @Excel(name = "姓名")
    @Schema(description = "幼儿姓名")
    private String name;

    /** gender */
    @Excel(name = "性别")
    @Schema(description = "gender")
    private String gender;

    /** 出生日期 */
    @Excel(name = "出生日期", dateFormat = "yyyy-MM-dd")
    @Schema(description = "出生日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date birthday;

    /** 所在班级ID */
    @Schema(description = "所在班级ID")
    private Long clazzId;

    /** 班级名称（关联查询填充，Excel 导入导出按名称匹配） */
    @Excel(name = "所在班级")
    @Schema(description = "班级名称")
    private String clazzName;

    /** 家长姓名 */
    @Excel(name = "家长姓名")
    @Schema(description = "家长姓名")
    private String guardianName;

    /** 家长电话 */
    @Excel(name = "家长电话")
    @Schema(description = "家长电话")
    private String guardianPhone;

    /** 家庭住址 */
    @Excel(name = "家庭住址")
    @Schema(description = "家庭住址")
    private String address;

    /** 过敏史/禁忌 */
    @Excel(name = "过敏史/禁忌")
    @Schema(description = "过敏史/禁忌")
    private String allergy;

    /** 入园日期 */
    @Excel(name = "入园日期", dateFormat = "yyyy-MM-dd")
    @Schema(description = "入园日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date enrollDate;

    /** 1在园 0离园 */
    @Excel(name = "状态", readConverterExp = "1=在园,0=离园")
    @Schema(description = "1在园 0离园")
    private Integer status;

    /** deleted */
    @Schema(description = "deleted")
    private Integer deleted;

    /** 档案完整度百分比（0-100，实时计算不落库） */
    @Schema(description = "档案完整度百分比")
    private Integer completeness;

    /** 待补全字段的中文说明，逗号分隔 */
    @Schema(description = "待补全字段说明")
    private String missingItems;

    /** 仅看待补全：查询条件，非数据库字段 */
    @Schema(description = "仅看待补全（查询条件）")
    private Boolean incompleteOnly;
}
