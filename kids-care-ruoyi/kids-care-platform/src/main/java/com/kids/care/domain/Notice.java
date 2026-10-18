package com.kids.care.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kids.common.annotation.Excel;
import com.kids.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知公告对象 notice
 *
 * @author kids
 * @date 2026-10-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "通知公告")
public class Notice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    @Excel(name = "id")
    @Schema(description = "id")
    private Long id;

    /** 标题 */
    @Excel(name = "标题")
    @Schema(description = "标题")
    private String title;

    /** 内容 */
    @Excel(name = "内容")
    @Schema(description = "内容")
    private String content;

    /** 类型：园务/班级/健康/活动 */
    @Excel(name = "类型：园务/班级/健康/活动")
    @Schema(description = "类型：园务/班级/健康/活动")
    private String type;

    /** 发布人 */
    @Excel(name = "发布人")
    @Schema(description = "发布人")
    private String publisher;

    /** 0草稿 1定时待发 2已发布 3已撤回 */
    @Excel(name = "0草稿 1定时待发 2已发布 3已撤回")
    @Schema(description = "0草稿 1定时待发 2已发布 3已撤回")
    private Integer status;

    /** 可见范围：all全园 class指定班级 */
    @Excel(name = "可见范围：all全园 class指定班级")
    @Schema(description = "可见范围：all全园 class指定班级")
    private String targetType;

    /** 指定班级ID（target_type=class 时有效） */
    @Excel(name = "指定班级ID（target_type=class 时有效）")
    @Schema(description = "指定班级ID（target_type=class 时有效）")
    private Long targetId;

    /** 发布时间 */
    @Excel(name = "发布时间", dateFormat = "yyyy-MM-dd")
    @Schema(description = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date publishTime;

    /** deleted */
    @Excel(name = "deleted")
    @Schema(description = "deleted")
    private Integer deleted;

    /** 定向可见的展示文案，如「全园」「中班」 */
    @Schema(description = "可见范围文案")
    private String targetLabel;

    /** 只查已发布（家长端） */
    @Schema(description = "只查已发布")
    private Boolean onlyPublished;

    /** 家长所在班级，用于定向过滤（家长端） */
    @Schema(description = "家长所在班级ID")
    private Long visibleClazzId;

    /** 管理端按状态筛选，可为空 */
    @Schema(description = "状态筛选")
    private Integer statusFilter;
}
