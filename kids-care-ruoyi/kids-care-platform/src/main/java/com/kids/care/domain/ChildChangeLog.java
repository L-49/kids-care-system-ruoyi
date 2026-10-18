package com.kids.care.domain;

import com.kids.common.annotation.Excel;
import com.kids.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 档案变更记录对象 child_change_log
 *
 * @author kids
 * @date 2026-10-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "档案变更记录")
public class ChildChangeLog extends BaseEntity
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

    /** 留档时的姓名（档案被删也能追溯） */
    @Excel(name = "留档时的姓名（档案被删也能追溯）")
    @Schema(description = "留档时的姓名（档案被删也能追溯）")
    private String childName;

    /** 新增/修改/导入 */
    @Excel(name = "新增/修改/导入")
    @Schema(description = "新增/修改/导入")
    private String changeType;

    /** 字段英文名，整条记录时为空 */
    @Excel(name = "字段英文名，整条记录时为空")
    @Schema(description = "字段英文名，整条记录时为空")
    private String fieldName;

    /** 字段中文名 */
    @Excel(name = "字段中文名")
    @Schema(description = "字段中文名")
    private String fieldLabel;

    /** 变更前 */
    @Excel(name = "变更前")
    @Schema(description = "变更前")
    private String oldValue;

    /** 变更后 */
    @Excel(name = "变更后")
    @Schema(description = "变更后")
    private String newValue;

    /** 操作人 */
    @Excel(name = "操作人")
    @Schema(description = "操作人")
    private String operator;

    /** 批量导入批次号，一次导入共用一个 */
    @Excel(name = "批量导入批次号，一次导入共用一个")
    @Schema(description = "批量导入批次号，一次导入共用一个")
    private String batchId;
}
