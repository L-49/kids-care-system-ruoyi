package com.kids.care.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kids.care.service.INoticeService;

/**
 * 公告定时发布任务。
 *
 * <p>每分钟第 0 秒扫描一次，把 publish_time 已到期的「定时待发」公告转为「已发布」。
 * 只更新状态、不逐条查库，一条 update 语句完成，避免公告量增长后扫描变慢。</p>
 *
 * @author kids
 */
@Component
public class NoticePublishTask
{
    private static final Logger log = LoggerFactory.getLogger(NoticePublishTask.class);

    @Autowired
    private INoticeService noticeService;

    @Scheduled(cron = "0 * * * * ?")
    public void publishDue()
    {
        try
        {
            int rows = noticeService.publishDueNotices();
            if (rows > 0)
            {
                log.info("定时发布公告 {} 条", rows);
            }
        }
        catch (Exception e)
        {
            log.error("定时发布公告失败", e);
        }
    }
}
