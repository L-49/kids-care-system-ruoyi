package com.kids.care.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kids.common.annotation.Excel;
import com.kids.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程对象 course
 *
 * @author kids
 * @date 2026-10-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程")
public class Course extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    @Excel(name = "id")
    @Schema(description = "id")
    private Long id;

    /** 课程名称 */
    @Excel(name = "课程名称")
    @Schema(description = "课程名称")
    private String title;

    /** 班级 */
    @Excel(name = "班级")
    @Schema(description = "班级")
    private Long clazzId;

    /** 授课教师 */
    @Excel(name = "授课教师")
    @Schema(description = "授课教师")
    private Long teacherId;

    /** 上课日期 */
    @Excel(name = "上课日期", dateFormat = "yyyy-MM-dd")
    @Schema(description = "上课日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date courseDate;

    /** 开始时间 HH:mm */
    @Excel(name = "开始时间 HH:mm")
    @Schema(description = "开始时间 HH:mm")
    private String startTime;

    /** 结束时间 HH:mm */
    @Excel(name = "结束时间 HH:mm")
    @Schema(description = "结束时间 HH:mm")
    private String endTime;

    /** 活动内容 */
    @Excel(name = "活动内容")
    @Schema(description = "活动内容")
    private String content;

    /** 1正常 0取消 */
    @Excel(name = "1正常 0取消")
    @Schema(description = "1正常 0取消")
    private Integer status;

    /** deleted */
    @Schema(description = "deleted")
    private Integer deleted;

    /** 班级名称（关联查询填充） */
    @Schema(description = "班级名称")
    private String clazzName;

    /** 教师姓名（关联查询填充） */
    @Schema(description = "教师姓名")
    private String teacherName;
}
