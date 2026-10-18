package com.kids.care.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kids.common.annotation.Excel;
import com.kids.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 请假申请对象 leave_request
 *
 * @author kids
 * @date 2026-10-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "请假申请")
public class LeaveRequest extends BaseEntity
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

    /** 开始日期 */
    @Excel(name = "开始日期", dateFormat = "yyyy-MM-dd")
    @Schema(description = "开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    /** 结束日期 */
    @Excel(name = "结束日期", dateFormat = "yyyy-MM-dd")
    @Schema(description = "结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;

    /** 请假天数 */
    @Excel(name = "请假天数")
    @Schema(description = "请假天数")
    private Integer days;

    /** 请假事由 */
    @Excel(name = "请假事由")
    @Schema(description = "请假事由")
    private String reason;

    /** 0待审批 1已通过 2已驳回 */
    @Excel(name = "0待审批 1已通过 2已驳回")
    @Schema(description = "0待审批 1已通过 2已驳回")
    private Integer status;

    /** 审批人 */
    @Excel(name = "审批人")
    @Schema(description = "审批人")
    private String approver;

    /** 审批时间 */
    @Excel(name = "审批时间", dateFormat = "yyyy-MM-dd")
    @Schema(description = "审批时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date approveTime;

    /** deleted */
    @Excel(name = "deleted")
    @Schema(description = "deleted")
    private Integer deleted;

    /** 幼儿姓名（关联查询填充） */
    @Schema(description = "幼儿姓名")
    private String childName;
}
