package com.kids.care.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kids.common.annotation.Excel;
import com.kids.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 考勤记录对象 attendance
 *
 * @author kids
 * @date 2026-10-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "考勤记录")
public class Attendance extends BaseEntity
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

    /** 班级ID（冗余，便于按班统计） */
    @Excel(name = "班级ID（冗余，便于按班统计）")
    @Schema(description = "班级ID（冗余，便于按班统计）")
    private Long clazzId;

    /** 考勤日期 */
    @Excel(name = "考勤日期", dateFormat = "yyyy-MM-dd")
    @Schema(description = "考勤日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date attDate;

    /** 出勤/迟到/请假/缺勤 */
    @Excel(name = "出勤/迟到/请假/缺勤")
    @Schema(description = "出勤/迟到/请假/缺勤")
    private String status;

    /** 入园时间 HH:mm */
    @Excel(name = "入园时间 HH:mm")
    @Schema(description = "入园时间 HH:mm")
    private String checkInTime;

    /** deleted */
    @Excel(name = "deleted")
    @Schema(description = "deleted")
    private Integer deleted;

    /** 幼儿姓名（关联查询填充） */
    @Schema(description = "幼儿姓名")
    private String childName;

    /** 班级名称（关联查询填充） */
    @Schema(description = "班级名称")
    private String clazzName;
}
