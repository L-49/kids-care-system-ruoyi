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
import com.kids.common.utils.StringUtils;

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
        Date now = new Date();
        if (StringUtils.isEmpty(notice.getTitle()) || notice.getTitle().trim().isEmpty())
        {
            throw new ServiceException("公告标题不能为空");
        }
        notice.setTitle(notice.getTitle().trim());
        if (StringUtils.isEmpty(notice.getType()))
        {
            notice.setType("通知");
        }
        if (notice.getStatus() == null)
        {
            notice.setStatus(STATUS_DRAFT);
        }
        checkPublishTime(notice.getStatus(), notice.getPublishTime(), now);
        // 立即发布不传发布时间，交给数据库 sysdate() 落库，避免 JVM 与数据库毫秒取整差异
        // 造成「发布时间比数据库当前时间晚 1 秒」，家长端短暂看不到刚发的公告
        if (notice.getPublisher() == null)
        {
            notice.setPublisher(ScopeUtils.currentUsername());
        }
        if (notice.getTargetType() == null)
        {
            notice.setTargetType("all");
        }
        if (!"class".equals(notice.getTargetType()))
        {
            notice.setTargetId(null);
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
        // 只提交了部分字段时，用原值补齐，避免 update 语句把未提交的列写成 NULL
        if (notice.getTitle() == null)
        {
            notice.setTitle(old.getTitle());
        }
        if (notice.getContent() == null)
        {
            notice.setContent(old.getContent());
        }
        if (notice.getType() == null)
        {
            notice.setType(old.getType());
        }
        if (notice.getPublisher() == null)
        {
            notice.setPublisher(old.getPublisher());
        }
        if (notice.getStatus() == null)
        {
            notice.setStatus(old.getStatus());
        }
        if (notice.getTargetType() == null)
        {
            notice.setTargetType(old.getTargetType());
        }
        if (!"class".equals(notice.getTargetType()))
        {
            notice.setTargetId(null);
        }
        else if (notice.getTargetId() == null)
        {
            notice.setTargetId(old.getTargetId());
        }

        if (notice.getTitle().trim().isEmpty())
        {
            throw new ServiceException("公告标题不能为空");
        }
        notice.setTitle(notice.getTitle().trim());

        if (old.getStatus() == STATUS_PUBLISHED && notice.getStatus() == STATUS_DRAFT)
        {
            throw new ServiceException("已发布的公告不能退回草稿，请使用撤回");
        }
        // 已发布的公告只允许保持「已发布」，改状态请走撤回 / 立即发布按钮
        if (old.getStatus() == STATUS_PUBLISHED && notice.getStatus() != STATUS_PUBLISHED)
        {
            throw new ServiceException("已发布的公告不能修改发布方式，如需下线请使用撤回");
        }
        // 发布时间：未提交时沿用原值；两者都为空则交给数据库 sysdate() 落库
        Date publishTime = notice.getPublishTime() == null ? old.getPublishTime() : notice.getPublishTime();
        checkPublishTime(notice.getStatus(), publishTime, new Date());
        notice.setPublishTime(publishTime);
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

    /**
     * 发布方式与发布时间的一致性校验。
     *
     * <ul>
     *   <li>定时发布：必须填发布时间，且必须晚于当前时间，否则定时任务会立刻把它发出去；</li>
     *   <li>立即发布：发布时间不能晚于当前时间，否则家长端会提前看到还没到点的公告。</li>
     * </ul>
     */
    private void checkPublishTime(Integer status, Date publishTime, Date now)
    {
        if (status == null)
        {
            return;
        }
        if (status == STATUS_SCHEDULED)
        {
            if (publishTime == null)
            {
                throw new ServiceException("定时发布的公告必须填写发布时间");
            }
            if (!publishTime.after(now))
            {
                throw new ServiceException("定时发布时间必须晚于当前时间");
            }
        }
        if (status == STATUS_PUBLISHED && publishTime != null && publishTime.after(now))
        {
            throw new ServiceException("发布时间晚于当前时间，请把发布方式改为“定时发布”");
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
