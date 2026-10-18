package com.kids.care.service;

import java.util.List;
import com.kids.care.domain.Notice;

/**
 * 通知公告 Service 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface INoticeService
{
    List<Notice> selectNoticeList(Notice notice);

    Notice selectNoticeById(Long id);

    int insertNotice(Notice notice);

    int updateNotice(Notice notice);

    int deleteNoticeByIds(Long[] ids);

    /** 立即发布 */
    void publish(Long id);

    /** 撤回：不物理删除，状态置为已撤回 */
    void revoke(Long id);

    /** 定时任务调用：把到点的定时公告转为已发布，返回影响条数 */
    int publishDueNotices();

    /** 家长端：已发布且限全园或自己孩子班级 */
    List<Notice> selectVisibleNotices();
}
