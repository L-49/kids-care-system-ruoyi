package com.kids.care.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kids.common.annotation.Excel;
import com.kids.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班级对象 clazz
 *
 * @author kids
 * @date 2026-10-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "班级")
public class Clazz extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    @Excel(name = "id")
    @Schema(description = "id")
    private Long id;

    /** 班级名称 */
    @Excel(name = "班级名称")
    @Schema(description = "班级名称")
    private String name;

    /** 适龄段，如 3-4岁 */
    @Excel(name = "适龄段，如 3-4岁")
    @Schema(description = "适龄段，如 3-4岁")
    private String ageRange;

    /** 教室 */
    @Excel(name = "教室")
    @Schema(description = "教室")
    private String room;

    /** 额定人数 */
    @Excel(name = "额定人数")
    @Schema(description = "额定人数")
    private Integer capacity;

    /** 班主任ID */
    @Excel(name = "班主任ID")
    @Schema(description = "班主任ID")
    private Long headTeacherId;

    /** 开班日期 */
    @Excel(name = "开班日期", dateFormat = "yyyy-MM-dd")
    @Schema(description = "开班日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date openDate;

    /** deleted */
    @Excel(name = "deleted")
    @Schema(description = "deleted")
    private Integer deleted;

    /** 班主任姓名（关联查询填充） */
    @Schema(description = "班主任姓名")
    private String headTeacherName;

    /** 在园幼儿数（统计填充） */
    @Schema(description = "在园幼儿数")
    private Long childCount;

    /** 满员状态：正常 / 已满 / 超额（实时计算） */
    @Schema(description = "满员状态")
    private String fullStatus;

    /** 教师端只读标记，非数据库字段 */
    @Schema(description = "只读标记")
    private Boolean readonly;
}
