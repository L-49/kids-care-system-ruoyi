package com.kids.care.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kids.care.domain.Child;
import com.kids.care.domain.Clazz;
import com.kids.care.domain.Notice;
import com.kids.care.mapper.ChildMapper;
import com.kids.care.mapper.ClazzMapper;
import com.kids.care.mapper.NoticeMapper;
import com.kids.care.service.INoticeService;
import com.kids.care.utils.ScopeUtils;
import com.kids.common.exception.ServiceException;

/**
 * 通知公告 Service 实现。
 *
 * <p>四状态机：0 草稿 → 1 定时待发 → 2 已发布 → 3 已撤回。</p>
 * <ul>
 *   <li>撤回不是物理删除，管理端默认隐藏，显式按 status=3 才查得到；</li>
 *   <li>定时公告由定时任务每分钟扫描一次，到点自动转为已发布；</li>
 *   <li>家长只能看到「全园」与自己孩子所在班级的公告，且只能看到已发布状态。</li>
 * </ul>
 *
 * @author kids
 * @date 2026-10-18
 */
@Service
public class NoticeServiceImpl implements INoticeService
{
    /** 草稿 */
    public static final int STATUS_DRAFT = 0;

    /** 定时待发 */
    public static final int STATUS_SCHEDULED = 1;

    /** 已发布 */
    public static final int STATUS_PUBLISHED = 2;

    /** 已撤回 */
    public static final int STATUS_REVOKED = 3;

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private ChildMapper childMapper;

    @Autowired
    private ClazzMapper clazzMapper;

    @Override
    public List<Notice> selectNoticeList(Notice query)
    {
        applyParentScope(query);
        List<Notice> list = noticeMapper.selectNoticeList(query);
        for (Notice n : list)
        {
            n.setTargetLabel(labelOf(n));
        }
        return list;
    }

    @Override
    public Notice selectNoticeById(Long id)
    {
        Notice notice = noticeMapper.selectNoticeById(id);
        if (notice == null)
        {
            throw new ServiceException("公告不存在或已删除");
        }
        notice.setTargetLabel(labelOf(notice));
        return notice;
    }

    @Override
    public int insertNotice(Notice notice)
    {
        if (notice.getStatus() == null)
        {
            notice.setStatus(STATUS_DRAFT);
        }
        if (notice.getStatus() == STATUS_SCHEDULED && notice.getPublishTime() == null)
        {
            throw new ServiceException("定时发布的公告必须填写发布时间");
        }
        if (notice.getStatus() == STATUS_PUBLISHED && notice.getPublishTime() == null)
        {
            notice.setPublishTime(new Date());
        }
        if (notice.getPublisher() == null)
        {
            notice.setPublisher(ScopeUtils.currentUsername());
        }
        if (notice.getTargetType() == null)
        {
            notice.setTargetType("all");
        }
        return noticeMapper.insertNotice(notice);
    }

    @Override
    public int updateNotice(Notice notice)
    {
        Notice old = noticeMapper.selectNoticeById(notice.getId());
        if (old == null)
        {
            throw new ServiceException("公告不存在或已删除");
        }
        if (old.getStatus() == STATUS_PUBLISHED && notice.getStatus() != null
                && notice.getStatus() == STATUS_DRAFT)
        {
            throw new ServiceException("已发布的公告不能退回草稿，请使用撤回");
        }
        if (notice.getStatus() != null && notice.getStatus() == STATUS_SCHEDULED
                && notice.getPublishTime() == null)
        {
            throw new ServiceException("定时发布的公告必须填写发布时间");
        }
        // 已发布的公告只允许保持「已发布」，改状态请走撤回 / 立即发布按钮
        if (old.getStatus() == STATUS_PUBLISHED && notice.getStatus() != null
                && notice.getStatus() != STATUS_PUBLISHED)
        {
            throw new ServiceException("已发布的公告不能修改发布方式，如需下线请使用撤回");
        }
        if (notice.getStatus() != null && notice.getStatus() == STATUS_PUBLISHED
                && notice.getPublishTime() == null)
        {
            notice.setPublishTime(old.getPublishTime() == null ? new Date() : old.getPublishTime());
        }
        return noticeMapper.updateNotice(notice);
    }

    @Override
    public int deleteNoticeByIds(Long[] ids)
    {
        return noticeMapper.deleteNoticeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id)
    {
        Notice notice = noticeMapper.selectNoticeById(id);
        if (notice == null)
        {
            throw new ServiceException("公告不存在或已删除");
        }
        if (notice.getStatus() != null && notice.getStatus() == STATUS_PUBLISHED)
        {
            throw new ServiceException("该公告已发布，无需重复发布");
        }
        if (notice.getStatus() != null && notice.getStatus() == STATUS_REVOKED)
        {
            throw new ServiceException("已撤回的公告不能直接发布，请先修改后重新提交");
        }
        noticeMapper.updateStatusAndTime(id, STATUS_PUBLISHED, new Date());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revoke(Long id)
    {
        Notice notice = noticeMapper.selectNoticeById(id);
        if (notice == null)
        {
            throw new ServiceException("公告不存在或已删除");
        }
        if (notice.getStatus() == null || notice.getStatus() != STATUS_PUBLISHED)
        {
            throw new ServiceException("只有已发布的公告才能撤回");
        }
        noticeMapper.updateStatus(id, STATUS_REVOKED);
    }

    @Override
    public int publishDueNotices()
    {
        return noticeMapper.publishDue();
    }

    /**
     * 家长端：只返回已发布，且限于全园或自己孩子班级。
     */
    @Override
    public List<Notice> selectVisibleNotices()
    {
        Notice query = new Notice();
        query.setOnlyPublished(Boolean.TRUE);
        Long childId = ScopeUtils.scopeChildId();
        if (childId != null)
        {
            Child child = childMapper.selectChildById(childId);
            if (child != null && child.getClazzId() != null)
            {
                query.setVisibleClazzId(child.getClazzId());
            }
            else
            {
                // 孩子没有班级时只保留全园公告
                query.setVisibleClazzId(-1L);
            }
        }
        List<Notice> list = noticeMapper.selectNoticeList(query);
        for (Notice n : list)
        {
            n.setTargetLabel(labelOf(n));
        }
        return list;
    }

    // ------------------------------------------------------------------

    private void applyParentScope(Notice query)
    {
        if (ScopeUtils.isParent())
        {
            query.setOnlyPublished(Boolean.TRUE);
            Long childId = ScopeUtils.scopeChildId();
            Child child = childId == null ? null : childMapper.selectChildById(childId);
            query.setVisibleClazzId(child != null && child.getClazzId() != null ? child.getClazzId() : -1L);
        }
    }

    private String labelOf(Notice n)
    {
        if (n.getTargetType() == null || "all".equals(n.getTargetType()))
        {
            return "全园";
        }
        if (n.getTargetId() == null)
        {
            return "指定班级";
        }
        Clazz clazz = clazzMapper.selectClazzById(n.getTargetId());
        return clazz == null || clazz.getName() == null ? "指定班级" : clazz.getName();
    }
}
