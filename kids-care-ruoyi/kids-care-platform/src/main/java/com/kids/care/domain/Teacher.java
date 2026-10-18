package com.kids.care.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kids.common.annotation.Excel;
import com.kids.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 教职工对象 teacher
 *
 * @author kids
 * @date 2026-10-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "教职工")
public class Teacher extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    @Excel(name = "id")
    @Schema(description = "id")
    private Long id;

    /** 姓名 */
    @Excel(name = "姓名")
    @Schema(description = "姓名")
    private String name;

    /** 性别 */
    @Excel(name = "性别")
    @Schema(description = "性别")
    private String gender;

    /** 联系电话 */
    @Excel(name = "联系电话")
    @Schema(description = "联系电话")
    private String phone;

    /** 岗位：园长/教师/保育员/保健医 */
    @Excel(name = "岗位：园长/教师/保育员/保健医")
    @Schema(description = "岗位：园长/教师/保育员/保健医")
    private String role;

    /** 职称 */
    @Excel(name = "职称")
    @Schema(description = "职称")
    private String title;

    /** 学历 */
    @Excel(name = "学历")
    @Schema(description = "学历")
    private String education;

    /** 身份证号 */
    @Excel(name = "身份证号")
    @Schema(description = "身份证号")
    private String idCard;

    /** 紧急联系人 */
    @Excel(name = "紧急联系人")
    @Schema(description = "紧急联系人")
    private String emergencyContact;

    /** 紧急电话 */
    @Excel(name = "紧急电话")
    @Schema(description = "紧急电话")
    private String emergencyPhone;

    /** 入职日期 */
    @Excel(name = "入职日期", dateFormat = "yyyy-MM-dd")
    @Schema(description = "入职日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date entryDate;

    /** 1在职 0离职 */
    @Excel(name = "1在职 0离职")
    @Schema(description = "1在职 0离职")
    private Integer status;

    /** deleted */
    @Excel(name = "deleted")
    @Schema(description = "deleted")
    private Integer deleted;
}
