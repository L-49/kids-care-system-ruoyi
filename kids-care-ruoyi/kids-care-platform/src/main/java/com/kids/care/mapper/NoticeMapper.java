package com.kids.care.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.kids.care.domain.Notice;

/**
 * Notice Mapper 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface NoticeMapper
{
    Notice selectNoticeById(Long id);

    List<Notice> selectNoticeList(Notice notice);

    int insertNotice(Notice notice);

    int updateNotice(Notice notice);

    int deleteNoticeById(Long id);

    int deleteNoticeByIds(Long[] ids);

    /** 只改状态 */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 改状态并写入发布时间 */
    int updateStatusAndTime(@Param("id") Long id, @Param("status") Integer status,
                            @Param("publishTime") Date publishTime);

    /** 定时发布：把到点的定时公告转为已发布 */
    int publishDue();
}
